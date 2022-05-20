package com.awakenedredstone.cubecontroller;

import com.awakenedredstone.cubecontroller.client.texture.GameControlSpriteManager;
import com.awakenedredstone.cubecontroller.commands.GameControlCommand;
import com.awakenedredstone.cubecontroller.events.CubeControllerEvents;
import com.awakenedredstone.cubecontroller.exceptions.GameControlException;
import com.awakenedredstone.cubecontroller.mixin.SimpleRegistryMixin;
import com.awakenedredstone.cubecontroller.util.ConversionUtils;
import com.awakenedredstone.cubecontroller.util.EasyNbtCompound;
import com.awakenedredstone.cubecontroller.util.MessageUtils;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.texture.StatusEffectSpriteManager;
import net.minecraft.datafixer.Schemas;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.world.PersistentStateManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class CubeController implements ModInitializer {
    public static final String MOD_ID = "cubecontroller";
    public static final Logger LOGGER = LoggerFactory.getLogger("CubeController");
    public static final SimpleRegistry<GameControl> GAME_CONTROL = FabricRegistryBuilder.createSimple(GameControl.class, identifier("game_controls"))
            .attribute(RegistryAttribute.PERSISTED).attribute(RegistryAttribute.SYNCED).buildAndRegister();

    private static final PersistentStateManager persistentStateManager = new PersistentStateManager(getModDataDir().toFile(), Schemas.getFixer());
    private static final Set<StatusEffect> blacklistedPotions = new HashSet<>();

    private static CubeData cubeData;
    private static ModPersistentData modData;
    private static MinecraftServer server;

    @Override
    public void onInitialize() {
        registerControl(identifier("entity_jump"), new EasyNbtCompound(Map.of("playerOnly", NbtByte.of(true))));
        registerControl(identifier("player_movement_speed"));
        registerControl(identifier("entity_speed_attribute"), new EasyNbtCompound(Map.of("playerOnly", NbtByte.of(true))));
        registerControl(identifier("entity_health_attribute"), new EasyNbtCompound(Map.of("playerOnly", NbtByte.of(true))));
        registerControl(identifier("potion_chaos"), CubeControllerEvents.POTION_CHAOS);
        registerControl(identifier("floating_items"), null, false);
        registerControl(identifier("inventory_shuffle"), new EasyNbtCompound(Map.of("shuffleEverything", NbtByte.of(false))));
        registerControl(identifier("rotate_player"));
        registerListeners();
    }

    public static @NotNull Optional<GameControl> getControl(Identifier identifier) {
        GameControl control = GAME_CONTROL.get(identifier);
        return control == null ? Optional.empty() : Optional.of(control);
    }

    public static @NotNull GameControl getControlSafe(Identifier identifier) {
        Optional<GameControl> control = getControl(identifier);
        if (control.isEmpty()) LOGGER.error("", new GameControlException(identifier(identifier.getPath())));
        return control.orElse(new GameControl(identifier("null")));
    }

    public static void registerControl(@NotNull Identifier identifier) {
        Registry.register(GAME_CONTROL, identifier, new GameControl(identifier));
    }

    public static void registerControl(@NotNull Identifier identifier, @NotNull NbtCompound nbt) {
        Registry.register(GAME_CONTROL, identifier, new GameControl(identifier, CubeControllerEvents.NONE, true, nbt));
    }

    public static void registerControl(@NotNull Identifier identifier, @NotNull Event<CubeControllerEvents> event) {
        Registry.register(GAME_CONTROL, identifier, new GameControl(identifier, event));
    }

    public static void registerControl(@NotNull Identifier identifier, @Nullable Event<CubeControllerEvents> event, boolean valueBased) {
        Registry.register(GAME_CONTROL, identifier, new GameControl(identifier, Objects.requireNonNullElse(event, CubeControllerEvents.NONE), valueBased));
    }

    public static void registerControl(@NotNull Identifier identifier, @Nullable Event<CubeControllerEvents> event, boolean valueBased, @NotNull NbtCompound nbt) {
        Registry.register(GAME_CONTROL, identifier, new GameControl(identifier, Objects.requireNonNullElse(event, CubeControllerEvents.NONE), valueBased, nbt));
    }

    public static void registerControl(@NotNull Identifier identifier, @NotNull GameControl control) {
        Registry.register(GAME_CONTROL, identifier, control);
    }

    public static void registerControl(@NotNull GameControl control) {
        Registry.register(GAME_CONTROL, control.identifier(), control);
    }

    /**
     * <p>Generates a Translatable text with the key on the format
     * {@code <namespace>.<path>} with any {@code /} on path being replaced with {@code .}<br/>
     * Examples: {@code minecraft:jump_boost} -> {@code minecraft.jump_boost}, <br/>
     * {@code minecraft:chests/stronghold_library} -> {@code minecraft.chests.stronghold_library}
     *
     * @param identifier The {@link Identifier}
     * @return a {@link TranslatableText} with the key on the format {@code <namespace>.<path>}
     */
    public static TranslatableText getIdentifierTranslation(Identifier identifier) {
        return new TranslatableText(String.format("%s.%s", identifier.getNamespace(), identifier.getPath().replaceAll("/", ".")));
    }

    public static MinecraftServer getServer() {
        return server;
    }

    public static Identifier identifier(String path) {
        return new Identifier(MOD_ID, path);
    }

    public static CubeData getCubeData() {
        return cubeData;
    }

    public static ModPersistentData getModData() {
        return modData;
    }

    @SuppressWarnings("unchecked")
    public static void setModData(ModPersistentData modData) {
        ((SimpleRegistryMixin<GameControl>) GAME_CONTROL).callAssertNotFrozen(GAME_CONTROL.getEntrySet().iterator().next().getKey());
        CubeController.modData = modData;
    }

    public static void blackListEffect(StatusEffect effect) {
        blacklistedPotions.add(effect);
    }

    public static PersistentStateManager getPersistentStateManager() {
        return persistentStateManager;
    }

    public static Path getModDataDir() {
        Path modDataDir = FabricLoader.getInstance().getGameDir().resolve("mod-data");
        if (!Files.exists(modDataDir)) {
            try {
                Files.createDirectories(modDataDir);
            } catch (IOException e) {
                throw new RuntimeException("Creating mod persistent data directory", e);
            }
        }
        return modDataDir;
    }

    private void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        GameControlCommand.register(dispatcher);
    }

    private void registerListeners() {
        CommandRegistrationCallback.EVENT.register(this::registerCommands);
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTicks() % 20 == 0) {
                PacketByteBuf buf = PacketByteBufs.create();
                NbtCompound nbt = new NbtCompound();
                Stream<GameControl> controlStream = GAME_CONTROL.stream()
                        .filter(control -> !control.nbtData().getBoolean("hideInfo"))
                        .filter(control -> control.nbtData().getBoolean("alwaysVisible") || control.enabled());
                for (GameControl control : controlStream.toList()) {
                    NbtCompound info = new NbtCompound();
                    info.putBoolean("enabled", control.enabled());
                    info.putBoolean("valueBased", control.valueBased());
                    if (control.valueBased()) info.putDouble("value", control.value());
                    nbt.put(control.identifier().toString(), info);
                }

                buf.writeNbt(nbt);
                MessageUtils.broadcast(player -> ServerPlayNetworking.send(player, new Identifier(MOD_ID, "update_control_info"), buf), "send_control_info");
            }
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            if (server.isSingleplayer()) {
                NbtCompound defaultState = getModData().getData().getCompound("gameControlDefaultState");
                for (String key : defaultState.getKeys()) {
                    if (GAME_CONTROL.containsId(new Identifier(key))) {
                        GAME_CONTROL.get(new Identifier(key)).enabled(false);
                        GAME_CONTROL.get(new Identifier(key)).value(0);
                        GAME_CONTROL.get(new Identifier(key)).nbtData(defaultState.getCompound(key).getCompound("data"));
                    }
                }
            }
        });
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            CubeController.server = server;
            cubeData = server.getOverworld().getPersistentStateManager().getOrCreate(CubeData::fromNbt, CubeData::new, MOD_ID);
        });

        getControlSafe(new Identifier(MOD_ID, "potion_chaos")).event().register(() -> {
            MessageUtils.broadcast(player -> {
                Random random = new Random();
                double _duration = random.nextGaussian(16, 7) + Math.max(0, random.nextGaussian(-50, 80));
                double _amplifier = random.nextGaussian(3, 2) + Math.max(0, random.nextGaussian(-10, 20));
                int duration = ConversionUtils.toInt(Math.round(MathHelper.clamp(3, _duration, 90) * 20));
                int amplifier = ConversionUtils.toInt(Math.round(MathHelper.clamp(0, _amplifier, 100)));
                Optional<RegistryEntry<StatusEffect>> randomPotion;
                StatusEffect effect;
                do {
                    randomPotion = Registry.STATUS_EFFECT.getRandom(random);
                    effect = randomPotion.isPresent() ? randomPotion.get().value() : StatusEffects.SLOWNESS;
                } while (blacklistedPotions.contains(effect));
                if (player.hasStatusEffect(effect)) {
                    StatusEffectInstance playerEffect = player.getStatusEffect(effect);
                    duration += playerEffect.getDuration();
                    amplifier = Math.max(amplifier, playerEffect.getAmplifier());
                    player.removeStatusEffectInternal(playerEffect.getEffectType());
                }
                player.addStatusEffect(new StatusEffectInstance(effect, duration, amplifier, false, false, true));
            }, "potion_chaos");
        });
    }
}
