package com.pkfl.creepyhorse.scenario;

import com.mojang.brigadier.ParseResults;
import com.pkfl.creepyhorse.scenario.CreepyHorseScenario;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="creepyhorse")
public final class ScenarioEvents {
    private static final ResourceLocation SADDLE_RECIPE = new ResourceLocation("minecraft", "saddle");

    private ScenarioEvents() {
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        ServerPlayer player;
        Player player2 = event.getEntity();
        if (player2 instanceof ServerPlayer && (player = (ServerPlayer)player2).serverLevel().dimension() == Level.OVERWORLD) {
            CreepyHorseScenario.spawnIntroPig(player.serverLevel(), player);
            CreepyHorseScenario.syncLoopSound(player.serverLevel(), player);
            player.getServer().getRecipeManager().byKey(SADDLE_RECIPE).ifPresent(recipe -> player.awardRecipes(List.of(recipe)));
        }
    }

    @SubscribeEvent
    public static void onPigDeath(LivingDropsEvent event) {
        Pig pig;
        block3: {
            block2: {
                LivingEntity livingEntity = event.getEntity();
                if (!(livingEntity instanceof Pig)) break block2;
                pig = (Pig)livingEntity;
                if (!pig.level().isClientSide && CreepyHorseScenario.isIntroPig(pig)) break block3;
            }
            return;
        }
        ServerLevel level = (ServerLevel)pig.level();
        event.getDrops().add(new ItemEntity((Level)level, pig.getX(), pig.getY() + 0.25, pig.getZ(), CreepyHorseScenario.rulesBook()));
        CreepyHorseScenario.introPigKilled(level);
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        ServerLevel level;
        Level level2;
        if (event.phase == TickEvent.Phase.END && (level2 = event.level) instanceof ServerLevel && (level = (ServerLevel)level2).dimension() == Level.OVERWORLD) {
            CreepyHorseScenario.tick(level);
        }
    }

    @SubscribeEvent
    public static void onGameModeChanged(PlayerEvent.PlayerChangeGameModeEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer player2 = (ServerPlayer)player;
            if (event.getNewGameMode().isCreative() && player2.serverLevel().dimension() == Level.OVERWORLD) {
                CreepyHorseScenario.recordCheat(player2.serverLevel());
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        ServerPlayer player;
        Player player2;
        if (event.phase == TickEvent.Phase.END && (player2 = event.player) instanceof ServerPlayer && (player = (ServerPlayer)player2).isCreative() && player.serverLevel().dimension() == Level.OVERWORLD) {
            CreepyHorseScenario.recordCheat(player.serverLevel());
        }
    }

    @SubscribeEvent
    public static void onCommand(CommandEvent event) {
        ServerPlayer player;
        ParseResults results = event.getParseResults();
        String input = results.getReader().getString().trim();
        if (input.startsWith("creepyhorse")) {
            return;
        }
        Entity entity = ((CommandSourceStack)results.getContext().getSource()).getEntity();
        if (entity instanceof ServerPlayer && (player = (ServerPlayer)entity).serverLevel().dimension() == Level.OVERWORLD) {
            if (CreepyHorseScenario.commandsRevoked(player.serverLevel())) {
                event.setCanceled(true);
                player.displayClientMessage((Component)Component.literal((String)"COMMANDS REVOKED"), true);
                return;
            }
            CreepyHorseScenario.recordCheat(player.serverLevel());
        }
    }
}

