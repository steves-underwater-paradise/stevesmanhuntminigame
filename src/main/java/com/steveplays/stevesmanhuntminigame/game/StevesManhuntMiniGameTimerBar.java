package com.steveplays.stevesmanhuntminigame.game;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import xyz.nucleoid.plasmid.api.game.common.GlobalWidgets;
import xyz.nucleoid.plasmid.api.game.common.widget.SidebarWidget;

import static com.steveplays.stevesmanhuntminigame.util.TickUtil.TICKS_PER_SECOND;

public final class StevesManhuntMiniGameTimerBar {
    private final SidebarWidget sidebarWidget;

    public StevesManhuntMiniGameTimerBar(GlobalWidgets widgets) {
        // TODO: Display sidebar with different contents based on the player's team
        // TODO: Replace literal text with translatable text
        this.sidebarWidget = widgets.addSidebar(Text.literal("Manhunt"));
        sidebarWidget.addLines(Text.literal("Waiting for the game to start..."));
    }

    public void update(long ticksUntilEnd, long totalTicksUntilEnd) {
        if (ticksUntilEnd % TICKS_PER_SECOND != 0) {
            return;
        }

        // TODO: Replace literal text with translatable text
        sidebarWidget.clearLines();
        sidebarWidget.addLines(Text.literal("Time remaining: ").append(this.getText(ticksUntilEnd)));
    }

    private Text getText(long ticksUntilEnd) {
        long secondsUntilEnd = ticksUntilEnd / TICKS_PER_SECOND;

        long minutes = secondsUntilEnd / 60;
        long seconds = secondsUntilEnd % 60;
        String time = String.format("%02d:%02d left", minutes, seconds);

        return Text.literal(time).styled(style -> style.withColor(Formatting.GRAY));
    }
}
