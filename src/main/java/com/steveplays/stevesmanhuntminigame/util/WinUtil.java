package com.steveplays.stevesmanhuntminigame.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.minecraft.server.world.ServerWorld;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamKey;
import xyz.nucleoid.plasmid.api.game.common.team.TeamManager;

public class WinUtil {
	public static @NotNull WinResult checkWinResult(boolean ignoreWinState, TeamManager teamManager, GameTeamKey hunterTeamKey, GameTeamKey runnerTeamKey, ServerWorld end) {
		// For testing purposes: don't end the game if we only ever had one participant
		if (ignoreWinState) {
			return WinResult.no();
		}

		if (teamManager.playersIn(runnerTeamKey).stream().allMatch(runner -> runner.isSpectator())) {
			return WinResult.win(hunterTeamKey);
		} else if (end.getEnderDragonFight().hasPreviouslyKilled()) {
			return WinResult.win(runnerTeamKey);
		}

		return WinResult.no();
	}

	public static class WinResult {
		final @Nullable GameTeamKey winningTeamKey;
		final boolean win;

		private WinResult(@Nullable GameTeamKey winningTeamKey, boolean win) {
			this.winningTeamKey = winningTeamKey;
			this.win = win;
		}

		static WinResult no() {
			return new WinResult(null, false);
		}

		static WinResult win(@NotNull GameTeamKey winningGameTeamKey) {
			return new WinResult(winningGameTeamKey, true);
		}

		public boolean isWin() {
			return this.win;
		}

		public @Nullable GameTeamKey getWinningTeamKey() {
			return this.winningTeamKey;
		}
	}
}
