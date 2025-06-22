package com.steveplays.stevesmanhuntminigame.util;

import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamConfig;
import static com.steveplays.stevesmanhuntminigame.StevesManhuntMiniGame.MOD_ID;
import org.jetbrains.annotations.NotNull;

public class TeamUtil {
	public static final @NotNull String HUNTERS_TEAM_ID = "hunters";
	public static final @NotNull String RUNNERS_TEAM_ID = "runners";

	public static @NotNull Text getTeamNameStyled(@NotNull String teamId) {
		return Text.translatable(String.format("%s.team.%s.name", MOD_ID, teamId)).styled(style -> TeamUtil.getStyleForTeam(teamId));
	}

	public static @NotNull Text getTeamNamePrefixStyled(@NotNull String teamId) {
		return Text.translatable(String.format("%s.team.%s.prefix", MOD_ID, teamId)).styled(style -> TeamUtil.getStyleForTeam(teamId));
	}

	public static @NotNull Style getStyleForTeam(@NotNull String teamId) {
		return Style.EMPTY.withColor(getColorForTeam(teamId).chatFormatting());
	}

	public static @NotNull GameTeamConfig.Colors getColorForTeam(@NotNull String teamId) {
		@NotNull GameTeamConfig.Colors teamColor;
		switch (teamId) {
			case HUNTERS_TEAM_ID: {
				teamColor = GameTeamConfig.Colors.from(DyeColor.RED);
				break;
			}
			case RUNNERS_TEAM_ID: {
				teamColor = GameTeamConfig.Colors.from(DyeColor.BLUE);
				break;
			}
			default: {
				teamColor = GameTeamConfig.Colors.from(DyeColor.WHITE);
				break;
			}
		}
		return teamColor;
	}
}
