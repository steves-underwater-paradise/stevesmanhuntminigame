package org.example.MODNAME.game;

import net.minecraft.util.math.Vec3d;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameMode;
import org.example.MODNAME.MODCLASS;
import org.example.MODNAME.game.map.MODCLASSMap;

import java.util.Set;

public class MODCLASSSpawnLogic {
    private final GameSpace gameSpace;
    private final MODCLASSMap map;
    private final ServerWorld world;

    public MODCLASSSpawnLogic(GameSpace gameSpace, ServerWorld world, MODCLASSMap map) {
        this.gameSpace = gameSpace;
        this.map = map;
        this.world = world;
    }

    public void resetPlayer(ServerPlayerEntity player, GameMode gameMode) {
        player.changeGameMode(gameMode);
        player.setVelocity(Vec3d.ZERO);
        player.fallDistance = 0.0f;

        player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.NIGHT_VISION,
                20 * 60 * 60,
                1,
                true,
                false
        ));
    }

    public void spawnPlayer(ServerPlayerEntity player) {
        BlockPos pos = this.map.spawn;
        if (pos == null) {
            MODCLASS.LOGGER.error("Cannot spawn player! No spawn is defined in the map!");
            return;
        }

        float radius = 4.5f;
        float x = pos.getX() + MathHelper.nextFloat(player.getRandom(), -radius, radius);
        float z = pos.getZ() + MathHelper.nextFloat(player.getRandom(), -radius, radius);

        player.teleport(this.world, x, pos.getY(), z, Set.of(), 0.0F, 0.0F, true);
    }
}
