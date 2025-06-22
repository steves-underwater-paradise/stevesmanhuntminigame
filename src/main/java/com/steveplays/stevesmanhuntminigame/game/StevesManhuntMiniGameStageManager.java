package com.steveplays.stevesmanhuntminigame.game;

import com.google.common.collect.ImmutableSet;
import com.steveplays.stevesmanhuntminigame.util.WinUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamKey;
import xyz.nucleoid.plasmid.api.game.common.team.TeamManager;
import xyz.nucleoid.plasmid.api.game.player.PlayerSet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import static com.steveplays.stevesmanhuntminigame.util.TickUtil.TICKS_PER_SECOND;
import static com.steveplays.stevesmanhuntminigame.util.TickUtil.TICKS_PER_SECOND_FLOAT;

public class StevesManhuntMiniGameStageManager {
    private final Object2ObjectMap<ServerPlayerEntity, FrozenPlayer> frozen;

    private long startTime = -1;
    private long finishTime = -1;
    private long closeTime = -1;

    public StevesManhuntMiniGameStageManager() {
        this.frozen = new Object2ObjectOpenHashMap<>();
    }

    public void onOpen(long time, StevesManhuntMiniGameConfig config) {
        this.startTime = time - (time % 20) + (4 * 20) + 19;
        this.finishTime = this.startTime + (config.timeLimitSeconds() * 20);
    }

    public long getFinishTime() {
        return finishTime;
    }

    public IdleTickResult tick(long time, GameSpace gameSpace, TeamManager teamManager, GameTeamKey hunterTeamKey, GameTeamKey runnerTeamKey, boolean ignoreWinState, ServerWorld end) {
        // Game has finished. Wait a few seconds before finally closing the game.
        if (this.closeTime > 0) {
            if (time >= this.closeTime) {
                return IdleTickResult.GAME_CLOSED;
            }
            return IdleTickResult.TICK_FINISHED;
        }

        // Game hasn't started yet. Display a countdown before it begins.
        if (this.startTime > time) {
            this.tickStartWaiting(time, gameSpace);
            return IdleTickResult.TICK_FINISHED;
        }

        // Game has just finished. Transition to the waiting-before-close state.
        if (time > this.finishTime || WinUtil.checkWinResult(ignoreWinState, teamManager, hunterTeamKey, runnerTeamKey, end).isWin() || gameSpace.getPlayers().isEmpty()) {
            this.closeTime = time + (5 * TICKS_PER_SECOND);
            return IdleTickResult.GAME_FINISHED;
        }

        return IdleTickResult.CONTINUE_TICK;
    }

    private void tickStartWaiting(long time, GameSpace space) {
        float timeUntilStartSeconds = (this.startTime - time) / TICKS_PER_SECOND_FLOAT;
        if (timeUntilStartSeconds > 1) {
            for (ServerPlayerEntity player : space.getPlayers()) {
                if (player.isSpectator()) {
                    continue;
                }

                FrozenPlayer state = this.frozen.computeIfAbsent(player, p -> new FrozenPlayer());
                if (state.lastPos == null) {
                    state.lastPos = player.getPos();
                }

                player.networkHandler.requestTeleport(state.lastPos.getX(), state.lastPos.getY(), state.lastPos.getZ(), player.getYaw(), player.getPitch(), ImmutableSet.of(PositionFlag.X_ROT, PositionFlag.Y_ROT));
            }
        }

        int timeUntilStartSecondsFloored = (int) Math.floor(timeUntilStartSeconds) - 1;
        if ((this.startTime - time) % 20 == 0) {
            PlayerSet players = space.getPlayers();
            if (timeUntilStartSecondsFloored > 0) {
                players.showTitle(Text.literal(Integer.toString(timeUntilStartSecondsFloored)).formatted(Formatting.BOLD), 20);
                players.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.5f, 1.0f);
            } else {
                players.showTitle(Text.literal("Go!").formatted(Formatting.BOLD), 20);
                players.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.5f, 2.0f);
            }
        }
    }

    public static class FrozenPlayer {
        public Vec3d lastPos;
    }

    public enum IdleTickResult {
        CONTINUE_TICK, TICK_FINISHED, GAME_FINISHED, GAME_CLOSED,
    }
}
