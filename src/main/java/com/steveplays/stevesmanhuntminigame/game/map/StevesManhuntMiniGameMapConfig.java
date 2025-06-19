package com.steveplays.stevesmanhuntminigame.game.map;

import org.jetbrains.annotations.NotNull;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;

public class StevesManhuntMiniGameMapConfig {
    public static final Codec<StevesManhuntMiniGameMapConfig> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(Codec.INT.fieldOf("size").forGetter(map -> map.size), BlockState.CODEC.fieldOf("spawn_block").forGetter(map -> map.spawnBlock))
                    .apply(instance, StevesManhuntMiniGameMapConfig::new));

    public final int size;
    public final @NotNull BlockState spawnBlock;

    public StevesManhuntMiniGameMapConfig(int size, @NotNull BlockState spawnBlock) {
        this.size = size;
        this.spawnBlock = spawnBlock;
    }
}
