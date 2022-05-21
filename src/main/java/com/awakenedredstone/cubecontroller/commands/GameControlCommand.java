package com.awakenedredstone.cubecontroller.commands;

import com.awakenedredstone.cubecontroller.CubeController;
import com.awakenedredstone.cubecontroller.GameControl;
import com.awakenedredstone.cubecontroller.commands.argument.RegistryEntryArgumentType;
import com.awakenedredstone.cubecontroller.events.CubeControllerEvents;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.argument.NbtCompoundArgumentType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class GameControlCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("gamecontrol").requires(source -> source.hasPermissionLevel(2))
                .then(argument("gameControl", RegistryEntryArgumentType.registry(CubeController.GAME_CONTROL))
                        .then(literal("get")
                                .then(literal("enabled")
                                        .executes(context -> executeGetEnabled(context.getSource(),
                                                RegistryEntryArgumentType.getRegistryValue(context, "gameControl", GameControl.class))))
                                .then(literal("value")
                                        .executes(context -> executeGetValue(context.getSource(),
                                                RegistryEntryArgumentType.getRegistryValue(context, "gameControl", GameControl.class))))
                                .then(literal("data")
                                        .executes(context -> executeGetData(context.getSource(),
                                                RegistryEntryArgumentType.getRegistryValue(context, "gameControl", GameControl.class)))))
                        .then(literal("set")
                                .then(literal("enabled")
                                        .then(argument("boolean", BoolArgumentType.bool())
                                                .executes(context -> executeSetEnabled(context.getSource(),
                                                        RegistryEntryArgumentType.getRegistryValue(context, "gameControl", GameControl.class),
                                                        BoolArgumentType.getBool(context, "boolean")))))
                                .then(literal("value")
                                        .then(argument("double", DoubleArgumentType.doubleArg())
                                                .executes(context -> executeSetValue(context.getSource(),
                                                        RegistryEntryArgumentType.getRegistryValue(context, "gameControl", GameControl.class),
                                                        DoubleArgumentType.getDouble(context, "double")))))
                                .then(literal("data")
                                        .then(argument("nbt", NbtCompoundArgumentType.nbtCompound())
                                                .executes(context -> executeSetData(context.getSource(),
                                                        RegistryEntryArgumentType.getRegistryValue(context, "gameControl", GameControl.class),
                                                        NbtCompoundArgumentType.getNbtCompound(context, "nbt")))))
                                .then(literal("dataRaw")
                                        .then(argument("nbt", NbtCompoundArgumentType.nbtCompound())
                                                .executes(context -> executeSetDataRaw(context.getSource(),
                                                        RegistryEntryArgumentType.getRegistryValue(context, "gameControl", GameControl.class),
                                                        NbtCompoundArgumentType.getNbtCompound(context, "nbt"))))))
                        .then(literal("invoke")
                                .executes(source -> executeInvoke(source.getSource(), RegistryEntryArgumentType.getRegistryValue(source, "gameControl", GameControl.class))))
                )
        );
    }

    public static int executeSetEnabled(ServerCommandSource source, GameControl control, boolean enabled) {
        control.enabled(enabled);
        sendFeedback(source, "commands.cubecontroller.set.enabled", control, true, new TranslatableText("text.cubecontroller.enabled." + enabled));
        CubeController.getCubeData().markDirty();
        return 0;
    }

    public static int executeGetEnabled(ServerCommandSource source, GameControl control) {
        sendFeedback(source, "commands.cubecontroller.get.enabled", control, false, new TranslatableText("text.cubecontroller.enabled." + control.enabled()));
        return 0;
    }

    public static int executeSetValue(ServerCommandSource source, GameControl control, double value) throws CommandSyntaxException {
        if (!control.valueBased()) {
            throw createCommandException("commands.cubecontroller.error.notValueBased", control);
        }
        control.value(value);
        sendFeedback(source, "commands.cubecontroller.set.value", control, true, value);
        CubeController.getCubeData().markDirty();
        return 0;
    }

    public static int executeGetValue(ServerCommandSource source, GameControl control) {
        if (!control.valueBased())
            sendFeedback(source, "commands.cubecontroller.error.notValueBased", control, false, control.value());
        else sendFeedback(source, "commands.cubecontroller.get.value", control, false, control.value());
        return 0;
    }

    public static int executeSetData(ServerCommandSource source, GameControl control, NbtCompound nbt) throws CommandSyntaxException {
        NbtCompound nbtCompound = control.getNbt().copyFrom(nbt);
        if (control.getNbt().equals(nbtCompound)) {
            throw createCommandException("commands.cubecontroller.error.nothingChanged", control);
        }
        control.copyNbt(nbt);
        sendFeedback(source, "commands.cubecontroller.set.nbtData", control, true);
        CubeController.getCubeData().markDirty();
        return 0;
    }

    public static int executeSetDataRaw(ServerCommandSource source, GameControl control, NbtCompound nbt) throws CommandSyntaxException {
        NbtCompound nbtCompound = control.getNbt().copyFrom(nbt);
        if (control.getNbt().equals(nbtCompound)) {
            throw createCommandException("commands.cubecontroller.error.nothingChanged", control);
        }
        control.setNbt(nbt);
        sendFeedback(source, "commands.cubecontroller.set.nbtData", control, true);
        CubeController.getCubeData().markDirty();
        return 0;
    }

    public static int executeGetData(ServerCommandSource source, GameControl control) {
        sendNbtFeedback(source, "commands.cubecontroller.get.nbtData", control, true, control.getNbt());
        return 0;
    }

    public static int executeInvoke(ServerCommandSource source, GameControl control) throws CommandSyntaxException {
        if (control.event() == CubeControllerEvents.NONE) {
            throw createCommandException("commands.cubecontroller.error.doesNotHaveEvent", control);
        }
        control.invoke();
        sendFeedback(source, "commands.cubecontroller.invoke", control, true);
        return 0;
    }

    private static void sendNbtFeedback(ServerCommandSource source, String key, GameControl control, boolean broadcastToOps, NbtCompound nbtCompound) {
        source.sendFeedback(new TranslatableText(key, CubeController.getIdentifierTranslation(control.identifier()), NbtHelper.toPrettyPrintedText(nbtCompound)), broadcastToOps);
    }

    private static void sendFeedback(ServerCommandSource source, String key, GameControl control, boolean broadcastToOps, Object... arguments) {
        List<Object> args = new ArrayList<>(List.of(CubeController.getIdentifierTranslation(control.identifier())));
        args.addAll(List.of(arguments));
        source.sendFeedback(new TranslatableText(key, args.toArray()), broadcastToOps);
    }

    private static CommandSyntaxException createCommandException(String key, GameControl control, Object... arguments) {
        List<Object> args = new ArrayList<>(List.of(CubeController.getIdentifierTranslation(control.identifier())));
        args.addAll(List.of(arguments));
        return new SimpleCommandExceptionType(new TranslatableText(key, args.toArray())).create();
    }
}
