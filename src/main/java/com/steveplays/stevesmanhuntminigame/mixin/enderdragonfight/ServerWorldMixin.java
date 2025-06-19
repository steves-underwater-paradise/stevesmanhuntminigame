package com.steveplays.stevesmanhuntminigame.mixin.enderdragonfight;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.steveplays.stevesmanhuntminigame.StevesManhuntMiniGame;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.util.math.random.RandomSequencesState;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.spawner.SpecialSpawner;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.plasmid.api.game.GameSpaceManager;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World {
	@Shadow private @Nullable EnderDragonFight enderDragonFight;

	protected ServerWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, DynamicRegistryManager registryManager, RegistryEntry<DimensionType> dimensionEntry,
			Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long biomeAccess, int maxChainedNeighborUpdates) {
		super(properties, registryRef, registryManager, dimensionEntry, profiler, isClient, debugWorld, biomeAccess, maxChainedNeighborUpdates);
	}

	// TODO: Only initialize the Ender Dragon fight if this world is part of a game space
	@Inject(method = "<init>", at = @At(value = "TAIL"))
	private void stevesmanhuntminigame$allowEnderDragonFightInGameWorlds(MinecraftServer server, Executor workerExecutor, LevelStorage.Session session, ServerWorldProperties properties,
			RegistryKey<World> worldKey, DimensionOptions dimensionOptions, WorldGenerationProgressListener worldGenerationProgressListener, boolean debugWorld, long seed,
			List<SpecialSpawner> spawners, boolean shouldTickTime, RandomSequencesState randomSequencesState, CallbackInfo ci, @Local(ordinal = 1) long saveSeed) {
		if (!this.getDimension().effects().equals(DimensionTypes.THE_END_ID)) {
			return;
		}

		this.enderDragonFight = new EnderDragonFight((ServerWorld) (Object) this, saveSeed, EnderDragonFight.Data.DEFAULT);
	}

	@WrapOperation(method = "saveLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/SaveProperties;setDragonFight(Lnet/minecraft/entity/boss/dragon/EnderDragonFight$Data;)V"))
	private void stevesmanhuntminigame$preventSavingEnderDragonFightInGameWorlds(SaveProperties instance, EnderDragonFight.Data data, Operation<Void> original) {
		var gameSpace = GameSpaceManager.get().byWorld(this);
		if (gameSpace == null) {
			original.call(instance, data);
		}
	}
}
