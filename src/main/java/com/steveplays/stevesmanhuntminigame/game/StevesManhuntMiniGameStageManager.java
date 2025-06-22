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
import static com.steveplays.stevesmanhuntminigame.util.TickUtil.TICKS_PER_SECOND;
import static com.steveplays.stevesmanhuntminigame.util.TickUtil.TICKS_PER_SECOND_FLOAT;
import org.jetbrains.annotations.NotNull;

public class StevesManhuntMiniGameStageManager {
    private static final int START_COUNTDOWN_LENGTH_SECONDS = 5;
    private static final int ROLE_REVEAL_LENGTH_SECONDS = 10;
    private static final @NotNull String HUNTER_DESCRIPTION = "Eliminate all Runners before they kill the Ender Dragon to win.";
    private static final @NotNull String RUNNER_DESCRIPTION = "Kill the Ender Dragon while avoiding Hunters, who are trying to kill you.";

    private final Object2ObjectMap<ServerPlayerEntity, FrozenPlayer> frozen;

    private long roleRevealEndTime = -1;
    private long startTime = -1;
    private long finishTime = -1;
    private long closeTime = -1;
    private boolean hasRevealedRoles = false;

    public StevesManhuntMiniGameStageManager() {
        this.frozen = new Object2ObjectOpenHashMap<>();
    }

    public void onOpen(long time, StevesManhuntMiniGameConfig config) {
        this.roleRevealEndTime = time - (time % TICKS_PER_SECOND) + (ROLE_REVEAL_LENGTH_SECONDS * TICKS_PER_SECOND) + 19;
        this.startTime = roleRevealEndTime - (roleRevealEndTime % TICKS_PER_SECOND) + (START_COUNTDOWN_LENGTH_SECONDS * TICKS_PER_SECOND) + 19;
        this.finishTime = this.startTime + (config.timeLimitSeconds() * TICKS_PER_SECOND);
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
            this.tickStartWaiting(time, gameSpace, teamManager, hunterTeamKey, runnerTeamKey);
            return IdleTickResult.TICK_FINISHED;
        }

        // Game has just finished. Transition to the waiting-before-close state.
        if (time > this.finishTime || WinUtil.checkWinResult(ignoreWinState, teamManager, hunterTeamKey, runnerTeamKey, end).isWin() || gameSpace.getPlayers().isEmpty()) {
            this.closeTime = time + (5 * TICKS_PER_SECOND);
            return IdleTickResult.GAME_FINISHED;
        }

        return IdleTickResult.CONTINUE_TICK;
    }

    private void tickStartWaiting(long time, GameSpace space, TeamManager teamManager, GameTeamKey hunterTeamKey, GameTeamKey runnerTeamKey) {
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

                player.networkHandler.requestTeleport(state.lastPos.getX(), state.lastPos.getY(), state.lastPos.getZ(), player.getYaw(), player.getPitch(),
                        ImmutableSet.of(PositionFlag.X_ROT, PositionFlag.Y_ROT));
            }
        }

        int timeUntilStartSecondsFloored = (int) Math.floor(timeUntilStartSeconds) - 1;
        if ((this.startTime - time) % 20 != 0) {
            return;
        }

        PlayerSet participants = space.getPlayers().participants();
        for (var participant : participants) {
            if (timeUntilStartSecondsFloored > 0) {
                var timeUntilRoleRevealEndFloored = (int) Math.floor((this.roleRevealEndTime - time) / TICKS_PER_SECOND_FLOAT) - 1;
                var playerTeamKey = teamManager.teamFor(participant);
                var playerIsHunter = playerTeamKey.equals(hunterTeamKey);
                if (hasRevealedRoles) {
                    if (timeUntilRoleRevealEndFloored > 0) {
                        if (playerIsHunter) {
                            participant.sendMessage(Text.literal("Infinite respawns"), true);
                        } else {
                            participant.sendMessage(Text.literal("No respawns"), true);
                        }
                        continue;
                    }

                    participants.showTitle(Text.literal(Integer.toString(timeUntilStartSecondsFloored)).formatted(Formatting.BOLD), Text.empty(), 5, TICKS_PER_SECOND - 5, 0);
                    participants.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.5f, 1.0f);
                    continue;
                }

                participants.showTitle(Text.literal(String.format("You are a %s.", teamManager.getTeamConfig(playerTeamKey).name().getString())).formatted(Formatting.BOLD),
                        Text.literal(playerIsHunter ? HUNTER_DESCRIPTION : RUNNER_DESCRIPTION), 5, ROLE_REVEAL_LENGTH_SECONDS * TICKS_PER_SECOND - 5, 0);
                participants.playSound(SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), SoundCategory.PLAYERS, 0.5f, 0.5f);
                hasRevealedRoles = true;
                continue;
            }

            participants.showTitle(Text.literal("Go!").formatted(Formatting.BOLD), Text.empty(), 5, TICKS_PER_SECOND - 5, 0);
            participants.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.5f, 2.0f);
        }

    }

    public static class FrozenPlayer {
        public Vec3d lastPos;
    }

    public enum IdleTickResult {
        CONTINUE_TICK, TICK_FINISHED, GAME_FINISHED, GAME_CLOSED,
    }
}
