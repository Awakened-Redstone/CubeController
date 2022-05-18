package com.awakenedredstone.cubecontroller.client.render;

import com.awakenedredstone.cubecontroller.CubeController;
import com.awakenedredstone.cubecontroller.mixin.InGameHudAccessor;
import com.awakenedredstone.cubecontroller.util.ConversionUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.*;

public class ControlListRenderer {
    public static final ControlListRenderer INSTANCE = new ControlListRenderer();
    private final MinecraftClient client = MinecraftClient.getInstance();
    private final Map<Identifier, TextRenderer> texts = new HashMap<>();

    public void addText(Identifier identifier, double value, boolean shadow, float x, float y, int color) {
        addText(identifier, value, shadow, x, y, color, 1, -1, -1, -1);
    }

    public void addText(Identifier identifier, double value, boolean shadow, float x, float y, int color, float fontScale) {
        addText(identifier, value, shadow, x, y, color, fontScale, -1, -1, -1);
    }

    public void addText(Identifier identifier, double value, boolean shadow, float x, float y, int color, float fontScale, int stay, int fadeIn, int fadeOut) {
        if (texts.containsKey(identifier)) {
            texts.get(identifier).keepAlive();
            texts.get(identifier).updatePos(x, y);
            texts.get(identifier).updateValue(value);
            texts.get(identifier).updateScale(fontScale);
        } else {
            texts.put(identifier, new TextRenderer(identifier, value, shadow, x, y, color, fontScale, stay, fadeIn, fadeOut));
        }
    }

    public void render(MatrixStack matrix, float tickDelta) {
        int y = 0;
        int i = 0;
        matrix.push();
        InGameHudAccessor hud = (InGameHudAccessor) client.inGameHud;
        IntegratedServer integratedServer = client.getServer();
        if (client.currentScreen instanceof ChatScreen) matrix.translate(0, -13, 0.0);
        else if (client.options.showAutosaveIndicator && integratedServer != null && (hud.getAutosaveIndicatorAlpha() > 0.08f || hud.getLastAutosaveIndicatorAlpha() > 0.08f))
            matrix.translate(0, -15, 0.0);
        for (TextRenderer text : texts.values()) {
            if (i++ > 0) y -= client.textRenderer.fontHeight + 4;
            matrix.push();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            matrix.translate(text.x, text.y, 0.0);
            matrix.scale(text.fontScale, text.fontScale, 1);
            matrix.translate(0, y, 0);
            text.render(matrix, tickDelta);
            RenderSystem.disableBlend();
            matrix.pop();
        }
        matrix.pop();
    }

    public void tick() {
        try {
            for (TextRenderer text : texts.values()) text.tick();
        } catch (ConcurrentModificationException ignored) {}
    }

    private class TextRenderer {
        private final Identifier identifier;
        private final Text controlName;
        private final boolean shadow;
        private final int color;
        private float x;
        private float y;
        private double value;
        private Text controlValue;
        private int stayTicks = 60;
        private int fadeOutTicks = 10;
        private int fadeInTicks = 5;
        private int remainingTime;
        private float fontScale;

        public void keepAlive() {
            remainingTime = stayTicks + fadeOutTicks;
        }

        public void updatePos(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public void updateValue(double value) {
            if (value != this.value) {
                this.controlValue = new TranslatableText("text.cubecontroller.value", value);
                this.value = value;
            }
        }

        public void updateScale(float scale) {
            this.fontScale = scale;
        }

        public void tick() {
            if (this.remainingTime-- <= 0) {
                texts.remove(identifier);
            }
        }

        public TextRenderer(Identifier identifier, double value, boolean shadow, float x, float y, int color, float fontScale, int stay, int fadeIn, int fadeOut) {
            this.identifier = identifier;
            this.controlName = CubeController.getIdentifierTranslation(identifier);
            this.controlValue = new TranslatableText("text.cubecontroller.value", value);
            this.value = value;
            this.shadow = shadow;
            this.x = x;
            this.y = y;
            this.color = color;
            this.fontScale = fontScale;
            if (stay > 0) this.stayTicks = stay;
            if (fadeIn >= 0) this.fadeInTicks = fadeIn;
            if (fadeOut >= 0) this.fadeOutTicks = fadeOut;
            this.remainingTime = fadeInTicks + stayTicks + fadeOutTicks;
        }

        public void render(MatrixStack matrix, float tickDelta) {
            if (client.options.hudHidden) return;
            client.getProfiler().push("controlInfoRenderer");
            if (remainingTime > 0) {
                float time = (float) this.remainingTime - tickDelta;
                int fade = 255;
                if (remainingTime > fadeOutTicks + stayTicks) {
                    float o = (fadeInTicks + stayTicks + fadeOutTicks) - time;
                    fade = ConversionUtils.toInt(o * 255.0f / fadeInTicks);
                }
                if (remainingTime <= fadeOutTicks) {
                    fade = ConversionUtils.toInt(time * 255.0f / fadeOutTicks);
                }
                if ((fade = MathHelper.clamp(fade, 0, 255)) > 8) {
                    int fadeAlpha = fade << 24 & 0xFF000000;
                    int alpha = color >> 24 & 0xFF;
                    int red = color >> 16 & 0xFF;
                    int green = color >> 8 & 0xFF;
                    int blue = color & 0xFF;
                    int rgb = (alpha << 24) + (red << 16) + (green << 8) + (blue);

                    if (shadow) {
                        client.textRenderer.drawWithShadow(matrix, controlValue, 0, 0, rgb | fadeAlpha);
                    } else {
                        client.textRenderer.draw(matrix, controlValue, 0, 0, rgb | fadeAlpha);
                    }
                    matrix.push();
                    float nameWidth = client.textRenderer.getWidth(controlName);
                    float valueWidth = client.textRenderer.getWidth(controlValue);
                    float scale = MathHelper.clamp(valueWidth / nameWidth, 0.1f, 0.4f);
                    matrix.scale(scale, scale, 1);
                    matrix.translate(0, ((client.textRenderer.fontHeight) + 2) * -1, 0.0);
                    if (shadow) {
                        client.textRenderer.drawWithShadow(matrix, controlName, 0, 0, rgb | fadeAlpha);
                    } else {
                        client.textRenderer.draw(matrix, controlName, 0, 0, rgb | fadeAlpha);
                    }
                    matrix.pop();
                }
            }
            client.getProfiler().pop();
        }
    }
}
