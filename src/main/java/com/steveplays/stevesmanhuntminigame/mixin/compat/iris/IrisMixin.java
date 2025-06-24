package com.steveplays.stevesmanhuntminigame.mixin.compat.iris;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.shaderpack.DimensionId;
import net.irisshaders.iris.shaderpack.materialmap.NamespacedId;
import net.minecraft.client.MinecraftClient;
import net.minecraft.world.dimension.DimensionTypes;
import xyz.nucleoid.fantasy.Fantasy;

@Mixin(value = Iris.class, remap = false)
public abstract class IrisMixin {
	@Inject(method = "getCurrentDimension", at = @At(value = "HEAD"), cancellable = true)
	private static void stevesmanhuntminigame$returnCorrectDimensionInMiniGameWorlds(@NotNull CallbackInfoReturnable<NamespacedId> cir) {
		@Nullable var clientWorld = MinecraftClient.getInstance().world;
		if (clientWorld == null || !clientWorld.getRegistryKey().getValue().getNamespace().equals(Fantasy.ID)) {
			return;
		}

		var gameSpaceClientWorldDimensionTypeIdentifier = clientWorld.getDimension().effects();
		if (gameSpaceClientWorldDimensionTypeIdentifier.equals(DimensionTypes.OVERWORLD_ID)) {
			cir.setReturnValue(DimensionId.OVERWORLD);
		} else if (gameSpaceClientWorldDimensionTypeIdentifier.equals(DimensionTypes.THE_NETHER_ID)) {
			cir.setReturnValue(DimensionId.NETHER);
		} else if (gameSpaceClientWorldDimensionTypeIdentifier.equals(DimensionTypes.THE_END_ID)) {
			cir.setReturnValue(DimensionId.END);
		}
	}
}
