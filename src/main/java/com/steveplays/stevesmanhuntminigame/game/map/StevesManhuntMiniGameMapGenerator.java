package com.steveplays.stevesmanhuntminigame.game.map;

import xyz.nucleoid.map_templates.MapTemplate;
import org.jetbrains.annotations.NotNull;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.border.WorldBorder;

public class StevesManhuntMiniGameMapGenerator {
    private final @NotNull StevesManhuntMiniGameMapConfig config;

    public StevesManhuntMiniGameMapGenerator(@NotNull StevesManhuntMiniGameMapConfig config) {
        this.config = config;
    }

    public @NotNull StevesManhuntMiniGameMap build() {
        MapTemplate template = MapTemplate.createEmpty();
        StevesManhuntMiniGameMap map = new StevesManhuntMiniGameMap(template, this.config);
        this.buildSpawn(template);
        map.spawn = new BlockPos(0, 65, 0);

        return map;
    }

    private void buildSpawn(@NotNull MapTemplate builder) {
        BlockPos min = new BlockPos(-5, 64, -5);
        BlockPos max = new BlockPos(5, 64, 5);

        for (BlockPos pos : BlockPos.iterate(min, max)) {
            builder.setBlockState(pos, this.config.spawnBlock);
        }
    }
}
