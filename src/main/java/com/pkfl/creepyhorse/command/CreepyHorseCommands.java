package com.pkfl.creepyhorse.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.pkfl.creepyhorse.scenario.CreepyHorseData;
import com.pkfl.creepyhorse.scenario.CreepyHorseScenario;
import com.pkfl.creepyhorse.scenario.ScenarioTask;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="creepyhorse")
public final class CreepyHorseCommands {
    private CreepyHorseCommands() {
    }

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal((String)"creepyhorse").requires(source -> source.hasPermission(2))).then(Commands.literal((String)"stage").then(Commands.argument((String)"number", (ArgumentType)IntegerArgumentType.integer((int)1, (int)4)).executes(context -> {
            ServerLevel level = ((CommandSourceStack)context.getSource()).getLevel();
            int stage = IntegerArgumentType.getInteger((CommandContext)context, (String)"number");
            CreepyHorseScenario.setStage(level, stage);
            ((CommandSourceStack)context.getSource()).sendSuccess(() -> Component.literal((String)("Creepy Horse stage set to " + stage)), true);
            return 1;
        })))).then(Commands.literal((String)"task").then(Commands.argument((String)"name", (ArgumentType)StringArgumentType.word()).executes(context -> {
            ScenarioTask task = ScenarioTask.fromCommand(StringArgumentType.getString((CommandContext)context, (String)"name"));
            if (task == ScenarioTask.NONE) {
                ((CommandSourceStack)context.getSource()).sendFailure((Component)Component.literal((String)"Unknown task. Use intro, saddle30, saddle10, saddle5, neck, stalk, flies, follow, feed, enter, hide, survive, or chase."));
                return 0;
            }
            CreepyHorseScenario.forceTask(((CommandSourceStack)context.getSource()).getLevel(), task);
            ((CommandSourceStack)context.getSource()).sendSuccess(() -> Component.literal((String)("Creepy Horse task started: " + String.valueOf((Object)task))), true);
            return 1;
        })))).then(Commands.literal((String)"cheats").then(Commands.argument((String)"enabled", (ArgumentType)BoolArgumentType.bool()).executes(context -> {
            ServerLevel level = ((CommandSourceStack)context.getSource()).getLevel();
            boolean enabled = BoolArgumentType.getBool((CommandContext)context, (String)"enabled");
            CreepyHorseData data = CreepyHorseData.get(level);
            if (enabled) {
                CreepyHorseScenario.recordCheat(level);
            } else {
                data.cheatsDetected = false;
                data.setDirty();
            }
            ((CommandSourceStack)context.getSource()).sendSuccess(() -> Component.literal((String)("Creepy Horse cheat detection: " + enabled)), true);
            return 1;
        })))).then(Commands.literal((String)"status").executes(context -> {
            ((CommandSourceStack)context.getSource()).sendSuccess(() -> Component.literal((String)CreepyHorseScenario.status(((CommandSourceStack)context.getSource()).getLevel())), false);
            return 1;
        }))).then(Commands.literal((String)"stop").executes(context -> {
            CreepyHorseScenario.forceTask(((CommandSourceStack)context.getSource()).getLevel(), ScenarioTask.NONE);
            ((CommandSourceStack)context.getSource()).sendSuccess(() -> Component.literal((String)"Creepy Horse sequence stopped."), true);
            return 1;
        }))).then(Commands.literal((String)"reset").executes(context -> {
            CreepyHorseScenario.reset(((CommandSourceStack)context.getSource()).getLevel());
            ((CommandSourceStack)context.getSource()).sendSuccess(() -> Component.literal((String)"Creepy Horse world sequence reset."), true);
            return 1;
        })));
    }
}

