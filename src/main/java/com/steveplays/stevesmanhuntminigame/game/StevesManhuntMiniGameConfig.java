package com.steveplays.stevesmanhuntminigame.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.steveplays.stevesmanhuntminigame.game.map.StevesManhuntMiniGameMapConfig;
import xyz.nucleoid.plasmid.api.game.common.config.WaitingLobbyConfig;

public record StevesManhuntMiniGameConfig(WaitingLobbyConfig players, StevesManhuntMiniGameMapConfig mapConfig,
        int timeLimitSecs) {
    public static final MapCodec<StevesManhuntMiniGameConfig> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(
                    WaitingLobbyConfig.CODEC.fieldOf("players").forGetter(StevesManhuntMiniGameConfig::players),
                    StevesManhuntMiniGameMapConfig.CODEC.fieldOf("map")
                            .forGetter(StevesManhuntMiniGameConfig::mapConfig),
                    Codec.INT.fieldOf("time_limit_secs").forGetter(StevesManhuntMiniGameConfig::timeLimitSecs))
            .apply(instance, StevesManhuntMiniGameConfig::new));
}
