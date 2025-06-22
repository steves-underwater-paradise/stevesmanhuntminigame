package com.steveplays.stevesmanhuntminigame.game;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import xyz.nucleoid.plasmid.api.game.GameCloseReason;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.common.GlobalWidgets;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeam;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamConfig;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamKey;
import xyz.nucleoid.plasmid.api.game.common.team.TeamManager;
import xyz.nucleoid.plasmid.api.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.api.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.api.game.player.JoinOffer;
import xyz.nucleoid.plasmid.api.game.player.PlayerSet;
import xyz.nucleoid.plasmid.api.game.rule.GameRuleType;
import xyz.nucleoid.plasmid.api.util.PlayerRef;
import net.minecraft.block.pattern.BlockPattern.Result;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.scoreboard.AbstractTeam.CollisionRule;
import net.minecraft.scoreboard.AbstractTeam.VisibilityRule;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameMode;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;
import xyz.nucleoid.stimuli.event.world.EndPortalOpenEvent;
import xyz.nucleoid.stimuli.event.world.NetherPortalOpenEvent;
import java.util.*;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Nullable;
import com.steveplays.stevesmanhuntminigame.util.TeamUtil;
import com.steveplays.stevesmanhuntminigame.util.WinUtil;

import static com.steveplays.stevesmanhuntminigame.StevesManhuntMiniGame.MOD_ID;
import static com.steveplays.stevesmanhuntminigame.util.TickUtil.TICKS_PER_SECOND;
import static com.steveplays.stevesmanhuntminigame.util.TeamUtil.HUNTERS_TEAM_ID;
import static com.steveplays.stevesmanhuntminigame.util.TeamUtil.RUNNERS_TEAM_ID;

public class StevesManhuntMiniGameActive {
    public final GameSpace gameSpace;

    private final ServerWorld overworld;
    @SuppressWarnings("unused") private final ServerWorld nether;
    private final ServerWorld end;
    private final StevesManhuntMiniGameConfig config;
    private final Object2ObjectMap<PlayerRef, StevesManhuntMiniGamePlayer> participants;
    private final StevesManhuntMiniGameSpawnLogic spawnLogic;
    private final StevesManhuntMiniGameStageManager stageManager;
    private final boolean ignoreWinState;
    private final GlobalWidgets widgets;
    private final TeamManager teamManager;
    private final GameTeam hunterTeam;
    private final GameTeam runnerTeam;
    private final StevesManhuntMiniGameSidebar timerBar;

    private StevesManhuntMiniGameActive(GameSpace gameSpace, ServerWorld overworld, ServerWorld nether, ServerWorld end, GlobalWidgets widgets, TeamManager teamManager,
            StevesManhuntMiniGameConfig config, Set<PlayerRef> participants) {
        this.gameSpace = gameSpace;
        this.config = config;
        this.spawnLogic = new StevesManhuntMiniGameSpawnLogic(overworld, config.mapConfig().spawnRadius);
        this.participants = new Object2ObjectOpenHashMap<>();
        this.overworld = overworld;
        this.nether = nether;
        this.end = end;

        for (PlayerRef player : participants) {
            this.participants.put(player, new StevesManhuntMiniGamePlayer());
        }

        this.stageManager = new StevesManhuntMiniGameStageManager();
        this.ignoreWinState = this.participants.size() <= 1;
        this.widgets = widgets;
        this.teamManager = teamManager;

        this.hunterTeam = new GameTeam(new GameTeamKey(HUNTERS_TEAM_ID), new GameTeamConfig(TeamUtil.getTeamNameStyled(HUNTERS_TEAM_ID), TeamUtil.getColorForTeam(HUNTERS_TEAM_ID), true,
                CollisionRule.ALWAYS, VisibilityRule.ALWAYS, TeamUtil.getTeamNamePrefixStyled(HUNTERS_TEAM_ID).copy().append(" "), Text.empty()));
        this.runnerTeam = new GameTeam(new GameTeamKey(RUNNERS_TEAM_ID), new GameTeamConfig(TeamUtil.getTeamNameStyled(RUNNERS_TEAM_ID), TeamUtil.getColorForTeam(RUNNERS_TEAM_ID), true,
                CollisionRule.ALWAYS, VisibilityRule.ALWAYS, Text.empty(), Text.empty()));

        this.timerBar = new StevesManhuntMiniGameSidebar();
    }

    public static void open(GameSpace gameSpace, ServerWorld overworld, ServerWorld nether, ServerWorld end, StevesManhuntMiniGameConfig config) {
        gameSpace.setActivity(game -> {
            Set<PlayerRef> participants = gameSpace.getPlayers().participants().stream().map(PlayerRef::of).collect(Collectors.toSet());
            GlobalWidgets widgets = GlobalWidgets.addTo(game);
            TeamManager teamManager = TeamManager.addTo(game);
            StevesManhuntMiniGameActive active = new StevesManhuntMiniGameActive(gameSpace, overworld, nether, end, widgets, teamManager, config, participants);

            game.allow(GameRuleType.PORTALS);

            game.listen(GameActivityEvents.ENABLE, active::onOpen);
            game.listen(GameActivityEvents.DISABLE, active::onClose);
            game.listen(GameActivityEvents.STATE_UPDATE, state -> state.canPlay(false));

            game.listen(GamePlayerEvents.OFFER, JoinOffer::acceptSpectators);
            game.listen(GamePlayerEvents.ACCEPT, joinAcceptor -> joinAcceptor.teleport(overworld, Vec3d.ZERO));
            game.listen(GamePlayerEvents.ADD, active::addPlayer);
            game.listen(GamePlayerEvents.REMOVE, active::removePlayer);

            game.listen(GameActivityEvents.TICK, active::tick);

            game.listen(PlayerDeathEvent.EVENT, active::onPlayerDeath);

            game.listen(NetherPortalOpenEvent.EVENT, active::onNetherPortalOpen);
            game.listen(EndPortalOpenEvent.EVENT, active::onEndPortalOpen);
        });
    }

    private void onOpen() {
        this.teamManager.addTeam(this.hunterTeam);
        this.teamManager.addTeam(this.runnerTeam);

        List<ServerPlayerEntity> participants = new ArrayList<>();
        for (var participant : this.gameSpace.getPlayers().participants()) {
            participants.add(participant);
        }

        var huntersTeamRatio = 0.2f;
        var random = Random.create();
        for (int i = 0; i < Math.clamp((int) Math.round(participants.size() * huntersTeamRatio), 1, Integer.MAX_VALUE); i++) {
            this.teamManager.addPlayerTo(participants.get(random.nextInt(participants.size())), this.hunterTeam.key());
        }

        for (var participant : this.gameSpace.getPlayers().participants()) {
            if (this.teamManager.teamFor(participant) == null) {
                this.teamManager.addPlayerTo(participant, this.runnerTeam.key());
            }

            this.spawnParticipant(participant);
        }
        for (var spectator : this.gameSpace.getPlayers().spectators()) {
            this.spawnSpectator(spectator);
        }

        this.stageManager.onOpen(this.overworld.getTime(), this.config);
        this.timerBar.onOpen(this.widgets, this.teamManager, this.hunterTeam.key(), this.runnerTeam.key());
    }

    private void onClose() {
        // TODO teardown logic
    }

    private void addPlayer(ServerPlayerEntity player) {
        if (!this.participants.containsKey(PlayerRef.of(player)) || this.gameSpace.getPlayers().spectators().contains(player)) {
            this.spawnSpectator(player);
        }
    }

    private void removePlayer(ServerPlayerEntity player) {
        this.participants.remove(PlayerRef.of(player));
    }

    private ActionResult onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
        var serverWorld = player.getServerWorld();
        var playerRandom = player.getRandom();
        for (int i = 0; i < 20; i++) {
            serverWorld.spawnParticles(ParticleTypes.POOF, player.getParticleX(1d), player.getRandomBodyY(), player.getParticleZ(1d), 1, playerRandom.nextGaussian() * 0.02d,
                    playerRandom.nextGaussian() * 0.02d, playerRandom.nextGaussian() * 0.02d, 0.02d);
        }
        player.getInventory().dropAll();
        if (this.teamManager.teamFor(player).equals(this.runnerTeam.key())) {
            this.spawnLogic.resetPlayer(player, GameMode.SPECTATOR);
            return ActionResult.FAIL;
        }

        this.respawnParticipant(player);
        return ActionResult.FAIL;
    }

    private ActionResult onNetherPortalOpen(ServerWorld serverworld1, BlockPos blockpos2) {
        return ActionResult.SUCCESS;
    }

    private ActionResult onEndPortalOpen(ItemUsageContext itemusagecontext1, Result result2) {
        return ActionResult.SUCCESS;
    }

    private void spawnParticipant(ServerPlayerEntity player) {
        player.setSpawnPoint(this.overworld.getRegistryKey(), this.overworld.getSpawnPos(), this.overworld.getSpawnAngle(), true, false);
        this.spawnLogic.resetPlayer(player, GameMode.SURVIVAL);
        this.spawnLogic.spawnPlayer(player);
    }

    private void respawnParticipant(ServerPlayerEntity player) {
        this.spawnLogic.respawnPlayer(player);
    }

    private void spawnSpectator(ServerPlayerEntity player) {
        this.spawnLogic.resetPlayer(player, GameMode.SPECTATOR);
        this.spawnLogic.spawnPlayer(player);
    }

    private void tick() {
        long time = this.overworld.getTime();
        this.timerBar.update(this.stageManager.getFinishTime() - time, this.config.timeLimitSeconds() * TICKS_PER_SECOND, this.teamManager, this.hunterTeam.key(), this.runnerTeam.key());

        StevesManhuntMiniGameStageManager.IdleTickResult result =
                this.stageManager.tick(time, this.gameSpace, this.teamManager, this.hunterTeam.key(), this.runnerTeam.key(), this.ignoreWinState, this.end);
        switch (result) {
            case CONTINUE_TICK:
                break;
            case TICK_FINISHED:
                return;
            case GAME_FINISHED:
                this.broadcastWin(WinUtil.checkWinResult(this.ignoreWinState, this.teamManager, this.hunterTeam.key(), this.runnerTeam.key(), this.end));
                for (ServerPlayerEntity player : gameSpace.getPlayers()) {
                    player.changeGameMode(GameMode.SPECTATOR);
                }
                return;
            case GAME_CLOSED:
                this.gameSpace.close(GameCloseReason.FINISHED);
                return;
        }
    }

    private void broadcastWin(WinUtil.WinResult winResult) {
        @Nullable var winningGameTeamKey = winResult.getWinningTeamKey();
        Text message;
        if (winningGameTeamKey != null) {
            message = Text.translatable(String.format("%s.game_ended_with_winner", MOD_ID), this.teamManager.getTeamConfig(winningGameTeamKey).name()).formatted(Formatting.GOLD);
        } else {
            message = Text.translatable(String.format("%s.game_ended_without_winner", MOD_ID)).formatted(Formatting.GOLD);
        }

        PlayerSet players = this.gameSpace.getPlayers();
        players.sendMessage(message);
        players.playSound(SoundEvents.ENTITY_VILLAGER_YES);
    }
}
