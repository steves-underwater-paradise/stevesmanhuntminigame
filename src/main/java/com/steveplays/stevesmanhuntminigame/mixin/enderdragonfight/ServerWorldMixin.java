package com.steveplays.stevesmanhuntminigame.mixin.enderdragonfight;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import net.minecraft.server.world.ServerWorld;
import xyz.nucleoid.plasmid.api.game.GameSpaceManager;

import java.util.function.Supplier;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World {
	@Shadow private @Nullable EnderDragonFight enderDragonFight;

	protected ServerWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, DynamicRegistryManager registryManager, RegistryEntry<DimensionType> dimensionEntry,
			Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long biomeAccess, int maxChainedNeighborUpdates) {
		super(properties, registryRef, registryManager, dimensionEntry, profiler, isClient, debugWorld, biomeAccess, maxChainedNeighborUpdates);
	}

	@WrapOperation(method = "saveLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/SaveProperties;setDragonFight(Lnet/minecraft/entity/boss/dragon/EnderDragonFight$Data;)V"))
	private void stevesmanhuntminigame$preventSavingEnderDragonFightInGameWorlds(SaveProperties instance, EnderDragonFight.Data data, Operation<Void> original) {
		var gameSpace = GameSpaceManager.get().byWorld(this);
		if (gameSpace == null) {
			original.call(instance, data);
		}
	}
}
