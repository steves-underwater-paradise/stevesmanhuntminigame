package com.steveplays.stevesmanhuntminigame.util;

import org.jetbrains.annotations.NotNull;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class PlayerUtil {
	public static void showTitle(@NotNull ServerPlayerEntity serverPlayer, @NotNull Text title, @NotNull Text subtitle, int fadeInTicks, int stayTicks, int fadeOutTicks) {
		serverPlayer.networkHandler.sendPacket(new TitleS2CPacket(title));
		serverPlayer.networkHandler.sendPacket(new TitleFadeS2CPacket(fadeInTicks, stayTicks, fadeOutTicks));
		serverPlayer.networkHandler.sendPacket(new SubtitleS2CPacket(subtitle));
	}

	public static class HungerManagerUtil {
		public static final int SPAWN_FOOD_LEVEL = 20;
		public static final float SPAWN_SATURATION_LEVEL = 5f;
	}
}
