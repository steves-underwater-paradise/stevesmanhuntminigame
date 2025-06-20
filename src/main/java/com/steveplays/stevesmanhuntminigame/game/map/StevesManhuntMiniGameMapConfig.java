package com.steveplays.stevesmanhuntminigame.game.map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.dynamic.Codecs;

public class StevesManhuntMiniGameMapConfig {
    public static final Codec<StevesManhuntMiniGameMapConfig> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(Codecs.rangedInt(3500, Integer.MAX_VALUE).fieldOf("size").forGetter(map -> map.size), Codec.INT.fieldOf("spawn_radius").forGetter(map -> map.spawnRadius))
                    .apply(instance, StevesManhuntMiniGameMapConfig::new));

    public final int size;
    public final int spawnRadius;

    public StevesManhuntMiniGameMapConfig(int size, int spawnRadius) {
        this.size = size;
        this.spawnRadius = spawnRadius;
    }
}
