package com.awakenedredstone.cubecontroller.client.gui.hud;

import com.awakenedredstone.cubecontroller.CubeController;
import com.awakenedredstone.cubecontroller.client.CubeControllerClient;
import com.awakenedredstone.cubecontroller.client.GameControlInfo;
import com.awakenedredstone.cubecontroller.client.texture.GameControlSpriteManager;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class ControlListRenderer {
    public static final ControlListRenderer INSTANCE = new ControlListRenderer();
    private final MinecraftClient client = MinecraftClient.getInstance();

    public void render(MatrixStack matrix, float tickDelta) {
        matrix.push();
        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0, HandledScreen.BACKGROUND_TEXTURE);

        final GameControlSpriteManager spriteManager = CubeControllerClient.INSTANCE.controlSpriteManager;
        final InGameHud inGameHud = client.inGameHud;
        final ArrayList<Runnable> textRender = Lists.newArrayListWithExpectedSize(CubeControllerClient.INSTANCE.controlInfo.size());
        final ArrayList<Runnable> spriteRender = Lists.newArrayListWithExpectedSize(CubeControllerClient.INSTANCE.controlInfo.size());

        int i = 0;
        for (Map.Entry<Identifier, GameControlInfo> controlInfo : CubeControllerClient.INSTANCE.controlInfo.entrySet()) {
            GameControlInfo control = controlInfo.getValue();
            boolean enabled = control.enabled();
            boolean valueBased = control.valueBased();
            double value = control.value();
            int x = 25 * i++ + 1;
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 0.8f);
            inGameHud.drawTexture(matrix, x, 1, 165, 166, 24, 24);
            textRender.add(() -> DrawableHelper.drawStringWithShadow(matrix, client.textRenderer, "aaa", x + 3, 14, 0xFFFFFF));
            Sprite sprite = spriteManager.getSprite(control);
            int a = 0; //Dummy line
            /*spriteRender.add(() -> {
                RenderSystem.setShaderTexture(0, sprite.getAtlas().getId());
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
                InGameHud.drawSprite(matrix, x + 3,  3, inGameHud.getZOffset(), 18, 18, sprite);
            });*/
        }
        textRender.forEach(Runnable::run);
        spriteRender.forEach(Runnable::run);
        matrix.pop();
    }

    public void tick() {}
}
