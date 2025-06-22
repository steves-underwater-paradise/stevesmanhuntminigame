package com.steveplays.stevesmanhuntminigame.game;

import com.google.common.collect.ImmutableSet;
import com.steveplays.stevesmanhuntminigame.util.PlayerUtil;
import com.steveplays.stevesmanhuntminigame.util.TeamUtil;
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

import static com.steveplays.stevesmanhuntminigame.StevesManhuntMiniGame.MOD_ID;
import static com.steveplays.stevesmanhuntminigame.util.TickUtil.TICKS_PER_SECOND;
import static com.steveplays.stevesmanhuntminigame.util.TickUtil.TICKS_PER_SECOND_FLOAT;
import static com.steveplays.stevesmanhuntminigame.util.TeamUtil.HUNTERS_TEAM_ID;
import static com.steveplays.stevesmanhuntminigame.util.TeamUtil.RUNNERS_TEAM_ID;

public class StevesManhuntMiniGameStageManager {
    private static final int START_COUNTDOWN_LENGTH_SECONDS = 5;
    private static final int ROLE_REVEAL_LENGTH_SECONDS = 10;

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

        var timeUntilRoleRevealEndFloored = (int) Math.floor((this.roleRevealEndTime - time) / TICKS_PER_SECOND_FLOAT) - 1;
        PlayerSet participants = space.getPlayers().participants();
        for (var participant : participants) {
            if (timeUntilStartSecondsFloored > 0) {
                var participantTeamKey = teamManager.teamFor(participant);
                var participantIsHunter = participantTeamKey.equals(hunterTeamKey);
                if (hasRevealedRoles) {
                    if (timeUntilRoleRevealEndFloored > 0) {
                        if (participantIsHunter) {
                            participant.sendMessage(Text.translatable(String.format("%s.infinite_respawns", MOD_ID)), true);
                        } else {
                            participant.sendMessage(Text.translatable(String.format("%s.no_respawns", MOD_ID)), true);
                        }
                        continue;
                    }

                    PlayerUtil.showTitle(participant, Text.literal(Integer.toString(timeUntilStartSecondsFloored)).formatted(Formatting.BOLD), Text.empty(), 5, TICKS_PER_SECOND - 5, 0);
                    participant.playSoundToPlayer(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.5f, 1.0f);
                    continue;
                }

                PlayerUtil.showTitle(participant,
                        (Text.translatable(String.format("%s.your_role", MOD_ID)).append(" ").append(TeamUtil.getTeamNamePrefixStyled(teamManager.teamFor(participant).id())).append("."))
                                .formatted(Formatting.BOLD),
                        participantIsHunter
                                ? Text.translatable(String.format("%s.team.%s.description_0", MOD_ID, HUNTERS_TEAM_ID)).append(" ").append(TeamUtil.getTeamNameStyled(RUNNERS_TEAM_ID)).append(" ")
                                        .append(Text.translatable(String.format("%s.team.%s.description_1", MOD_ID, HUNTERS_TEAM_ID)))
                                : Text.translatable(String.format("%s.team.%s.description_0", MOD_ID, RUNNERS_TEAM_ID)).append(" ").append(TeamUtil.getTeamNameStyled(HUNTERS_TEAM_ID))
                                        .append(Text.translatable(String.format("%s.team.%s.description_1", MOD_ID, RUNNERS_TEAM_ID))),
                        5, ROLE_REVEAL_LENGTH_SECONDS * TICKS_PER_SECOND - 5, 0);
                participant.playSoundToPlayer(SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), SoundCategory.PLAYERS, 0.5f, 0.5f);
                hasRevealedRoles = true;
                continue;
            }

            PlayerUtil.showTitle(participant, Text.translatable(String.format("%s.go", MOD_ID)).formatted(Formatting.BOLD), Text.empty(), 5, TICKS_PER_SECOND - 5, 0);
            participant.playSoundToPlayer(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.5f, 2.0f);
        }

    }

    public static class FrozenPlayer {
        public Vec3d lastPos;
    }

    public enum IdleTickResult {
        CONTINUE_TICK, TICK_FINISHED, GAME_FINISHED, GAME_CLOSED,
    }
}
