package com.steveplays.stevesmanhuntminigame.game;

import net.minecraft.util.math.Vec3d;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.GameSpaceManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameMode;
import java.util.Set;

public class StevesManhuntMiniGameSpawnLogic {
    private final GameSpace gameSpace;
    private final ServerWorld overworld;
    private final int spawnRadius;

    public StevesManhuntMiniGameSpawnLogic(GameSpace gameSpace, ServerWorld overworld, int spawnRadius) {
        this.gameSpace = gameSpace;
        this.overworld = overworld;
        this.spawnRadius = spawnRadius;
    }

    public void resetPlayer(ServerPlayerEntity player, GameMode gameMode) {
        player.changeGameMode(gameMode);
        player.setVelocity(Vec3d.ZERO);
        player.fallDistance = 0f;
    }

    public void spawnPlayer(ServerPlayerEntity player) {
        var spawnPosition = this.overworld.getSpawnPos();
        // TODO: Prevent spawning players in blocks
        float x = spawnPosition.getX() + MathHelper.nextFloat(player.getRandom(), -this.spawnRadius, this.spawnRadius);
        float z = spawnPosition.getZ() + MathHelper.nextFloat(player.getRandom(), -this.spawnRadius, this.spawnRadius);

        player.teleport(this.overworld, x, spawnPosition.getY(), z, Set.of(), this.overworld.getSpawnAngle(), 0f);
    }

    public void respawnPlayer(ServerPlayerEntity player) {
        var server = player.getServer();
        var spawnPointDimension = player.getSpawnPointDimension();
        if (GameSpaceManager.get().byWorld(server.getWorld(spawnPointDimension)) == null) {
            var overworldSpawnPointPosition = this.overworld.getSpawnPos();
            player.teleport(this.overworld, overworldSpawnPointPosition.getX(), overworldSpawnPointPosition.getY(), overworldSpawnPointPosition.getZ(), this.overworld.getSpawnAngle(), 0f);
            return;
        }

        var playerSpawnPointPosition = player.getSpawnPointPosition();
        player.teleport(server.getWorld(spawnPointDimension), playerSpawnPointPosition.getX(), playerSpawnPointPosition.getY(), playerSpawnPointPosition.getZ(), player.getSpawnAngle(), 0f);
    }
}
