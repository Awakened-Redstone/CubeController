package com.awakenedredstone.cubecontroller.mixin;

import com.awakenedredstone.cubecontroller.CubeController;
import com.awakenedredstone.cubecontroller.GameControl;
import com.awakenedredstone.cubecontroller.commands.argument.RegistryEntryArgumentType;
import com.mojang.brigadier.ResultConsumer;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.DataCommandObject;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.nbt.*;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.DataCommand;
import net.minecraft.server.command.ExecuteCommand;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.BinaryOperator;
import java.util.function.IntFunction;

@Mixin(ExecuteCommand.class)
public abstract class ExecuteCommandMixin {
    @Shadow @Final private static BinaryOperator<ResultConsumer<ServerCommandSource>> BINARY_RESULT_CONSUMER;

    @Inject(method = "addStoreArguments", at = @At("TAIL"))
    private static void addStoreArguments(LiteralCommandNode<ServerCommandSource> node, LiteralArgumentBuilder<ServerCommandSource> builder2, boolean requestResult, CallbackInfoReturnable<ArgumentBuilder<ServerCommandSource, ?>> cir) {
        if (requestResult) {
            builder2.then(CommandManager.literal("control")
                    .then(CommandManager.literal("value")
                            .then(CommandManager.literal("value")
                                    .then(CommandManager.argument("scale", DoubleArgumentType.doubleArg())
                                            .then(CommandManager.argument("control", RegistryEntryArgumentType.registry(CubeController.GAME_CONTROL))
                                                    .redirect(node, context -> executeStoreControlValue(context.getSource(),
                                                            RegistryEntryArgumentType.getRegistryValue(context, "control", GameControl.class),
                                                            result -> result * DoubleArgumentType.getDouble(context, "scale"))))))));
        }
    }

    private static ServerCommandSource executeStoreControlValue(ServerCommandSource source, GameControl control, IntFunction<Double> value) {

        return source.mergeConsumers((context, success, result) -> {
            control.value(value.apply(result));
        }, BINARY_RESULT_CONSUMER);
    }
}
