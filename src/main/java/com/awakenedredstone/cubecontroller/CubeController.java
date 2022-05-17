package com.awakenedredstone.cubecontroller;

import com.awakenedredstone.cubecontroller.commands.GameControlCommand;
import com.awakenedredstone.cubecontroller.events.CubeControllerEvents;
import com.awakenedredstone.cubecontroller.exceptions.GameControlException;
import com.awakenedredstone.cubecontroller.util.ConversionUtils;
import com.awakenedredstone.cubecontroller.util.EasyNbtCompound;
import com.awakenedredstone.cubecontroller.util.MessageUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;

public class CubeController implements ModInitializer {
    public static final String MOD_ID = "cubecontroller";
    public static final Logger LOGGER = LoggerFactory.getLogger("CubeController");

    private static final Set<StatusEffect> blacklistedPotions = new HashSet<>();
    public static final Registry<GameControl> GAME_CONTROL = FabricRegistryBuilder.createSimple(GameControl.class, identifier("game_controls"))
            .attribute(RegistryAttribute.PERSISTED).attribute(RegistryAttribute.SYNCED).buildAndRegister();


    private static CubeData cubeData;
    private static MinecraftServer server;

    @Override
    public void onInitialize() {
        registerControl(identifier("player_jump"));
        registerControl(identifier("entity_jump"));
        registerControl(identifier("player_movement_speed"));
        registerControl(identifier("player_speed_attribute"));
        registerControl(identifier("entity_speed_attribute"));
        registerControl(identifier("player_health_attribute"));
        registerControl(identifier("entity_health_attribute"));
        registerControl(identifier("potion_chaos"), CubeControllerEvents.POTION_CHAOS);
        registerControl(identifier("floating_items"), null, false);
        registerControl(identifier("inventory_shuffle"), null, true, new EasyNbtCompound(Map.of("shuffleEverything", NbtByte.of(false))));
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

    public static void blackListEffect(StatusEffect effect) {
        blacklistedPotions.add(effect);
    }

    private void registerCommands(MinecraftServer server) {
        GameControlCommand.register(server.getCommandManager().getDispatcher());
    }

    private void registerListeners() {
        ServerLifecycleEvents.SERVER_STARTING.register(this::registerCommands);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            getCubeData().markDirty();
        });
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, serverResourceManager, success) -> registerCommands(server));
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
