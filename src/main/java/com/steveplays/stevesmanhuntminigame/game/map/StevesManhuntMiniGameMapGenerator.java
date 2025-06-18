package com.steveplays.stevesmanhuntminigame.game.map;

import xyz.nucleoid.map_templates.MapTemplate;
import net.minecraft.util.math.BlockPos;

public class StevesManhuntMiniGameMapGenerator {

    private final StevesManhuntMiniGameMapConfig config;

    public StevesManhuntMiniGameMapGenerator(StevesManhuntMiniGameMapConfig config) {
        this.config = config;
    }

    public StevesManhuntMiniGameMap build() {
        MapTemplate template = MapTemplate.createEmpty();
        StevesManhuntMiniGameMap map = new StevesManhuntMiniGameMap(template, this.config);

        this.buildSpawn(template);
        map.spawn = new BlockPos(0,65,0);

        return map;
    }

    private void buildSpawn(MapTemplate builder) {
        BlockPos min = new BlockPos(-5, 64, -5);
        BlockPos max = new BlockPos(5, 64, 5);

        for (BlockPos pos : BlockPos.iterate(min, max)) {
            builder.setBlockState(pos, this.config.spawnBlock);
        }
    }
}
