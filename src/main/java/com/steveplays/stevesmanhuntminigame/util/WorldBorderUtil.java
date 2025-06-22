package com.steveplays.stevesmanhuntminigame.util;

import org.jetbrains.annotations.NotNull;
import com.steveplays.stevesmanhuntminigame.StevesManhuntMiniGame;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static com.steveplays.stevesmanhuntminigame.StevesManhuntMiniGame.MOD_ID;

public class WorldBorderUtil {
	private static final @NotNull String MULTI_WORLD_BORDERS_NOT_INSTALLED_WARNING =
			"Multi World Borders by PotatoPresident (mod ID: multiworldborders) is not installed. World borders may not be set correctly.\nDownload:\n- GitHub: https://github.com/PotatoPresident/worldborderfixer\n- Modrinth: https://modrinth.com/mod/worldborderfix\n- CurseForge: https://www.curseforge.com/minecraft/mc-mods/world-border-fix";

	public static void WarnInLogIfMultiWorldBordersIsNotInstalled() {
		if (IsMultiWorldBordersInstalled()) {
			return;
		}

		StevesManhuntMiniGame.LOGGER.warn(MULTI_WORLD_BORDERS_NOT_INSTALLED_WARNING);
	}

	public static void WarnInChatToServerAdministratorIfMultiWorldBordersIsNotInstalled(@NotNull ServerPlayerEntity serverPlayer) {
		if (IsMultiWorldBordersInstalled() || !serverPlayer.hasPermissionLevel(2)) {
			return;
		}

		serverPlayer.sendMessage(Text.translatable(String.format("%s.only_server_administrators_can_see_this_message", MOD_ID)).styled(style -> style.withColor(Formatting.GRAY)).append("\n")
				.append(Text.translatable(String.format("%s.multi_world_borders_not_installed_warning", MOD_ID))));
	}

	private static boolean IsMultiWorldBordersInstalled() {
		return FabricLoader.getInstance().isModLoaded("multiworldborders");
	}
}
