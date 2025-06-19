package com.steveplays.stevesmanhuntminigame.mixin.portal;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionTypes;
import xyz.nucleoid.plasmid.api.game.GameSpaceManager;

@Mixin(NetherPortalBlock.class)
public class NetherPortalBlockMixin {
	@ModifyArg(method = "createTeleportTarget",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getWorld(Lnet/minecraft/registry/RegistryKey;)Lnet/minecraft/server/world/ServerWorld;"), index = 0)
	private RegistryKey<World> stevesmanhuntminigame$allowNetherPortalCreationInGameWorlds(RegistryKey<World> original, @Local(argsOnly = true) @NotNull ServerWorld serverWorld) {
		var gameSpace = GameSpaceManager.get().byWorld(serverWorld);
		if (gameSpace == null) {
			return original;
		}

		ServerWorld overworldServerWorld = null;
		ServerWorld netherServerWorld = null;
		for (ServerWorld gameSpaceServerWorld : gameSpace.getWorlds()) {
			var gameSpaceServerWorldDimensionTypeIdentifier = gameSpaceServerWorld.getDimension().effects();
			if (gameSpaceServerWorldDimensionTypeIdentifier.equals(DimensionTypes.OVERWORLD_ID)) {
				overworldServerWorld = gameSpaceServerWorld;
			} else if (gameSpaceServerWorldDimensionTypeIdentifier.equals(DimensionTypes.THE_NETHER_ID)) {
				netherServerWorld = gameSpaceServerWorld;
			}
		}
		if (overworldServerWorld == null || netherServerWorld == null) {
			return original;
		}

		return serverWorld.getDimension().effects().equals(DimensionTypes.THE_NETHER_ID) ? overworldServerWorld.getRegistryKey() : netherServerWorld.getRegistryKey();
	}

	@ModifyArg(method = "createTeleportTarget", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/block/NetherPortalBlock;getOrCreateExitPortalTarget(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;ZLnet/minecraft/world/border/WorldBorder;)Lnet/minecraft/world/TeleportTarget;"))
	private boolean stevesmanhuntminigame$allowNetherPortalCreationInGameWorlds(boolean original, @Local(argsOnly = true) @NotNull ServerWorld serverWorld) {
		var gameSpace = GameSpaceManager.get().byWorld(serverWorld);
		if (gameSpace == null) {
			return original;
		}

		return serverWorld.getDimension().effects().equals(DimensionTypes.THE_NETHER_ID);
	}
}
