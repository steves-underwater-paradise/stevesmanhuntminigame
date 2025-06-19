package com.steveplays.stevesmanhuntminigame.mixin.portal;

import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.TeleportTarget;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.EndPortalBlock;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionTypes;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.plasmid.api.game.GameSpaceManager;

@Mixin(EndPortalBlock.class)
public class EndPortalBlockMixin {
	@ModifyArg(method = "createTeleportTarget",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getWorld(Lnet/minecraft/registry/RegistryKey;)Lnet/minecraft/server/world/ServerWorld;"), index = 0)
	private RegistryKey<World> stevesmanhuntminigame$allowEndPortalCreationInGameWorlds(RegistryKey<World> original, @Local(argsOnly = true) @NotNull ServerWorld serverWorld) {
		var gameSpace = GameSpaceManager.get().byWorld(serverWorld);
		if (gameSpace == null) {
			return original;
		}

		ServerWorld overworldServerWorld = null;
		ServerWorld endServerWorld = null;
		for (ServerWorld gameSpaceServerWorld : gameSpace.getWorlds()) {
			var gameSpaceServerWorldDimensionTypeIdentifier = gameSpaceServerWorld.getDimension().effects();
			if (gameSpaceServerWorldDimensionTypeIdentifier.equals(DimensionTypes.OVERWORLD_ID)) {
				overworldServerWorld = gameSpaceServerWorld;
			} else if (gameSpaceServerWorldDimensionTypeIdentifier.equals(DimensionTypes.THE_END_ID)) {
				endServerWorld = gameSpaceServerWorld;
			}
		}
		if (overworldServerWorld == null || endServerWorld == null) {
			return original;
		}

		return serverWorld.getDimension().effects().equals(DimensionTypes.THE_END_ID) ? overworldServerWorld.getRegistryKey() : endServerWorld.getRegistryKey();
	}

	@Inject(method = "createTeleportTarget",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;getSpawnPos()Lnet/minecraft/util/math/BlockPos;", shift = At.Shift.BY, by = -7))
	private void stevesmanhuntminigame$allowEndPortalCreationInGameWorlds(ServerWorld serverWorld, Entity entity, BlockPos blockPosition, @NotNull CallbackInfoReturnable<TeleportTarget> cir,
			@Local(ordinal = 1) @NotNull ServerWorld otherServerWorld, @Local @NotNull LocalBooleanRef isEnd) {
		isEnd.set(otherServerWorld.getDimension().effects().equals(DimensionTypes.THE_END_ID));
	}
}
