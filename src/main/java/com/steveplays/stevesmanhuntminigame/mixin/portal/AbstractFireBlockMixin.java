package com.steveplays.stevesmanhuntminigame.mixin.portal;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionTypes;
import xyz.nucleoid.plasmid.api.game.GameSpaceManager;

@Mixin(AbstractFireBlock.class)
public class AbstractFireBlockMixin {
	// TODO: Only allow nether portal creation in manhunt game worlds
	@Inject(method = "isOverworldOrNether", at = @At(value = "HEAD"), cancellable = true)
	private static void stevesmanhuntminigame$allowNetherPortalCreationInGameWorlds(World world, @NotNull CallbackInfoReturnable<Boolean> cir) {
		var gameSpace = GameSpaceManager.get().byWorld(world);
		if (gameSpace == null) {
			return;
		}

		cir.setReturnValue(world.getDimension().effects().equals(DimensionTypes.THE_NETHER_ID) || world.getDimension().effects().equals(DimensionTypes.OVERWORLD_ID));
	}
}
