package com.steveplays.stevesmanhuntminigame.util;

import org.jetbrains.annotations.NotNull;
import com.steveplays.stevesmanhuntminigame.StevesManhuntMiniGame;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class WorldBorderUtil {
	private static final @NotNull String MULTI_WORLD_BORDERS_NOT_INSTALLED_WARNING =
			"Multi World Borders by PotatoPresident (mod ID: multiworldborders) is not installed. World borders may not be set correctly.\nDownload:\n- GitHub: https://github.com/PotatoPresident/worldborderfixer\n- Modrinth: https://modrinth.com/mod/worldborderfix\n- CurseForge: https://www.curseforge.com/minecraft/mc-mods/world-border-fix";

	public static void WarnInLogIfMultiWorldBordersIsNotInstalled(@NotNull ServerWorld serverWorld) {
		if (IsMultiWorldBordersInstalled()) {
			return;
		}

		StevesManhuntMiniGame.LOGGER.warn(MULTI_WORLD_BORDERS_NOT_INSTALLED_WARNING);
	}

	public static void WarnInChatToServerAdministratorIfMultiWorldBordersIsNotInstalled(@NotNull ServerPlayerEntity serverPlayer) {
		if (IsMultiWorldBordersInstalled() || !serverPlayer.hasPermissionLevel(2)) {
			return;
		}

		// TODO: Replace literal text with translatable text
		serverPlayer.sendMessage(
				Text.literal("Only server administrators can see this message:\n").styled(style -> style.withColor(Formatting.GRAY)).append(Text.literal(MULTI_WORLD_BORDERS_NOT_INSTALLED_WARNING)));
	}

	private static boolean IsMultiWorldBordersInstalled() {
		return FabricLoader.getInstance().isModLoaded("multiworldborders");
	}
}
