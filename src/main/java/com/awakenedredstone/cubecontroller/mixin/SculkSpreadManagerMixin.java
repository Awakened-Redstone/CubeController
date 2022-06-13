package com.awakenedredstone.cubecontroller.mixin;

import com.awakenedredstone.cubecontroller.CubeController;
import com.awakenedredstone.cubecontroller.GameControl;
import net.minecraft.block.entity.SculkSpreadManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(SculkSpreadManager.class)
public class SculkSpreadManagerMixin {
    @Shadow private List<SculkSpreadManager.Cursor> cursors;

    @Inject(method = "addCursor", at = @At(value = "RETURN", ordinal = 0))
    private void addCursor(SculkSpreadManager.Cursor cursor, CallbackInfo ci) {
        GameControl control = CubeController.getControlSafe(CubeController.identifier("laggy/sculk_charge_no_limit"));
        if (control.enabled() && control.getNbt().getBoolean("noCursorCap")) {
            this.cursors.add(cursor);
        }
    }

    @Redirect(method = "spread", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I"))
    private int spreadMathMin(int a, int b) {
        GameControl control = CubeController.getControlSafe(CubeController.identifier("laggy/sculk_charge_no_limit"));
        if (control.enabled() && control.getNbt().getBoolean("noChargeCap")) {
            return a;
        }
        return Math.min(a, b);
    }
}
