package com.awakenedredstone.cubecontroller.mixin;

import com.awakenedredstone.cubecontroller.CubeController;
import com.awakenedredstone.cubecontroller.GameControl;
import com.awakenedredstone.cubecontroller.util.ConversionUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.awakenedredstone.cubecontroller.CubeController.identifier;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    @Shadow public abstract AttributeContainer getAttributes();

    @Shadow public abstract float getHealth();

    @Shadow @Final private static TrackedData<Float> HEALTH;

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "jump", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;setVelocity(DDD)V", shift = At.Shift.AFTER))
    private void jump(CallbackInfo ci) {
        GameControl jump = CubeController.getControlSafe(new Identifier(CubeController.MOD_ID, "entity_jump"));
        if (jump.enabled() && (this.isPlayer() || !jump.getNbt().getBoolean("playerOnly"))) {
                this.addVelocity(0, jump.value() * 0.1f, 0);
        }
    }

    @Inject(method = "getAttributeValue", at = @At("HEAD"), cancellable = true)
    public final void getAttributeValue(EntityAttribute attribute, CallbackInfoReturnable<Double> cir) {
        GameControl health = CubeController.getControlSafe(identifier("entity_health_attribute"));
        GameControl speed = CubeController.getControlSafe(identifier("entity_speed_attribute"));

        if (attribute == EntityAttributes.GENERIC_MAX_HEALTH && health.enabled() && (this.isPlayer() || !health.getNbt().getBoolean("playerOnly"))) {
            double newHealth = this.getAttributes().getValue(attribute) + health.value();
            if (this.getHealth() > newHealth) this.dataTracker.set(HEALTH, MathHelper.clamp(this.getHealth(), 0.0f, ConversionUtils.toFloat(newHealth)));
            cir.setReturnValue(newHealth);
        } else if (attribute == EntityAttributes.GENERIC_MOVEMENT_SPEED && speed.enabled() && (this.isPlayer() || !speed.getNbt().getBoolean("playerOnly"))) {
            double newSpeed = this.getAttributes().getValue(attribute) * speed.value();
            cir.setReturnValue(newSpeed);
        }
    }
}
