package com.steveplays.stevesmanhuntminigame.game;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import xyz.nucleoid.plasmid.api.game.common.GlobalWidgets;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamKey;
import xyz.nucleoid.plasmid.api.game.common.team.TeamManager;
import xyz.nucleoid.plasmid.api.game.common.widget.SidebarWidget;

import static com.steveplays.stevesmanhuntminigame.util.TickUtil.TICKS_PER_SECOND;
import org.jetbrains.annotations.NotNull;

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
public final class StevesManhuntMiniGameSideBar {
    private static final @NotNull Text[] COMPASS = new Text[] {Text.literal("    ███"), Text.literal("  █      █"), Text.literal("█          █"), Text.literal("█    █    █"),
            Text.literal("█          █"), Text.literal("  █      █"), Text.literal("    ███")};
    private static final @NotNull Text[] COMPASS_NORTH = new Text[] {Text.literal("    ███"), Text.literal("  █  █  █"), Text.literal("█    █    █"), Text.literal("█    █    █"),
            Text.literal("█    █    █"), Text.literal("  █      █"), Text.literal("    ███")};
    private static final @NotNull Text[] COMPASS_NORTH_EAST = new Text[] {Text.literal("    ███"), Text.literal("  █      █"), Text.literal("█      █  █"), Text.literal("█    █    █"),
            Text.literal("█  █      █"), Text.literal("  █      █"), Text.literal("    ███")};
    private static final @NotNull Text[] COMPASS_EAST = new Text[] {Text.literal("    ███"), Text.literal("  █      █"), Text.literal("█          █"), Text.literal("█  ████"),
            Text.literal("█          █"), Text.literal("  █      █"), Text.literal("    ███")};
    private static final @NotNull Text[] COMPASS_SOUTH_EAST = new Text[] {Text.literal("    ███"), Text.literal("  █      █"), Text.literal("█  █      █"), Text.literal("█    █    █"),
            Text.literal("█      █  █"), Text.literal("  █      █"), Text.literal("    ███")};
    private static final @NotNull Text[] COMPASS_SOUTH = new Text[] {Text.literal("    ███"), Text.literal("  █      █"), Text.literal("█    █    █"), Text.literal("█    █    █"),
            Text.literal("█    █    █"), Text.literal("  █  █  █"), Text.literal("    ███")};
    private static final @NotNull Text[] COMPASS_SOUTH_WEST = new Text[] {Text.literal("    ███"), Text.literal("  █      █"), Text.literal("█      █  █"), Text.literal("█    █    █"),
            Text.literal("█  █      █"), Text.literal("  █      █"), Text.literal("    ███")};
    private static final @NotNull Text[] COMPASS_WEST = new Text[] {Text.literal("    ███"), Text.literal("  █      █"), Text.literal("█          █"), Text.literal("█████  █"),
            Text.literal("█          █"), Text.literal("  █      █"), Text.literal("    ███")};
    private static final @NotNull Text[] COMPASS_NORTH_WEST = new Text[] {Text.literal("    ███"), Text.literal("  █      █"), Text.literal("█  █      █"), Text.literal("█    █    █"),
            Text.literal("█      █  █"), Text.literal("  █      █"), Text.literal("    ███")};

    private final SidebarWidget waitingSidebarWidget;

    private SidebarWidget hunterSidebarWidget;
    private SidebarWidget runnerSidebarWidget;

    public StevesManhuntMiniGameSideBar(GlobalWidgets widgets) {
        this.waitingSidebarWidget = widgets.addSidebar(Text.literal("Manhunt"));
        waitingSidebarWidget.addLines(Text.literal("Waiting for the game to start..."));
    }

    public void onOpen(GlobalWidgets widgets, TeamManager teamManager, GameTeamKey hunterTeamKey, GameTeamKey runnerTeamKey) {
        // TODO: Replace literal text with translatable text
        this.hunterSidebarWidget = widgets.addSidebar(Text.literal("Manhunt"), participant -> teamManager.teamFor(participant).equals(hunterTeamKey));
        this.runnerSidebarWidget = widgets.addSidebar(Text.literal("Manhunt"), participant -> teamManager.teamFor(participant).equals(runnerTeamKey));
        // teamManager.getteam
    }

    public void update(long ticksUntilEnd, long totalTicksUntilEnd) {
        if (ticksUntilEnd % TICKS_PER_SECOND != 0) {
            return;
        }

        // TODO: Replace literal text with translatable text
        hunterSidebarWidget.clearLines();
        hunterSidebarWidget.addLines(Text.literal("Your team: Hunter"));
        hunterSidebarWidget.addLines(Text.literal("Time remaining: ").append(this.getText(ticksUntilEnd)));
        hunterSidebarWidget.addLines(Text.literal(""), Text.literal("Closest runner:"), Text.literal("- Distance: TODO m"), Text.literal("- Direction:"));
        hunterSidebarWidget.addLines(COMPASS_SOUTH);

        runnerSidebarWidget.clearLines();
        runnerSidebarWidget.addLines(Text.literal("Your team: Runner"));
        runnerSidebarWidget.addLines(Text.literal("Time remaining: ").append(this.getText(ticksUntilEnd)));
    }

    private Text getText(long ticksUntilEnd) {
        long secondsUntilEnd = ticksUntilEnd / TICKS_PER_SECOND;

        long minutes = secondsUntilEnd / 60;
        long seconds = secondsUntilEnd % 60;
        String time = String.format("%02d:%02d left", minutes, seconds);

        return Text.literal(time).styled(style -> style.withColor(Formatting.GRAY));
    }

    private @NotNull Text[] getCompassWithDirectionIndicator(float direction) {
        if (direction >= -0.0625f && direction < 0.0625f) {
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
