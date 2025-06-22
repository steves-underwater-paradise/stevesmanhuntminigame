package com.steveplays.stevesmanhuntminigame.game;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.plasmid.api.game.*;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionTypes;
import com.steveplays.stevesmanhuntminigame.util.WorldBorderUtil;
import xyz.nucleoid.plasmid.api.game.common.GameWaitingLobby;
import xyz.nucleoid.plasmid.api.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.api.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.api.game.player.JoinOffer;
import xyz.nucleoid.stimuli.event.player.PlayerDamageEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

import static com.steveplays.stevesmanhuntminigame.util.WeatherUtil.MIN_TIME_UNTIL_WEATHER_CHANGE_TICKS;
import com.steveplays.stevesmanhuntminigame.util.RuntimeWorldWithSpecialSpawners;
import static com.steveplays.stevesmanhuntminigame.util.WeatherUtil.MAX_TIME_UNTIL_WEATHER_CHANGE_TICKS;

public class StevesManhuntMiniGameWaiting {
    private final GameSpace gameSpace;
    private final StevesManhuntMiniGameConfig config;
    private final StevesManhuntMiniGameSpawnLogic spawnLogic;
    private final ServerWorld overworld;
    private final ServerWorld nether;
    private final ServerWorld end;

    private StevesManhuntMiniGameWaiting(GameSpace gameSpace, ServerWorld overworld, ServerWorld nether, ServerWorld end, StevesManhuntMiniGameConfig config) {
        this.gameSpace = gameSpace;
        this.config = config;
        this.overworld = overworld;
        this.nether = nether;
        this.end = end;
        this.spawnLogic = new StevesManhuntMiniGameSpawnLogic(overworld, config.mapConfig().spawnRadius);
    }

    @SuppressWarnings("deprecation")
    public static GameOpenProcedure open(GameOpenContext<StevesManhuntMiniGameConfig> context) {
        return context.open(game -> {
            var gameSpace = game.getGameSpace();
            var server = gameSpace.getServer();
            var random = Random.create();
            var seed = random.nextLong();
            var overworld = gameSpace.getWorlds()
                    .add(new RuntimeWorldConfig().setWorldConstructor(RuntimeWorldWithSpecialSpawners::createWorld).setSeed(seed).setGameRule(GameRules.DO_MOB_SPAWNING, true)
                            .setGenerator(server.getOverworld().getChunkManager().getChunkGenerator()).setTimeOfDay(0).setShouldTickTime(true).setGameRule(GameRules.DO_WEATHER_CYCLE, true)
                            .setSunny(random.nextBetween(MIN_TIME_UNTIL_WEATHER_CHANGE_TICKS, MAX_TIME_UNTIL_WEATHER_CHANGE_TICKS)));
            var nether = gameSpace.getWorlds().add(new RuntimeWorldConfig().setSeed(seed).setGameRule(GameRules.DO_MOB_SPAWNING, true)
                    .setGenerator(server.getWorld(World.NETHER).getChunkManager().getChunkGenerator()).setDimensionType(DimensionTypes.THE_NETHER));
            var end = gameSpace.getWorlds().add(new RuntimeWorldConfig().setSeed(seed).setGameRule(GameRules.DO_MOB_SPAWNING, true)
                    .setGenerator(server.getWorld(World.END).getChunkManager().getChunkGenerator()).setDimensionType(DimensionTypes.THE_END));
            end.setEnderDragonFight(new EnderDragonFight(end, seed, EnderDragonFight.Data.DEFAULT));

            WorldBorderUtil.WarnInLogIfMultiWorldBordersIsNotInstalled();

            var config = context.config();
            overworld.getWorldBorder().setSize(config.mapConfig().size);
            nether.getWorldBorder().setSize(config.mapConfig().size);
            end.getWorldBorder().setSize(config.mapConfig().size);

            StevesManhuntMiniGameWaiting waiting = new StevesManhuntMiniGameWaiting(gameSpace, overworld, nether, end, config);
            GameWaitingLobby.addTo(game, config.players());
            game.listen(GameActivityEvents.REQUEST_START, waiting::requestStart);
            game.listen(GamePlayerEvents.ADD, waiting::addPlayer);
            game.listen(GamePlayerEvents.OFFER, JoinOffer::accept);
            game.listen(GamePlayerEvents.ACCEPT, joinAcceptor -> joinAcceptor.teleport(overworld, Vec3d.ZERO));
            game.listen(PlayerDamageEvent.EVENT, waiting::onPlayerDamage);
            game.listen(PlayerDeathEvent.EVENT, waiting::onPlayerDeath);
        });
    }

    private GameResult requestStart() {
        StevesManhuntMiniGameActive.open(this.gameSpace, this.overworld, this.nether, this.end, this.config);
        return GameResult.ok();
    }

    private void addPlayer(ServerPlayerEntity serverPlayer) {
        this.spawnPlayer(serverPlayer);
        WorldBorderUtil.WarnInChatToServerAdministratorIfMultiWorldBordersIsNotInstalled(serverPlayer);
    }

    private ActionResult onPlayerDeath(ServerPlayerEntity serverPlayer, DamageSource source) {
        serverPlayer.setHealth(20.0f);
        this.spawnPlayer(serverPlayer);
        return ActionResult.FAIL;
    }

    private ActionResult onPlayerDamage(ServerPlayerEntity serverPlayer, DamageSource damageSource, float damage) {
        return ActionResult.FAIL;
    }

    private void spawnPlayer(ServerPlayerEntity serverPlayer) {
        this.spawnLogic.resetPlayer(serverPlayer, GameMode.ADVENTURE);
        this.spawnLogic.spawnPlayer(serverPlayer);
    }
}
