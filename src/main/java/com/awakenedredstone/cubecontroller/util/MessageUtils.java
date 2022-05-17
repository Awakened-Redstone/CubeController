package com.awakenedredstone.cubecontroller.util;

import com.awakenedredstone.cubecontroller.CubeController;
import com.awakenedredstone.cubecontroller.override.ExtendedOperatorList;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.BaseText;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.awakenedredstone.cubecontroller.CubeController.LOGGER;

public class MessageUtils {

    public static void sendError(BaseText message) {
        LOGGER.error(message.getString());
        broadcastToOps(player -> player.sendMessage(message.formatted(Formatting.RED), false), "error_message");
    }

    public static void sendError(BaseText message, Throwable throwable) {
        LOGGER.error(message.getString(), throwable);
        broadcastToOps(player -> player.sendMessage(message.formatted(Formatting.RED), false), "error_message");
    }

    public static void broadcast(Consumer<ServerPlayerEntity> consumer, String id) {
        broadcast(CubeController.getServer().getPlayerManager().getPlayerList(), consumer, id);
    }

    public static void broadcast(List<ServerPlayerEntity> players, Consumer<ServerPlayerEntity> consumer, String id) {
        try {
            players.forEach(consumer);
        } catch (Exception e) {
            CubeController.getServer().getCommandSource().sendFeedback(new TranslatableText("text.subathon.error.broadcast", id).formatted(Formatting.RED), true);
        }
    }

    public static void broadcastToOps(Consumer<ServerPlayerEntity> consumer, String id) {
        if (CubeController.getServer().isDedicated()) {
            try {
                if (CubeController.getServer().getPlayerManager().getOpList() instanceof ExtendedOperatorList ops) {
                    List<UUID> uuids = Arrays.stream(ops.getUUIDs()).toList();
                    CubeController.getServer().getPlayerManager().getPlayerList().stream().filter(player -> uuids.contains(player.getUuid())).forEach(consumer);
                } else {
                    List<String> names = Arrays.stream(CubeController.getServer().getPlayerManager().getOpList().getNames()).toList();
                    CubeController.getServer().getPlayerManager().getPlayerList().stream().filter(player -> names.contains(player.getGameProfile().getName())).forEach(consumer);
                }
            } catch (Exception e) {
                CubeController.getServer().getCommandSource().sendFeedback(new TranslatableText("text.subathon.error.broadcast", id), true);
            }
        } else {
            broadcast(consumer, id);
        }
    }

    public static void sendTitle(ServerCommandSource source, ServerPlayerEntity player, Text title, Function<Text, Packet<?>> constructor) throws CommandSyntaxException {
        player.networkHandler.sendPacket(constructor.apply(Texts.parse(source, title, player, 0)));
    }

    public static void sendTitle(ServerPlayerEntity player, Text title, Function<Text, Packet<?>> constructor) {
        player.networkHandler.sendPacket(constructor.apply(title));
    }
}
