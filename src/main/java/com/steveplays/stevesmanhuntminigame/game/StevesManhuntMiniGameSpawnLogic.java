package com.steveplays.stevesmanhuntminigame.game;

import net.minecraft.util.math.Vec3d;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameMode;

import java.util.Set;

public class StevesManhuntMiniGameSpawnLogic {
    private final GameSpace gameSpace;
    private final ServerWorld overworld;

    public StevesManhuntMiniGameSpawnLogic(GameSpace gameSpace, ServerWorld overworld) {
        this.gameSpace = gameSpace;
        this.overworld = overworld;
    }

    public void resetPlayer(ServerPlayerEntity player, GameMode gameMode) {
        player.changeGameMode(gameMode);
        player.setVelocity(Vec3d.ZERO);
        player.fallDistance = 0f;
    }

    public void spawnPlayer(ServerPlayerEntity player) {
        var spawnPosition = this.overworld.getSpawnPos();
        // TODO: Prevent spawning players in blocks
        float spawnRadius = 4.5f;
        float x = spawnPosition.getX() + MathHelper.nextFloat(player.getRandom(), -spawnRadius, spawnRadius);
        float z = spawnPosition.getZ() + MathHelper.nextFloat(player.getRandom(), -spawnRadius, spawnRadius);

        player.teleport(this.overworld, x, spawnPosition.getY(), z, Set.of(), 0f, 0f);
    }
}
