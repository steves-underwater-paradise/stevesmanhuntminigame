package com.steveplays.stevesmanhuntminigame.game;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import xyz.nucleoid.plasmid.api.game.GameCloseReason;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.common.GlobalWidgets;
import xyz.nucleoid.plasmid.api.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.api.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.api.game.player.JoinOffer;
import xyz.nucleoid.plasmid.api.game.player.PlayerSet;
import xyz.nucleoid.plasmid.api.game.rule.GameRuleType;
import xyz.nucleoid.plasmid.api.util.PlayerRef;
import net.minecraft.block.pattern.BlockPattern.Result;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemUsageContext;
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
import static com.steveplays.stevesmanhuntminigame.util.TickUtil.TICKS_PER_SECOND;
import java.util.*;
import java.util.stream.Collectors;

public class StevesManhuntMiniGameActive {
    public final GameSpace gameSpace;

    private final StevesManhuntMiniGameConfig config;
    private final Object2ObjectMap<PlayerRef, StevesManhuntMiniGamePlayer> participants;
    private final StevesManhuntMiniGameSpawnLogic spawnLogic;
    private final StevesManhuntMiniGameStageManager stageManager;
    private final boolean ignoreWinState;
    private final StevesManhuntMiniGameTimerBar timerBar;
    private final ServerWorld overworld;
    private final ServerWorld nether;
    private final ServerWorld end;

    private StevesManhuntMiniGameActive(GameSpace gameSpace, ServerWorld overworld, ServerWorld nether, ServerWorld end, GlobalWidgets widgets, StevesManhuntMiniGameConfig config,
            Set<PlayerRef> participants) {
        this.gameSpace = gameSpace;
        this.config = config;
        this.spawnLogic = new StevesManhuntMiniGameSpawnLogic(gameSpace, overworld);
        this.participants = new Object2ObjectOpenHashMap<>();
        this.overworld = overworld;
        this.nether = nether;
        this.end = end;

        for (PlayerRef player : participants) {
            this.participants.put(player, new StevesManhuntMiniGamePlayer());
        }

        this.stageManager = new StevesManhuntMiniGameStageManager();
        this.ignoreWinState = this.participants.size() <= 1;
        this.timerBar = new StevesManhuntMiniGameTimerBar(widgets);
    }

    public static void open(GameSpace gameSpace, ServerWorld overworld, ServerWorld nether, ServerWorld end, StevesManhuntMiniGameConfig config) {
        gameSpace.setActivity(game -> {
            Set<PlayerRef> participants = gameSpace.getPlayers().participants().stream().map(PlayerRef::of).collect(Collectors.toSet());
            GlobalWidgets widgets = GlobalWidgets.addTo(game);
            StevesManhuntMiniGameActive active = new StevesManhuntMiniGameActive(gameSpace, overworld, nether, end, widgets, config, participants);

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
        for (var participant : this.gameSpace.getPlayers().participants()) {
            this.spawnParticipant(participant);
        }
        for (var spectator : this.gameSpace.getPlayers().spectators()) {
            this.spawnSpectator(spectator);
        }

        this.stageManager.onOpen(this.overworld.getTime(), this.config);
        // TODO setup logic
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
        // TODO handle death
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

        StevesManhuntMiniGameStageManager.IdleTickResult result = this.stageManager.tick(time, gameSpace);

        switch (result) {
            case CONTINUE_TICK:
                break;
            case TICK_FINISHED:
                return;
            case GAME_FINISHED:
                this.broadcastWin(this.checkWinResult());
                return;
            case GAME_CLOSED:
                this.gameSpace.close(GameCloseReason.FINISHED);
                return;
        }

        this.timerBar.update(this.stageManager.getFinishTime() - time, this.config.timeLimitSeconds() * TICKS_PER_SECOND);

        // TODO tick logic
    }

    private void broadcastWin(WinResult result) {
        ServerPlayerEntity winningPlayer = result.getWinningPlayer();

        Text message;
        if (winningPlayer != null) {
            message = winningPlayer.getDisplayName().copy().append(" has won the game!").formatted(Formatting.GOLD);
        } else {
            message = Text.literal("The game ended, but nobody won!").formatted(Formatting.GOLD);
        }

        PlayerSet players = this.gameSpace.getPlayers();
        players.sendMessage(message);
        players.playSound(SoundEvents.ENTITY_VILLAGER_YES);
    }

    private WinResult checkWinResult() {
        // for testing purposes: don't end the game if we only ever had one participant
        if (this.ignoreWinState) {
            return WinResult.no();
        }

        ServerPlayerEntity winningPlayer = null;

        // TODO win result logic
        return WinResult.no();
    }

    static class WinResult {
        final ServerPlayerEntity winningPlayer;
        final boolean win;

        private WinResult(ServerPlayerEntity winningPlayer, boolean win) {
            this.winningPlayer = winningPlayer;
            this.win = win;
        }

        static WinResult no() {
            return new WinResult(null, false);
        }

        static WinResult win(ServerPlayerEntity player) {
            return new WinResult(player, true);
        }

        public boolean isWin() {
            return this.win;
        }

        public ServerPlayerEntity getWinningPlayer() {
            return this.winningPlayer;
        }
    }
}
