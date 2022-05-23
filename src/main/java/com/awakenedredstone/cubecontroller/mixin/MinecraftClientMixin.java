package com.awakenedredstone.cubecontroller.mixin;

import com.awakenedredstone.cubecontroller.events.MinecraftClientCallback;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/resource/ReloadableResourceManagerImpl;registerReloader(Lnet/minecraft/resource/ResourceReloader;)V", ordinal = 19, shift = At.Shift.AFTER))
    private void init(RunArgs args, CallbackInfo ci) {
        MinecraftClientCallback.SPRITE_MANAGER.invoker().invoke((MinecraftClient) (Object) this);
    }
}
