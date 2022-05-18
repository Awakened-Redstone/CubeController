package com.awakenedredstone.cubecontroller.mixin;

import com.awakenedredstone.cubecontroller.CubeController;
import com.awakenedredstone.cubecontroller.GameControl;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author TechnoAlli3
 */
@Mixin(PlayerEntity.class)
public abstract class RotatePlayerMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void rotateEntity(CallbackInfo ci) {
        GameControl rotatePlayer = CubeController.getControlSafe(new Identifier(CubeController.MOD_ID, "rotate_player"));
        if (rotatePlayer.enabled() || rotatePlayer.value() != 0) {
            float yaw = ((PlayerEntity) (Object) this).getYaw();
            yaw += rotatePlayer.value();
            ((PlayerEntity) (Object) this).setYaw(yaw);
        }
    }
}
