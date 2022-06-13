package com.awakenedredstone.cubecontroller.commands;

import com.awakenedredstone.cubecontroller.CubeController;
import com.awakenedredstone.cubecontroller.GameControl;
import com.awakenedredstone.cubecontroller.commands.argument.RegistryEntryArgumentType;
import com.awakenedredstone.cubecontroller.util.ConversionUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.StorageDataObject;
import net.minecraft.command.argument.NbtCompoundArgumentType;
import net.minecraft.nbt.AbstractNbtNumber;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.DataCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class GameControlCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("gamecontrol").requires(source -> source.hasPermissionLevel(2))
                .then(argument("control", RegistryEntryArgumentType.registry(CubeController.GAME_CONTROL))
                        .then(literal("get")
                                .then(literal("enabled")
                                        .executes(context -> executeGetEnabled(context.getSource(),
                                                RegistryEntryArgumentType.getRegistryValue(context, "control", GameControl.class))))
                                .then(literal("value")
                                        .executes(context -> executeGetValue(context.getSource(),
                                                RegistryEntryArgumentType.getRegistryValue(context, "control", GameControl.class))))
                                .then(literal("data")
                                        .executes(context -> executeGetData(context.getSource(),
                                                RegistryEntryArgumentType.getRegistryValue(context, "control", GameControl.class)))))
                        .then(literal("set")
                                .then(literal("enabled")
                                        .then(argument("boolean", BoolArgumentType.bool())
                                                .executes(context -> executeSetEnabled(context.getSource(),
                                                        RegistryEntryArgumentType.getRegistryValue(context, "control", GameControl.class),
                                                        BoolArgumentType.getBool(context, "boolean")))))
                                .then(literal("value")
                                        .then(argument("double", DoubleArgumentType.doubleArg())
                                                .executes(context -> executeSetValue(context.getSource(),
                                                        RegistryEntryArgumentType.getRegistryValue(context, "control", GameControl.class),
                                                        DoubleArgumentType.getDouble(context, "double")))))
                                .then(literal("data")
                                        .then(argument("nbt", NbtCompoundArgumentType.nbtCompound())
                                                .suggests((context, builder) ->
                                                        builder.suggest(RegistryEntryArgumentType.getRegistryValue(context, "control", GameControl.class)
                                                                .getNbt().toString()).buildFuture())
                                                .executes(context -> executeSetData(context.getSource(),
                                                        RegistryEntryArgumentType.getRegistryValue(context, "control", GameControl.class),
                                                        NbtCompoundArgumentType.getNbtCompound(context, "nbt"))))
                                        .then(addStorageArgument(false)))
                                .then(literal("dataRaw")
                                        .then(argument("nbt", NbtCompoundArgumentType.nbtCompound())
                                                .suggests((context, builder) ->
                                                        builder.suggest(RegistryEntryArgumentType.getRegistryValue(context, "control", GameControl.class)
                                                                .getNbt().toString()).buildFuture())
                                                .executes(context -> executeSetDataRaw(context.getSource(),
                                                        RegistryEntryArgumentType.getRegistryValue(context, "control", GameControl.class),
                                                        NbtCompoundArgumentType.getNbtCompound(context, "nbt"))))
                                        .then(addStorageArgument(true))))
                        .then(literal("invoke")
                                .executes(source -> executeInvoke(source.getSource(), RegistryEntryArgumentType.getRegistryValue(source, "control", GameControl.class))))
                )
        );
    }

    private static ArgumentBuilder<ServerCommandSource, ?> addStorageArgument(boolean dataRaw) {
        LiteralArgumentBuilder<ServerCommandSource> literalArgumentBuilder = CommandManager.literal("from");
        DataCommand.ObjectType objectType = StorageDataObject.TYPE_FACTORY.apply("source");
        objectType.addArgumentsToBuilder(literalArgumentBuilder, builder -> {
            SetNbtData<ServerCommandSource, GameControl, NbtCompound> setData = dataRaw ? GameControlCommand::executeSetDataRaw : GameControlCommand::executeSetData;

            return builder.executes(context ->
                    setData.apply(context.getSource(),
                            RegistryEntryArgumentType.getRegistryValue(context, "control", GameControl.class),
                            objectType.getObject(context).getNbt()));
        });

        return literalArgumentBuilder;
    }

    public static int executeSetEnabled(ServerCommandSource source, GameControl control, boolean enabled) {
        control.enabled(enabled);
        sendFeedback(source, "commands.cubecontroller.set.enabled", control, true, Text.translatable("text.cubecontroller.enabled." + enabled));
        return 1;
    }

    public static int executeGetEnabled(ServerCommandSource source, GameControl control) {
        sendFeedback(source, "commands.cubecontroller.get.enabled", control, false, Text.translatable("text.cubecontroller.enabled." + control.enabled()));
        return control.enabled() ? 1 : 0;
    }

    public static int executeSetValue(ServerCommandSource source, GameControl control, double value) throws CommandSyntaxException {
        if (!control.valueBased()) {
            throw createCommandException("commands.cubecontroller.error.notValueBased", control);
        } else if (!control.isWithinLimit(value)) {
            NbtElement limit = control.getNbt().get("limit");
            if (limit instanceof NbtCompound compound) {
                throw createCommandException("commands.cubecontroller.error.limit.bounds", control, compound.getDouble("min"), compound.getDouble("max"));
            } else if (limit instanceof AbstractNbtNumber number) {
                throw createCommandException("commands.cubecontroller.error.limit", control, number.doubleValue());
            }
        }
        control.value(value);
        sendFeedback(source, "commands.cubecontroller.set.value", control, true, value);
        return 1;
    }

    public static int executeGetValue(ServerCommandSource source, GameControl control) {
        if (!control.valueBased())
            sendFeedback(source, "commands.cubecontroller.error.notValueBased", control, false, control.value());
        else sendFeedback(source, "commands.cubecontroller.get.value", control, false, control.value());
        return ConversionUtils.toInt(control.value());
    }

    public static int executeSetData(ServerCommandSource source, GameControl control, NbtCompound nbt) throws CommandSyntaxException {
        NbtCompound nbtCompound = control.getNbt().copyFrom(nbt);
        if (control.getNbt().equals(nbtCompound)) {
            throw createCommandException("commands.cubecontroller.error.nothingChanged", control);
        }
        control.copyNbt(nbt);
        sendFeedback(source, "commands.cubecontroller.set.nbtData", control, true);
        return 1;
    }

    public static int executeSetDataRaw(ServerCommandSource source, GameControl control, NbtCompound nbt) throws CommandSyntaxException {
        NbtCompound nbtCompound = control.getNbt().copyFrom(nbt);
        if (control.getNbt().equals(nbtCompound)) {
            throw createCommandException("commands.cubecontroller.error.nothingChanged", control);
        }
        control.setNbt(nbt);
        sendFeedback(source, "commands.cubecontroller.set.nbtData", control, true);
        return 1;
    }

    public static int executeGetData(ServerCommandSource source, GameControl control) {
        sendNbtFeedback(source, "commands.cubecontroller.get.nbtData", control, true, control.getNbt());
        return 1;
    }

    public static int executeInvoke(ServerCommandSource source, GameControl control) throws CommandSyntaxException {
        if (!control.hasEvent()) throw createCommandException("commands.cubecontroller.error.doesNotHaveEvent", control);

        boolean success = control.invoke();
        if (success) sendFeedback(source, "commands.cubecontroller.invoke.success", control, true);
        else sendFeedback(source, "commands.cubecontroller.invoke.fail", control, true);

        return success ? 1 : 0;
    }

    private static void sendNbtFeedback(ServerCommandSource source, String key, GameControl control, boolean broadcastToOps, NbtCompound nbtCompound) {
        source.sendFeedback(Text.translatable(key, CubeController.getIdentifierTranslation(control.identifier()), NbtHelper.toPrettyPrintedText(nbtCompound)), broadcastToOps);
    }

    private static void sendFeedback(ServerCommandSource source, String key, GameControl control, boolean broadcastToOps, Object... arguments) {
        List<Object> args = new ArrayList<>(List.of(CubeController.getIdentifierTranslation(control.identifier())));
        args.addAll(List.of(arguments));
        source.sendFeedback(Text.translatable(key, args.toArray()), broadcastToOps);
    }

    private static CommandSyntaxException createCommandException(String key, GameControl control, Object... arguments) {
        List<Object> args = new ArrayList<>(List.of(CubeController.getIdentifierTranslation(control.identifier())));
        args.addAll(List.of(arguments));
        return new SimpleCommandExceptionType(Text.translatable(key, args.toArray())).create();
    }

    @FunctionalInterface
    private interface SetNbtData<ServerCommandSource, GameControl, NbtCompound> {
        int apply(ServerCommandSource source, GameControl control, NbtCompound nbt) throws CommandSyntaxException;
    }
}
