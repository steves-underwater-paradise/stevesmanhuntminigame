package com.steveplays.stevesmanhuntminigame.game.map;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.plasmid.api.game.world.generator.TemplateChunkGenerator;

public class StevesManhuntMiniGameMap {
    private final MapTemplate template;
    private final StevesManhuntMiniGameMapConfig config;
    public BlockPos spawn;

    public StevesManhuntMiniGameMap(MapTemplate template, StevesManhuntMiniGameMapConfig config) {
        this.template = template;
        this.config = config;
    }

    public ChunkGenerator asGenerator(MinecraftServer server) {
        return new TemplateChunkGenerator(server, this.template);
    }
}
