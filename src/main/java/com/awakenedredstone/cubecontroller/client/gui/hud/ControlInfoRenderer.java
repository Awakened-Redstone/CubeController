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
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public class ControlInfoRenderer {
    public static final ControlInfoRenderer INSTANCE = new ControlInfoRenderer();
    private final MinecraftClient client = MinecraftClient.getInstance();

    public void render(MatrixStack matrix, float tickDelta) {
        try {
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
                MutableText text = control.valueBased() && enabled ? new LiteralText(new DecimalFormat("0.###").format(control.value())) :
                        new LiteralText(enabled ? "ON" : "OFF").formatted(enabled ? Formatting.GREEN : Formatting.RED);
                int screenWidth = (client.getWindow().getScaledWidth() / 3) / 25;
                int y = 25 * (i / screenWidth) + 1;
                int x = 25 * (i++ % screenWidth) + 1;
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 0.8f);
                inGameHud.drawTexture(matrix, x, y, 165, 166, 24, 24);
                final TextRenderer textRenderer = client.textRenderer;
                textRender.add(() -> {
                    matrix.push();
                    int width = textRenderer.getWidth(text);
                    matrix.translate(x + 12, y + 23, 0);
                    if (width > 20) matrix.scale(20.0f / width, 20.0f / width, 1);
                    matrix.translate(-(width / 2), -textRenderer.fontHeight, 0);
                    DrawableHelper.drawTextWithShadow(matrix, textRenderer, text, 0, 0, 0xFFFFFF);
                    matrix.pop();
                });
                Sprite sprite;
                if (control.identifier().equals(CubeController.identifier("entity_jump")) &&
                        Objects.equals(client.player.getUuid().toString(), "5f8eb73b-25be-4c5a-a50f-d27d65e30ca0")) {
                    sprite = spriteManager.getSprite(CubeController.identifier("jump_captain"));
                } else {
                    sprite = spriteManager.getSprite(control);
                }
                spriteRender.add(() -> {
                    RenderSystem.setShaderTexture(0, sprite.getAtlas().getId());
                    RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
                    InGameHud.drawSprite(matrix, x + 3, 3 + y, inGameHud.getZOffset(), 18, 18, sprite);
                });
            }
            spriteRender.forEach(Runnable::run);
            textRender.forEach(Runnable::run);
            matrix.pop();
        } catch (Exception e) {
            try {
                DrawableHelper.drawStringWithShadow(matrix, client.textRenderer, "§cRendering error!", 12, 12, 0xFF5555);
            } catch (Exception ignored) {
                CubeController.LOGGER.error("", e);
            }
        }
    }
}
