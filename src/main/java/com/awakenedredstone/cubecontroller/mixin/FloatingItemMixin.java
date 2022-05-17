package com.awakenedredstone.cubecontroller.mixin;

import com.awakenedredstone.cubecontroller.CubeController;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author TechnoAlli3
 */
@Mixin(Entity.class)
public abstract class FloatingItemMixin {

    @Inject(method="tick", at=@At("TAIL"))
    private void FloatItem(CallbackInfo ci) {
        if(((Entity)(Object)this) instanceof ItemEntity item && CubeController.getControlSafe(new Identifier(CubeController.MOD_ID, "floating_items")).enabled) {
            item.setNoGravity(true);
        }
    }
}
