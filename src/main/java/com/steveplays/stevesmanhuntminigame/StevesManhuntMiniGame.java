package com.steveplays.stevesmanhuntminigame;

import net.fabricmc.api.ModInitializer;
import xyz.nucleoid.plasmid.api.game.GameType;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.steveplays.stevesmanhuntminigame.game.StevesManhuntMiniGameConfig;
import com.steveplays.stevesmanhuntminigame.game.StevesManhuntMiniGameWaiting;

public class StevesManhuntMiniGame implements ModInitializer {
    public static final String MOD_ID = "stevesmanhuntminigame";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static final GameType<StevesManhuntMiniGameConfig> TYPE = GameType.register(
            Identifier.of(MOD_ID, "stevesmanhuntminigame"),
            StevesManhuntMiniGameConfig.CODEC,
            StevesManhuntMiniGameWaiting::open);

    @Override
    public void onInitialize() {
    }
}
