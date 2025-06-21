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
import net.minecraft.scoreboard.AbstractTeam.CollisionRule;
import net.minecraft.scoreboard.AbstractTeam.VisibilityRule;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameMode;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;
import xyz.nucleoid.stimuli.event.world.EndPortalOpenEvent;
import xyz.nucleoid.stimuli.event.world.NetherPortalOpenEvent;
import static com.steveplays.stevesmanhuntminigame.util.TickUtil.TICKS_PER_SECOND;
import java.util.*;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Nullable;
import com.steveplays.stevesmanhuntminigame.util.WinUtil;

public class StevesManhuntMiniGameActive {
    public final GameSpace gameSpace;

    private final ServerWorld overworld;
    private final ServerWorld nether;
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
    private final StevesManhuntMiniGameSideBar timerBar;

    private StevesManhuntMiniGameActive(GameSpace gameSpace, ServerWorld overworld, ServerWorld nether, ServerWorld end, GlobalWidgets widgets, TeamManager teamManager,
            StevesManhuntMiniGameConfig config, Set<PlayerRef> participants) {
        this.gameSpace = gameSpace;
        this.config = config;
        this.spawnLogic = new StevesManhuntMiniGameSpawnLogic(gameSpace, overworld, config.mapConfig().spawnRadius);
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

        // TODO: Replace literal text with translatable text
        this.hunterTeam = new GameTeam(new GameTeamKey("hunters"), new GameTeamConfig(Text.literal("Hunters"), GameTeamConfig.Colors.from(DyeColor.RED), true, CollisionRule.ALWAYS,
                VisibilityRule.ALWAYS, Text.literal("[Hunter] ").styled(style -> style.withColor(Formatting.RED)), Text.empty()));
        this.runnerTeam = new GameTeam(new GameTeamKey("runners"),
                new GameTeamConfig(Text.literal("Runners"), GameTeamConfig.Colors.from(DyeColor.BLUE), true, CollisionRule.ALWAYS, VisibilityRule.ALWAYS, Text.empty(), Text.empty()));

        this.timerBar = new StevesManhuntMiniGameSideBar(widgets);
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
        for (int i = 0; i < (int) Math.round(participants.size() * huntersTeamRatio); i++) {
            this.teamManager.addPlayerTo(participants.get(random.nextInt(participants.size())), this.hunterTeam.key());
        }

        for (var participant : this.gameSpace.getPlayers().participants()) {
            if (this.teamManager.teamFor(participant) == null) {
                this.teamManager.addPlayerTo(participant, this.runnerTeam.key());
                participant.setCustomName(Text.literal("Runner").styled(style -> style.withColor(Formatting.BLUE)));
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
        // TODO: Fix beds allowing players to respawn in different game spaces
        this.spawnParticipant(player);
        return ActionResult.CONSUME;
    }

    private ActionResult onNetherPortalOpen(ServerWorld serverworld1, BlockPos blockpos2) {
        return ActionResult.SUCCESS;
    }

    private ActionResult onEndPortalOpen(ItemUsageContext itemusagecontext1, Result result2) {
        return ActionResult.SUCCESS;
    }

    private void spawnParticipant(ServerPlayerEntity player) {
        this.spawnLogic.resetPlayer(player, GameMode.SURVIVAL);
        this.spawnLogic.spawnPlayer(player);
    }

    private void spawnSpectator(ServerPlayerEntity player) {
        this.spawnLogic.resetPlayer(player, GameMode.SPECTATOR);
        this.spawnLogic.spawnPlayer(player);
    }

    private void tick() {
        long time = this.overworld.getTime();
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

        this.timerBar.update(this.stageManager.getFinishTime() - time, this.config.timeLimitSeconds() * TICKS_PER_SECOND);
    }

    private void broadcastWin(WinUtil.WinResult winResult) {
        @Nullable var winningGameTeamKey = winResult.getWinningTeamKey();
        Text message;
        if (winningGameTeamKey != null) {
            message = this.teamManager.getTeamConfig(winningGameTeamKey).name().copy().append(" have won the game!").formatted(Formatting.GOLD);
        } else {
            message = Text.literal("The game ended, but nobody won!").formatted(Formatting.GOLD);
        }

        PlayerSet players = this.gameSpace.getPlayers();
        players.sendMessage(message);
        players.playSound(SoundEvents.ENTITY_VILLAGER_YES);
    }
}
