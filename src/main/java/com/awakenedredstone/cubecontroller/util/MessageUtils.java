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
        if (CubeController.getServer() != null) CubeController.getServer().getCommandSource().sendFeedback(message.formatted(Formatting.RED), true);
    }

    public static void sendError(BaseText message, Throwable throwable) {
        LOGGER.error(message.getString(), throwable);
        if (CubeController.getServer() != null) CubeController.getServer().getCommandSource().sendFeedback(message.formatted(Formatting.RED), true);
    }

    public static void broadcast(Consumer<ServerPlayerEntity> consumer, Identifier identifier) {
        if (CubeController.getServer() != null) {
            broadcast(CubeController.getServer().getPlayerManager().getPlayerList(), consumer, identifier);
        }
    }

    public static void broadcastPacket(Identifier identifier, PacketByteBuf buf) {
        broadcast(player -> ServerPlayNetworking.send(player, identifier, buf), new Identifier(identifier.getNamespace(), "packet/" + identifier.getPath()));
    }

    public static void broadcast(List<ServerPlayerEntity> players, Consumer<ServerPlayerEntity> consumer, Identifier identifier) {
        try {
            players.forEach(consumer);
        } catch (Exception e) {
            if (CubeController.getServer() != null) CubeController.getServer().getCommandSource().sendFeedback(broadcastError(identifier), true);
            LOGGER.error("Error at broadcast " + identifier, e);
        }
    }

    public static void broadcastToOps(Consumer<ServerPlayerEntity> consumer, Identifier identifier) {
        if (CubeController.getServer() != null) {
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
                    CubeController.getServer().getCommandSource().sendFeedback(broadcastError(identifier), true);
                    LOGGER.error("Error at broadcast " + identifier, e);
                }
            } else {
                broadcast(consumer, identifier);
            }
        }
    }

    public static void sendTitle(ServerCommandSource source, ServerPlayerEntity player, Text title, Function<Text, Packet<?>> constructor) throws CommandSyntaxException {
        player.networkHandler.sendPacket(constructor.apply(Texts.parse(source, title, player, 0)));
    }

    public static void sendTitle(ServerPlayerEntity player, Text title, Function<Text, Packet<?>> constructor) {
        player.networkHandler.sendPacket(constructor.apply(title));
    }

    private static Text broadcastError(Identifier identifier) {
        return new TranslatableText("text.cubecontroller.error.broadcast", identifier).formatted(Formatting.RED);
    }
}
