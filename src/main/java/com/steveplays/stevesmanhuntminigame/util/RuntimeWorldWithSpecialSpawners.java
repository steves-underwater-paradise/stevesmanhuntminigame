package com.steveplays.stevesmanhuntminigame.util;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import com.google.common.collect.ImmutableList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import net.minecraft.village.ZombieSiegeManager;
import net.minecraft.world.WanderingTraderManager;
import net.minecraft.world.World;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.spawner.CatSpawner;
import net.minecraft.world.spawner.PatrolSpawner;
import net.minecraft.world.spawner.PhantomSpawner;
import net.minecraft.world.spawner.SpecialSpawner;
import xyz.nucleoid.fantasy.RuntimeWorld;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldProperties;
import xyz.nucleoid.fantasy.mixin.MinecraftServerAccess;
import xyz.nucleoid.fantasy.util.VoidWorldProgressListener;

public class RuntimeWorldWithSpecialSpawners extends RuntimeWorld {
	protected RuntimeWorldWithSpecialSpawners(MinecraftServer server, ServerWorldProperties serverWorldProperties, RegistryKey<World> registryKey, RuntimeWorldConfig config,
			List<SpecialSpawner> specialSpawners, Style style) {
		super(server, Util.getMainWorkerExecutor(), ((MinecraftServerAccess) server).getSession(), serverWorldProperties, registryKey, config.createDimensionOptions(server),
				VoidWorldProgressListener.INSTANCE, false, BiomeAccess.hashSeed(config.getSeed()), specialSpawners, config.shouldTickTime(), null, style);
	}

	public static RuntimeWorld createWorld(MinecraftServer server, RegistryKey<World> registryKey, RuntimeWorldConfig config, Style style) {
		@NotNull var serverWorldProperties = new RuntimeWorldProperties(server.getSaveProperties(), config);
		return new RuntimeWorldWithSpecialSpawners(server, serverWorldProperties, registryKey, config,
				ImmutableList.of(new PhantomSpawner(), new PatrolSpawner(), new CatSpawner(), new ZombieSiegeManager(), new WanderingTraderManager(serverWorldProperties)), style);
	}
}
