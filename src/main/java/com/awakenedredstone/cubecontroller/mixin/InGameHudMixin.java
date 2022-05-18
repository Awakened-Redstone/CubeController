package com.awakenedredstone.cubecontroller.mixin;

import com.awakenedredstone.cubecontroller.events.HudRenderEvents;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(value= EnvType.CLIENT)
@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Inject(method = "tick()V", at = @At(value = "HEAD"))
    public void tick(CallbackInfo ci) {
        HudRenderEvents.TICK.invoker().onHudTick();
    }

    @Inject(method = "tick(Z)V", at = @At(value = "HEAD"))
    public void preTick(boolean paused, CallbackInfo ci) {
        HudRenderEvents.PRE_TICK.invoker().onHudPreTick(paused);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderColor(FFFF)V"), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/PlayerListHud;render(Lnet/minecraft/client/util/math/MatrixStack;ILnet/minecraft/scoreboard/Scoreboard;Lnet/minecraft/scoreboard/ScoreboardObjective;)V")))
    public void render(MatrixStack matrixStack, float tickDelta, CallbackInfo callbackInfo) {
        HudRenderEvents.RENDER.invoker().onHudRender(matrixStack, tickDelta);
    }

    @Inject(method = "drawHeart", at = @At(value = "HEAD"), cancellable = true)
    public void drawHeart(MatrixStack matrices, InGameHud.HeartType type, int x, int y, int v, boolean blinking, boolean halfHeart, CallbackInfo ci) {
        if (y < -3) ci.cancel();
    }
}
