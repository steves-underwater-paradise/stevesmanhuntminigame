package com.steveplays.stevesmanhuntminigame.game;

import org.jetbrains.annotations.Nullable;
import com.google.common.base.CaseFormat;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.steveplays.stevesmanhuntminigame.game.map.StevesManhuntMiniGameMapConfig;
import net.minecraft.scoreboard.AbstractTeam.VisibilityRule;
import xyz.nucleoid.plasmid.api.game.common.config.WaitingLobbyConfig;

public class StevesManhuntMiniGameConfig {
    public static final MapCodec<StevesManhuntMiniGameConfig> CODEC =
            RecordCodecBuilder
                    .mapCodec(instance -> instance
                            .group(WaitingLobbyConfig.CODEC.fieldOf("players").forGetter(StevesManhuntMiniGameConfig::getPlayers),
                                    StevesManhuntMiniGameMapConfig.CODEC.fieldOf("map").forGetter(StevesManhuntMiniGameConfig::getMapConfig),
                                    Codec.INT.fieldOf("time_limit_seconds").forGetter(StevesManhuntMiniGameConfig::getTimeLimitSeconds),
                                    Codec.STRING.fieldOf("player_name_tag_visibility").forGetter(StevesManhuntMiniGameConfig::getPlayerNameTagVisibilityRuleRaw),
                                    Codec.FLOAT.fieldOf("hunters_team_ratio_percent").forGetter(StevesManhuntMiniGameConfig::getHuntersTeamRatioPercent))
                            .apply(instance, StevesManhuntMiniGameConfig::new));

    private WaitingLobbyConfig players;
    private StevesManhuntMiniGameMapConfig mapConfig;
    private int timeLimitSeconds;
    private String playerNameTagVisibilityRuleRaw;
    private @Nullable VisibilityRule playerNameTagVisibilityRule;
    private float huntersTeamRatioPercent;

    private StevesManhuntMiniGameConfig(WaitingLobbyConfig players, StevesManhuntMiniGameMapConfig mapConfig, int timeLimitSeconds, String playerNameTagVisibilityRuleRaw,
            float huntersTeamRatioPercent) {
        this.players = players;
        this.mapConfig = mapConfig;
        this.timeLimitSeconds = timeLimitSeconds;
        this.playerNameTagVisibilityRuleRaw = playerNameTagVisibilityRuleRaw;
    }

    public WaitingLobbyConfig getPlayers() {
        return players;
    }

    public StevesManhuntMiniGameMapConfig getMapConfig() {
        return mapConfig;
    }

    public int getTimeLimitSeconds() {
        return timeLimitSeconds;
    }

    private String getPlayerNameTagVisibilityRuleRaw() {
        return playerNameTagVisibilityRuleRaw;
    }

    public VisibilityRule getPlayerNameTagVisibilityRule() {
        if (this.playerNameTagVisibilityRule == null) {
            this.playerNameTagVisibilityRule = VisibilityRule.getRule(CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, this.getPlayerNameTagVisibilityRuleRaw()));
        }

        return this.playerNameTagVisibilityRule;
    }

    public float getHuntersTeamRatioPercent() {
        return huntersTeamRatioPercent;
    }
}
