package com.steveplays.stevesmanhuntminigame.game;

import net.minecraft.util.math.Vec3d;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.GameSpaceManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameMode;
import net.minecraft.world.Heightmap;
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

        // TODO: Reset player hunger and saturation
        // TODO: Remove player effects
    }

    public void spawnPlayer(ServerPlayerEntity player) {
        var overworldWorldBorder = this.overworld.getWorldBorder();
        var playerRandom = player.getRandom();
        var spawnPosition = BlockPos.ofFloored(overworldWorldBorder.getCenterX() + MathHelper.nextFloat(playerRandom, -this.spawnRadius, this.spawnRadius), 0.0,
                overworldWorldBorder.getCenterZ() + MathHelper.nextFloat(playerRandom, -this.spawnRadius, this.spawnRadius));
        spawnPosition = this.overworld.getTopPosition(Heightmap.Type.MOTION_BLOCKING, spawnPosition);
        player.teleport(this.overworld, spawnPosition.getX(), spawnPosition.getY(), spawnPosition.getZ(), Set.of(), this.overworld.getSpawnAngle(), 0f);
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
