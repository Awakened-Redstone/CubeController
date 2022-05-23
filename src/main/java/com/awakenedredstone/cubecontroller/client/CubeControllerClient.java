package com.awakenedredstone.cubecontroller.client;

import com.awakenedredstone.cubecontroller.CubeController;
import com.awakenedredstone.cubecontroller.client.gui.hud.ControlListRenderer;
import com.awakenedredstone.cubecontroller.client.texture.GameControlSpriteManager;
import com.awakenedredstone.cubecontroller.events.HudRenderEvents;
import com.awakenedredstone.cubecontroller.events.MinecraftClientCallback;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.resource.ReloadableResourceManagerImpl;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class CubeControllerClient implements ClientModInitializer {
    public static CubeControllerClient INSTANCE;
    public final Map<Identifier, GameControlInfo> controlInfo = new HashMap<>();
    public GameControlSpriteManager controlSpriteManager;

    @Override
    public void onInitializeClient() {
        INSTANCE = this;
        MinecraftClientCallback.SPRITE_MANAGER.register(client -> {
            controlSpriteManager = new GameControlSpriteManager(client.getTextureManager());
            ((ReloadableResourceManagerImpl) client.getResourceManager()).registerReloader(this.controlSpriteManager);
        });
        HudRenderEvents.RENDER.register(ControlListRenderer.INSTANCE::render);
        HudRenderEvents.TICK.register(ControlListRenderer.INSTANCE::tick);

        ClientLoginConnectionEvents.DISCONNECT.register((handler, client) -> controlInfo.clear());

        ClientPlayNetworking.registerGlobalReceiver(CubeController.identifier("remove_info"), (client, handler, buf, responseSender) -> {
            Identifier identifier = buf.readIdentifier();
            client.execute(() -> {
                controlInfo.remove(identifier);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(CubeController.identifier("info_update"), (client, handler, buf, responseSender) -> {
            Identifier identifier = buf.readIdentifier();
            boolean enabled = buf.readBoolean();
            boolean valueBased = buf.readBoolean();
            double value = valueBased ? buf.readDouble() : 0;
            client.execute(() -> {
                controlInfo.put(identifier, new GameControlInfo(identifier, enabled, valueBased, value));
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(CubeController.identifier("bulk_info_update"), (client, handler, buf, responseSender) -> {
            NbtCompound nbt = buf.readNbt();
            client.execute(() -> {
                controlInfo.clear();
                for (String key : nbt.getKeys()) {
                    NbtCompound info = nbt.getCompound(key);
                    boolean valueBased = info.getBoolean("valueBased");
                    double value = valueBased ? info.getDouble("value") : 0;
                    controlInfo.put(new Identifier(key), new GameControlInfo(new Identifier(key), info.getBoolean("enabled"), valueBased, value));
                }
            });
        });
    }
}
