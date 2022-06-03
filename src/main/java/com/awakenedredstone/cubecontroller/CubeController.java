package com.awakenedredstone.cubecontroller;

import com.awakenedredstone.cubecontroller.commands.GameControlCommand;
import com.awakenedredstone.cubecontroller.commands.argument.RegistryEntryArgumentType;
import com.awakenedredstone.cubecontroller.events.CubeControllerEvents;
import com.awakenedredstone.cubecontroller.exceptions.GameControlException;
import com.awakenedredstone.cubecontroller.mixin.ArgumentTypesAccessor;
import com.awakenedredstone.cubecontroller.mixin.SimpleRegistryMixin;
import com.awakenedredstone.cubecontroller.util.ConversionUtils;
import com.awakenedredstone.cubecontroller.util.MessageUtils;
import com.awakenedredstone.cubecontroller.util.NbtBuilder;
import com.awakenedredstone.cubecontroller.util.PacketUtils;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.entity.SculkSpreadManager;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.datafixer.Schemas;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
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

import static com.awakenedredstone.cubecontroller.events.ControlEvents.*;

public class CubeController implements ModInitializer {
    public static final String MOD_ID = "cubecontroller";
    public static final Logger LOGGER = LoggerFactory.getLogger("CubeController");
    public static final SimpleRegistry<GameControl> GAME_CONTROL = FabricRegistryBuilder.createSimple(GameControl.class, identifier("game_controls"))
            .attribute(RegistryAttribute.PERSISTED).attribute(RegistryAttribute.SYNCED).buildAndRegister();

    private static final PersistentStateManager persistentStateManager = new PersistentStateManager(getModDataDir().toFile(), Schemas.getFixer());
    private static final Set<StatusEffect> blacklistedPotions = new HashSet<>();
    private static final SculkSpreadManager spreadManager = SculkSpreadManager.create();

    private static CubeData cubeData;
    private static ModPersistentData modData;
    private static MinecraftServer server;

    @Override
    public void onInitialize() {
        registerRegistryArgumentType(GAME_CONTROL);
        registerControl(identifier("entity_jump"), NbtBuilder.create().addBoolean("playerOnly", true).build());
        registerControl(identifier("player_movement_speed"));
        registerControl(identifier("entity_speed_attribute"), NbtBuilder.create().addBoolean("playerOnly", true).build());
        registerControl(identifier("entity_health_attribute"), NbtBuilder.create().addBoolean("playerOnly", true).build());
        registerControl(identifier("potion_chaos"), POTION_CHAOS);
        registerControl(identifier("floating_items"), null, false);
        registerControl(identifier("inventory_shuffle"), SHUFFLE_INVENTORY, true, NbtBuilder.create().addBoolean("shuffleEverything", false).build());
        registerControl(identifier("rotate_player"));
        registerControl(identifier("sculk_spread_chaos"), SCULK_CHAOS, true, NbtBuilder.create()
                .addCompound("limit", NbtBuilder.create().addDouble("min", 0).addDouble("max", 32000).build())
                .addBoolean("noCursorCap", true)
                .addBoolean("noChargeCap", false)
                .build());
        registerControl(identifier("laggy/sculk_charge_no_limit"), null, false);
        registerPacketHandlers();
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

    public static void registerRegistryArgumentType(Registry<?> registry) {
        ConstantArgumentSerializer<? extends RegistryEntryArgumentType<?>> serializer = ConstantArgumentSerializer.of(() -> RegistryEntryArgumentType.registry(registry));
        Identifier identifier = identifier("registry/" + registry.getKey().getRegistry().getPath());
        //ArgumentTypeRegistry.registerArgumentType(identifier, RegistryEntryArgumentType.class, serializer);
        ArgumentTypesAccessor.fabric_getClassMap().put(RegistryEntryArgumentType.class, serializer);
        Registry.register(Registry.COMMAND_ARGUMENT_TYPE, identifier, serializer);
    }

    /**
     * <p>Generates a Translatable text with the key on the format
     * {@code <namespace>.<path>} with any {@code /} on path being replaced with {@code .}<br/>
     * Examples: {@code minecraft:jump_boost} -> {@code minecraft.jump_boost}, <br/>
     * {@code minecraft:chests/stronghold_library} -> {@code minecraft.chests.stronghold_library}
     *
     * @param identifier The {@link Identifier}
     * @return a {@link MutableText} with the key on the format {@code <namespace>.<path>}
     */
    public static MutableText getIdentifierTranslation(Identifier identifier) {
        return Text.translatable(String.format("%s.%s", identifier.getNamespace(), identifier.getPath().replaceAll("/", ".")));
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

    private void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        GameControlCommand.register(dispatcher);
    }

    private void registerPacketHandlers() {
        ServerPlayNetworking.registerGlobalReceiver(identifier("request_info_update"), (server, player, handler, buf, responseSender) -> {
            server.execute(() -> {
                responseSender.sendPacket(identifier("bulk_info_update"), PacketUtils.controlInfoBulkUpdate(PacketByteBufs.create()));
            });
        });
    }

    private void registerListeners() {
        CommandRegistrationCallback.EVENT.register(this::registerCommands);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            if (server.isSingleplayer()) {
                CubeController.server = null;
                NbtCompound defaultState = getModData().getData().getCompound("gameControlDefaultState");
                for (String key : defaultState.getKeys()) {
                    if (GAME_CONTROL.containsId(new Identifier(key))) {
                        GAME_CONTROL.get(new Identifier(key)).enabled(false);
                        GAME_CONTROL.get(new Identifier(key)).value(0);
                        GAME_CONTROL.get(new Identifier(key)).setNbt(defaultState.getCompound(key).getCompound("data"));
                    }
                }
            }
        });
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            cubeData = server.getOverworld().getPersistentStateManager().getOrCreate(CubeData::fromNbt, CubeData::new, MOD_ID);
            CubeController.server = server;
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            sender.sendPacket(identifier("bulk_info_update"), PacketUtils.controlInfoBulkUpdate(PacketByteBufs.create()));
        });

        SHUFFLE_INVENTORY.register(control ->
                MessageUtils.broadcast(player -> {
                    PlayerInventory inventory = player.getInventory();
                    net.minecraft.util.math.random.Random random = player.getRandom();
                    int size = control.getNbt().getBoolean("shuffleEverything") ? inventory.size() : inventory.main.size();

                    for (int i = 0; i < size; i++) {
                        int slotA = random.nextInt(size);
                        int slotB = random.nextInt(size);

                        ItemStack stackA = inventory.getStack(slotA);
                        ItemStack stackB = inventory.getStack(slotB);

                        if (stackA != null) {
                            inventory.removeStack(slotA);
                        }
                        if (stackB != null) {
                            inventory.removeStack(slotB);
                        }

                        if (stackA != null) {
                            inventory.setStack(slotB, stackA);
                        }
                        if (stackB != null) {
                            inventory.setStack(slotA, stackB);
                        }
                    }
                }, identifier("control/inventory_shuffle"))
        );

        POTION_CHAOS.register(control ->
                MessageUtils.broadcast(player -> {
                    Random random = new Random();
                    double _duration = Math.abs(random.nextGaussian(15, 20));
                    if (_duration < 5) _duration += random.nextDouble(20);
                    double _amplifier = Math.abs(random.nextGaussian(0, 23));
                    int duration = ConversionUtils.toInt(Math.round(_duration * 20));
                    int amplifier = ConversionUtils.toInt(Math.round(_amplifier));
                    Optional<RegistryEntry<StatusEffect>> randomPotion;
                    StatusEffect effect;
                    do {
                        randomPotion = Registry.STATUS_EFFECT.getRandom(net.minecraft.util.math.random.Random.create());
                        effect = randomPotion.isPresent() ? randomPotion.get().value() : StatusEffects.SLOWNESS;
                    } while (blacklistedPotions.contains(effect));
                    if (player.hasStatusEffect(effect)) {
                        StatusEffectInstance playerEffect = player.getStatusEffect(effect);
                        duration += playerEffect.getDuration();
                        amplifier = Math.max(amplifier, playerEffect.getAmplifier());
                        player.removeStatusEffect(playerEffect.getEffectType());
                    }
                    player.addStatusEffect(new StatusEffectInstance(effect, duration, amplifier, false, false, true));
                }, identifier("control/apply_potion_chaos"))
        );

        SCULK_CHAOS.register(control -> {
            MessageUtils.broadcast(player -> {
                spreadManager.spread(player.getBlockPos(), ConversionUtils.toInt(control.value()));
            }, identifier("control/apply_sculk_spread_chaos"));
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (getControlSafe(identifier("sculk_spread_chaos")).enabled()) {
                MessageUtils.broadcast(player -> {
                    spreadManager.tick(player.getWorld(), player.getBlockPos(), server.getOverworld().getRandom(), true);
                }, identifier("control/tick_sculk_spread_chaos"));
            }
        });
    }
}
