package com.steveplays.stevesmanhuntminigame.mixin;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import com.google.common.collect.ImmutableMap;
import net.fabricmc.loader.api.FabricLoader;

public class StevesManhuntMiniGameMixinConfigPlugin implements IMixinConfigPlugin {
	private static final @NotNull Supplier<Boolean> TRUE = () -> true;
	private static final @NotNull String MOD_MIXIN_PACKAGE = "com.steveplays.stevesmanhuntminigame.mixin";
	private static final @NotNull String IRIS_MOD_ID = "iris";
	private static final @NotNull Map<String, Supplier<Boolean>> CONDITIONS =
			ImmutableMap.of(String.format("%s.compat.iris.IrisMixin", MOD_MIXIN_PACKAGE), () -> FabricLoader.getInstance().isModLoaded(IRIS_MOD_ID));

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		return CONDITIONS.getOrDefault(mixinClassName, TRUE).get();
	}

	@Override
	public void onLoad(String mixinPackage) {}

	@Override
	public @Nullable String getRefMapperConfig() {
		return null;
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

	@Override
	public @Nullable List<String> getMixins() {
		return null;
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
