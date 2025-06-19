package com.steveplays.stevesmanhuntminigame.game;

import net.minecraft.util.math.Vec3d;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameMode;
import com.steveplays.stevesmanhuntminigame.StevesManhuntMiniGame;
import com.steveplays.stevesmanhuntminigame.game.map.StevesManhuntMiniGameMap;

import java.util.Set;

public class StevesManhuntMiniGameSpawnLogic {
    private final GameSpace gameSpace;
    private final StevesManhuntMiniGameMap map;
    private final ServerWorld world;

    public StevesManhuntMiniGameSpawnLogic(GameSpace gameSpace, ServerWorld world, StevesManhuntMiniGameMap map) {
        this.gameSpace = gameSpace;
        this.map = map;
        this.world = world;
    }

    public void resetPlayer(ServerPlayerEntity player, GameMode gameMode) {
        player.changeGameMode(gameMode);
        player.setVelocity(Vec3d.ZERO);
        player.fallDistance = 0f;

        player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 20 * 60 * 60, 1, true, false));
    }

    public void spawnPlayer(ServerPlayerEntity player) {
        BlockPos pos = this.map.spawn;
        if (pos == null) {
            StevesManhuntMiniGame.LOGGER.error("Cannot spawn player. No spawn is defined in the map.");
            return;
        }

        float radius = 4.5f;
        float x = pos.getX() + MathHelper.nextFloat(player.getRandom(), -radius, radius);
        float z = pos.getZ() + MathHelper.nextFloat(player.getRandom(), -radius, radius);

        player.teleport(this.world, x, pos.getY(), z, Set.of(), 0f, 0f);
    }
}
