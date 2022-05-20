package com.awakenedredstone.cubecontroller.client;

import com.awakenedredstone.cubecontroller.CubeController;
import com.awakenedredstone.cubecontroller.client.gui.hud.ControlListRenderer;
import com.awakenedredstone.cubecontroller.client.texture.GameControlSpriteManager;
import com.awakenedredstone.cubecontroller.events.HudRenderEvents;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
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
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            controlSpriteManager = new GameControlSpriteManager(MinecraftClient.getInstance().getTextureManager());
            ((ReloadableResourceManagerImpl) MinecraftClient.getInstance().getResourceManager()).registerReloader(this.controlSpriteManager);
        });
        HudRenderEvents.RENDER.register(ControlListRenderer.INSTANCE::render);
        HudRenderEvents.TICK.register(ControlListRenderer.INSTANCE::tick);

        ClientPlayNetworking.registerGlobalReceiver(new Identifier(CubeController.MOD_ID, "update_control_info"), (client, handler, buf, responseSender) -> {
            NbtCompound nbt = buf.readNbt();
            client.execute(() -> {
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
