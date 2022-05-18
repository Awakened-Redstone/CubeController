package com.awakenedredstone.cubecontroller.client;

import com.awakenedredstone.cubecontroller.CubeController;
import com.awakenedredstone.cubecontroller.client.render.ControlListRenderer;
import com.awakenedredstone.cubecontroller.events.HudRenderEvents;
import com.awakenedredstone.cubecontroller.util.ConversionUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class CubeControllerClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HudRenderEvents.RENDER.register(ControlListRenderer.INSTANCE::render);
        HudRenderEvents.TICK.register(ControlListRenderer.INSTANCE::tick);

        ClientPlayNetworking.registerGlobalReceiver(new Identifier(CubeController.MOD_ID, "enabled_controls"), (client, handler, buf, responseSender) -> {
            Identifier identifier = buf.readIdentifier();
            double value = buf.readDouble();
            client.execute(() -> {
                float fontScale = 1;
                int messageWidth = client.textRenderer.getWidth(new TranslatableText("text.cubecontroller.value", value));
                float x = ConversionUtils.toFloat(client.getWindow().getScaledWidth() - (messageWidth * fontScale) - 4);
                float y = ConversionUtils.toFloat(client.getWindow().getScaledHeight() - (client.textRenderer.fontHeight * fontScale) - 4);
                ControlListRenderer.INSTANCE.addText(identifier, value, true, x, y, 0xFFFFFF, fontScale);
            });
        });
    }
}
