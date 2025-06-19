package com.steveplays.stevesmanhuntminigame.game;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.plasmid.api.game.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;
import com.steveplays.stevesmanhuntminigame.game.map.StevesManhuntMiniGameMap;
import com.steveplays.stevesmanhuntminigame.game.map.StevesManhuntMiniGameMapGenerator;
import com.steveplays.stevesmanhuntminigame.util.WorldBorderUtil;
import xyz.nucleoid.plasmid.api.game.common.GameWaitingLobby;
import xyz.nucleoid.plasmid.api.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.api.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.api.game.player.JoinOffer;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

import static com.steveplays.stevesmanhuntminigame.util.WeatherUtil.MIN_TIME_UNTIL_WEATHER_CHANGE_TICKS;
import static com.steveplays.stevesmanhuntminigame.util.WeatherUtil.MAX_TIME_UNTIL_WEATHER_CHANGE_TICKS;

public class StevesManhuntMiniGameWaiting {
    private final GameSpace gameSpace;
    private final StevesManhuntMiniGameMap map;
    private final StevesManhuntMiniGameConfig config;
    private final StevesManhuntMiniGameSpawnLogic spawnLogic;
    private final ServerWorld world;

    private StevesManhuntMiniGameWaiting(GameSpace gameSpace, ServerWorld world, StevesManhuntMiniGameMap map, StevesManhuntMiniGameConfig config) {
        this.gameSpace = gameSpace;
        this.map = map;
        this.config = config;
        this.world = world;
        this.spawnLogic = new StevesManhuntMiniGameSpawnLogic(gameSpace, world, map);
    }

    public static GameOpenProcedure open(GameOpenContext<StevesManhuntMiniGameConfig> context) {
        StevesManhuntMiniGameConfig config = context.config();
        StevesManhuntMiniGameMapGenerator generator = new StevesManhuntMiniGameMapGenerator(config.mapConfig());
        StevesManhuntMiniGameMap map = generator.build();

        var random = Random.create();
        RuntimeWorldConfig worldConfig = new RuntimeWorldConfig().setGenerator(map.asGenerator(context.server())).setTimeOfDay(0).setShouldTickTime(true)
                .setSunny(random.nextBetween(MIN_TIME_UNTIL_WEATHER_CHANGE_TICKS, MAX_TIME_UNTIL_WEATHER_CHANGE_TICKS));

        return context.openWithWorld(worldConfig, (game, world) -> {
            WorldBorderUtil.WarnInLogIfMultiWorldBordersIsNotInstalled(world);
            world.getWorldBorder().setSize(config.mapConfig().size);

            StevesManhuntMiniGameWaiting waiting = new StevesManhuntMiniGameWaiting(game.getGameSpace(), world, map, context.config());
            GameWaitingLobby.addTo(game, config.players());
            game.listen(GameActivityEvents.REQUEST_START, waiting::requestStart);
            game.listen(GamePlayerEvents.ADD, waiting::addPlayer);
            game.listen(GamePlayerEvents.OFFER, JoinOffer::accept);
            game.listen(GamePlayerEvents.ACCEPT, joinAcceptor -> joinAcceptor.teleport(world, Vec3d.ZERO));
            game.listen(PlayerDeathEvent.EVENT, waiting::onPlayerDeath);
        });
    }

    private GameResult requestStart() {
        StevesManhuntMiniGameActive.open(this.gameSpace, this.world, this.map, this.config);
        return GameResult.ok();
    }

    private void addPlayer(ServerPlayerEntity player) {
        this.spawnPlayer(player);
        WorldBorderUtil.WarnInChatToServerAdministratorIfMultiWorldBordersIsNotInstalled(player);
    }

    private ActionResult onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
        player.setHealth(20.0f);
        this.spawnPlayer(player);
        return ActionResult.CONSUME;
    }

    private void spawnPlayer(ServerPlayerEntity player) {
        this.spawnLogic.resetPlayer(player, GameMode.ADVENTURE);
        this.spawnLogic.spawnPlayer(player);
    }
}
