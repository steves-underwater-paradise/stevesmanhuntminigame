package com.steveplays.stevesmanhuntminigame.game;

import net.minecraft.scoreboard.AbstractTeam.VisibilityRule;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import xyz.nucleoid.plasmid.api.game.common.GlobalWidgets;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamKey;
import xyz.nucleoid.plasmid.api.game.common.team.TeamManager;
import xyz.nucleoid.plasmid.api.game.common.widget.SidebarWidget;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import com.steveplays.stevesmanhuntminigame.util.TeamUtil;

import static com.steveplays.stevesmanhuntminigame.StevesManhuntMiniGame.MOD_ID;
import static com.steveplays.stevesmanhuntminigame.util.TickUtil.TICKS_PER_SECOND;
import static com.steveplays.stevesmanhuntminigame.util.TeamUtil.HUNTERS_TEAM_ID;
import static com.steveplays.stevesmanhuntminigame.util.TeamUtil.RUNNERS_TEAM_ID;

// Compass ASCII art (remove periods; without direction indicator):
// ....███████████
// ..██...........██
// .█...............█
// █.................█
// █........█........█
// █.................█
// .█...............█
// ..██...........██
// ....███████████
public final class StevesManhuntMiniGameSidebar {
    private static final @NotNull Text SIDEBAR_WIDGET_TITLE = Text.translatable(String.format("gameType.%s.%s", MOD_ID, MOD_ID)).styled(style -> style.withBold(true));
    private static final @NotNull Text[] COMPASS = new Text[] {Text.literal("    ███"), Text.literal("  █      █"), Text.literal("█          █"), Text.literal("█    █    █"),
            Text.literal("█          █"), Text.literal("  █      █"), Text.literal("    ███")};
    private static final @NotNull Text[] COMPASS_NORTH = new Text[] {Text.literal("    ███").styled(style -> style.withColor(Formatting.GRAY)),
            Text.literal("  █  ").styled(style -> style.withColor(Formatting.GRAY)).append(Text.literal("█").styled(style -> style.withColor(Formatting.RED)))
                    .append(Text.literal("  █").styled(style -> style.withColor(Formatting.GRAY))),
            Text.literal("█    ").styled(style -> style.withColor(Formatting.GRAY)).append(Text.literal("█").styled(style -> style.withColor(Formatting.RED)))
                    .append(Text.literal("    █").styled(style -> style.withColor(Formatting.GRAY))),
            Text.literal("█    ").styled(style -> style.withColor(Formatting.GRAY)).append(Text.literal("█").styled(style -> style.withColor(Formatting.RED)))
                    .append(Text.literal("    █").styled(style -> style.withColor(Formatting.GRAY))),
            Text.literal("█    █    █").styled(style -> style.withColor(Formatting.GRAY)), Text.literal("  █      █").styled(style -> style.withColor(Formatting.GRAY)),
            Text.literal("    ███").styled(style -> style.withColor(Formatting.GRAY))};
    private static final @NotNull Text[] COMPASS_NORTH_EAST =
            new Text[] {Text.literal("    ███").styled(style -> style.withColor(Formatting.GRAY)), Text.literal("  █      █").styled(style -> style.withColor(Formatting.GRAY)),
                    Text.literal("█      ").styled(style -> style.withColor(Formatting.GRAY)).append(Text.literal("█").styled(style -> style.withColor(Formatting.RED)))
                            .append(Text.literal("  █").styled(style -> style.withColor(Formatting.GRAY))),
                    Text.literal("█    ").styled(style -> style.withColor(Formatting.GRAY)).append(Text.literal("█").styled(style -> style.withColor(Formatting.RED)))
                            .append(Text.literal("    █").styled(style -> style.withColor(Formatting.GRAY))),
                    Text.literal("█  █      █").styled(style -> style.withColor(Formatting.GRAY)), Text.literal("  █      █").styled(style -> style.withColor(Formatting.GRAY)),
                    Text.literal("    ███").styled(style -> style.withColor(Formatting.GRAY))};
    private static final @NotNull Text[] COMPASS_EAST = new Text[] {Text.literal("    ███").styled(style -> style.withColor(Formatting.GRAY)),
            Text.literal("  █      █").styled(style -> style.withColor(Formatting.GRAY)), Text.literal("█          █").styled(style -> style.withColor(Formatting.GRAY)),
            Text.literal("█  █").styled(style -> style.withColor(Formatting.GRAY)).append(Text.literal("███").styled(style -> style.withColor(Formatting.RED))),
            Text.literal("█          █").styled(style -> style.withColor(Formatting.GRAY)), Text.literal("  █      █").styled(style -> style.withColor(Formatting.GRAY)),
            Text.literal("    ███").styled(style -> style.withColor(Formatting.GRAY))};
    private static final @NotNull Text[] COMPASS_SOUTH_EAST = new Text[] {Text.literal("    ███").styled(style -> style.withColor(Formatting.GRAY)),
            Text.literal("  █      █").styled(style -> style.withColor(Formatting.GRAY)), Text.literal("█  █      █").styled(style -> style.withColor(Formatting.GRAY)),
            Text.literal("█    ").styled(style -> style.withColor(Formatting.GRAY)).append(Text.literal("█").styled(style -> style.withColor(Formatting.RED)))
                    .append(Text.literal("    █").styled(style -> style.withColor(Formatting.GRAY))),
            Text.literal("█      ").styled(style -> style.withColor(Formatting.GRAY)).append(Text.literal("█").styled(style -> style.withColor(Formatting.RED)))
                    .append(Text.literal("  █").styled(style -> style.withColor(Formatting.GRAY))),
            Text.literal("  █      █").styled(style -> style.withColor(Formatting.GRAY)), Text.literal("    ███").styled(style -> style.withColor(Formatting.GRAY))};
    private static final @NotNull Text[] COMPASS_SOUTH = new Text[] {Text.literal("    ███").styled(style -> style.withColor(Formatting.GRAY)),
            Text.literal("  █      █").styled(style -> style.withColor(Formatting.GRAY)), Text.literal("█    █    █").styled(style -> style.withColor(Formatting.GRAY)),
            Text.literal("█    ").styled(style -> style.withColor(Formatting.GRAY)).append(Text.literal("█").styled(style -> style.withColor(Formatting.RED)))
                    .append(Text.literal("    █").styled(style -> style.withColor(Formatting.GRAY))),
            Text.literal("█    ").styled(style -> style.withColor(Formatting.GRAY)).append(Text.literal("█").styled(style -> style.withColor(Formatting.RED)))
                    .append(Text.literal("    █").styled(style -> style.withColor(Formatting.GRAY))),
            Text.literal("  █  ").styled(style -> style.withColor(Formatting.GRAY)).append(Text.literal("█").styled(style -> style.withColor(Formatting.RED)))
                    .append(Text.literal("  █").styled(style -> style.withColor(Formatting.GRAY))),
            Text.literal("    ███").styled(style -> style.withColor(Formatting.GRAY))};
    private static final @NotNull Text[] COMPASS_SOUTH_WEST = new Text[] {Text.literal("    ███").styled(style -> style.withColor(Formatting.GRAY)),
            Text.literal("  █      █").styled(style -> style.withColor(Formatting.GRAY)), Text.literal("█      █  █").styled(style -> style.withColor(Formatting.GRAY)),
            Text.literal("█    ").styled(style -> style.withColor(Formatting.GRAY)).append(Text.literal("█").styled(style -> style.withColor(Formatting.RED)))
                    .append(Text.literal("    █").styled(style -> style.withColor(Formatting.GRAY))),
            Text.literal("█  ").styled(style -> style.withColor(Formatting.GRAY)).append(Text.literal("█").styled(style -> style.withColor(Formatting.RED)))
                    .append(Text.literal("      █").styled(style -> style.withColor(Formatting.GRAY))),
            Text.literal("  █      █").styled(style -> style.withColor(Formatting.GRAY)), Text.literal("    ███").styled(style -> style.withColor(Formatting.GRAY))};
    private static final @NotNull Text[] COMPASS_WEST = new Text[] {Text.literal("    ███").styled(style -> style.withColor(Formatting.GRAY)),
            Text.literal("  █      █").styled(style -> style.withColor(Formatting.GRAY)), Text.literal("█          █").styled(style -> style.withColor(Formatting.GRAY)),
            Text.literal("█").styled(style -> style.withColor(Formatting.GRAY)).append(Text.literal("███").styled(style -> style.withColor(Formatting.RED)))
                    .append(Text.literal("█  █").styled(style -> style.withColor(Formatting.GRAY))),
            Text.literal("█          █").styled(style -> style.withColor(Formatting.GRAY)), Text.literal("  █      █").styled(style -> style.withColor(Formatting.GRAY)),
            Text.literal("    ███").styled(style -> style.withColor(Formatting.GRAY))};
    private static final @NotNull Text[] COMPASS_NORTH_WEST =
            new Text[] {Text.literal("    ███").styled(style -> style.withColor(Formatting.GRAY)), Text.literal("  █      █").styled(style -> style.withColor(Formatting.GRAY)),
                    Text.literal("█  ").styled(style -> style.withColor(Formatting.GRAY)).append(Text.literal("█").styled(style -> style.withColor(Formatting.RED)))
                            .append(Text.literal("      █").styled(style -> style.withColor(Formatting.GRAY))),
                    Text.literal("█    ").styled(style -> style.withColor(Formatting.GRAY)).append(Text.literal("█").styled(style -> style.withColor(Formatting.RED)))
                            .append(Text.literal("    █").styled(style -> style.withColor(Formatting.GRAY))),
                    Text.literal("█      █  █").styled(style -> style.withColor(Formatting.GRAY)), Text.literal("  █      █").styled(style -> style.withColor(Formatting.GRAY)),
                    Text.literal("    ███").styled(style -> style.withColor(Formatting.GRAY))};

    private Map<UUID, SidebarWidget> hunterSidebarWidgets = new HashMap<>();
    private SidebarWidget runnerSidebarWidget;

    public StevesManhuntMiniGameSidebar() {
        // NO-OP
    }

    public void onOpen(GlobalWidgets widgets, TeamManager teamManager, GameTeamKey hunterTeamKey, GameTeamKey runnerTeamKey) {
        for (var hunter : teamManager.playersIn(hunterTeamKey)) {
            var hunterSideBarWidget = widgets.addSidebar(SIDEBAR_WIDGET_TITLE, participant -> participant.getUuid().equals(hunter.getUuid()));
            hunterSidebarWidgets.put(hunter.getUuid(), hunterSideBarWidget);
        }
        this.runnerSidebarWidget = widgets.addSidebar(SIDEBAR_WIDGET_TITLE, participant -> teamManager.teamFor(participant).equals(runnerTeamKey));
    }

    public void update(long ticksUntilEnd, long totalTicksUntilEnd, StevesManhuntMiniGameConfig config, TeamManager teamManager, GameTeamKey hunterTeamKey, GameTeamKey runnerTeamKey) {
        for (var hunter : teamManager.playersIn(hunterTeamKey)) {
            var hunterSidebarWidget = hunterSidebarWidgets.get(hunter.getUuid());
            hunterSidebarWidget.clearLines();
            this.addGameModeInformationToSidebarWidget(hunterSidebarWidget, HUNTERS_TEAM_ID, ticksUntilEnd, config);

            var runners = teamManager.playersIn(runnerTeamKey);
            if (runners.stream().anyMatch(runner -> !runner.isSpectator() && runner.getServerWorld().getRegistryKey().equals(hunter.getServerWorld().getRegistryKey()))) {
                float closestRunnerDistance = Float.MAX_VALUE;
                @Nullable ServerPlayerEntity closestRunner = null;
                for (var runner : runners) {
                    if (closestRunner == null) {
                        closestRunner = runner;
                    }

                    var distanceToRunner = hunter.distanceTo(runner);
                    if (distanceToRunner < closestRunnerDistance) {
                        closestRunnerDistance = distanceToRunner;
                        closestRunner = runner;
                    }
                }
                if (closestRunner == null) {
                    return;
                }

                hunterSidebarWidget.addLines(Text.literal(""),
                        Text.translatable(String.format("%s.closest", MOD_ID)).append(" ").append(TeamUtil.getTeamNamePrefixStyled(teamManager.teamFor(closestRunner).id())).append(": ")
                                .append(closestRunner.getDisplayName()),
                        Text.literal("- ").append(Text.translatable(String.format("%s.distance", MOD_ID)))
                                .append((Text.literal(String.format(": %.2f", closestRunnerDistance)).append(" ").append(Text.translatable(String.format("%s.meters_suffix", MOD_ID))))
                                        .styled(style -> style.withColor(Formatting.GRAY))),
                        Text.literal("- ").append(Text.translatable(String.format("%s.direction", MOD_ID))).append(":"));
                var closestRunnerPosition = closestRunner.getPos();
                var directionToRunner = new Vec3d(closestRunnerPosition.x, hunter.getY(), closestRunnerPosition.z).subtract(hunter.getPos());
                var hunterForward = hunter.getRotationVector().toVector3f();
                var hunterRight = new Vector3f(hunterForward).rotateY((float) Math.toRadians(90d));
                var hunterUp = new Vector3f(hunterForward).cross(hunterRight);
                var directionToRunnerAngle = (float) (1d
                        - ((Math.toDegrees(new Vector3f(-hunterForward.x(), -hunterForward.y(), -hunterForward.z()).angleSigned(directionToRunner.normalize().toVector3f(), hunterUp)) + 180d) / 360d));
                hunterSidebarWidget.addLines(getCompassWithDirectionIndicator(directionToRunnerAngle));
            } else {
                hunterSidebarWidget.addLines(Text.translatable(String.format("%s.all_enemy_players_are_in_another_dimension_0", MOD_ID)).append(" ").append(TeamUtil.getTeamNameStyled(RUNNERS_TEAM_ID))
                        .append(" ").append(Text.translatable(String.format("%s.all_enemy_players_are_in_another_dimension_1", MOD_ID))));
            }
        }

        runnerSidebarWidget.clearLines();
        this.addGameModeInformationToSidebarWidget(runnerSidebarWidget, RUNNERS_TEAM_ID, ticksUntilEnd, config);
    }

    private void addGameModeInformationToSidebarWidget(@NotNull SidebarWidget sidebarWidget, @NotNull String teamId, long ticksUntilEnd, @NotNull StevesManhuntMiniGameConfig config) {
        sidebarWidget.addLines(Text.translatable(String.format("%s.your_role", MOD_ID)).append(" ").append(TeamUtil.getTeamNamePrefixStyled(teamId)).append("."));
        sidebarWidget.addLines(Text.translatable(String.format("%s.time_remaining", MOD_ID)).append(": ").append(this.getText(ticksUntilEnd)));
        if (!config.getPlayerNameTagVisibilityRule().equals(VisibilityRule.ALWAYS)) {
            sidebarWidget.addLines(Text.translatable(String.format("%s.do_not_disclose_your_role", MOD_ID)).styled(style -> style.withColor(Formatting.GRAY)));
        }
    }

    private Text getText(long ticksUntilEnd) {
        long secondsUntilEnd = ticksUntilEnd / TICKS_PER_SECOND;
        long minutes = secondsUntilEnd / 60;
        long seconds = secondsUntilEnd % 60;
        return Text.literal(String.format("%02d:%02d", minutes, seconds)).styled(style -> style.withColor(Formatting.GRAY));
    }

    private @NotNull Text[] getCompassWithDirectionIndicator(float direction) {
        if ((direction >= 0f && direction < 0.0625f) || (direction >= 0.9375f && direction <= 1f)) {
            return COMPASS_NORTH;
        } else if (direction >= 0.0625f && direction < 0.1875f) {
            return COMPASS_NORTH_EAST;
        } else if (direction >= 0.1875f && direction < 0.3125f) {
            return COMPASS_EAST;
        } else if (direction >= 0.3125f && direction < 0.4375f) {
            return COMPASS_SOUTH_EAST;
        } else if (direction >= 0.4375f && direction < 0.5625f) {
            return COMPASS_SOUTH;
        } else if (direction >= 0.5625f && direction < 0.6875f) {
            return COMPASS_SOUTH_WEST;
        } else if (direction >= 0.6875f && direction < 0.8125f) {
            return COMPASS_WEST;
        } else if (direction >= 0.8125f && direction < 0.9375f) {
            return COMPASS_NORTH_WEST;
        }

        return COMPASS_NORTH;
    }
}
