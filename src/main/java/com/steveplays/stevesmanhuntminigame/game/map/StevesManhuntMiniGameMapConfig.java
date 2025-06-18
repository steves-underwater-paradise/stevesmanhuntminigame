package com.steveplays.stevesmanhuntminigame.game.map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;

public class StevesManhuntMiniGameMapConfig {
    public static final Codec<StevesManhuntMiniGameMapConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockState.CODEC.fieldOf("spawn_block").forGetter(map -> map.spawnBlock)
    ).apply(instance, StevesManhuntMiniGameMapConfig::new));

    public final BlockState spawnBlock;

    public StevesManhuntMiniGameMapConfig(BlockState spawnBlock) {
        this.spawnBlock = spawnBlock;
    }
}
