package com.awakenedredstone.cubecontroller.mixin;

import com.awakenedredstone.cubecontroller.CubeController;
import com.awakenedredstone.cubecontroller.GameControl;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author TechnoAlli3
 */
@Mixin(PlayerEntity.class)
public abstract class ShuffleInventoryMixin extends LivingEntity {
    protected ShuffleInventoryMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Shadow @Final private PlayerInventory inventory;

    private static int shuffleCooldown = 0;

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void writeData(NbtCompound nbt, CallbackInfo ci) {
        nbt.putInt("shufflecooldown", shuffleCooldown);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readData(NbtCompound nbt, CallbackInfo ci) {
        shuffleCooldown = nbt.getInt("shufflecooldown");
    }


    @Inject(method = "tick", at = @At("TAIL"))
    private void shuffleInventory(CallbackInfo ci) {
        GameControl inventoryShuffle = CubeController.getControlSafe(new Identifier(CubeController.MOD_ID, "inventory_shuffle"));
        if (!inventoryShuffle.enabled() || inventoryShuffle.value() <= 0 || inventory.isEmpty() || getEntityWorld().isClient) return;
        if (shuffleCooldown++ >= inventoryShuffle.value()) {
            int size = inventoryShuffle.getNbt().getBoolean("shuffleEverything") ? inventory.size() : inventory.main.size();
            shuffleCooldown = 0;

            for (int i = 0; i < size; i++) {
                int slotA = random.nextInt(size);
                int slotB = random.nextInt(size);

                ItemStack stackA = inventory.getStack(slotA);
                ItemStack stackB = inventory.getStack(slotB);

                if (stackA != null) {
                    inventory.removeStack(slotA);
                }
                if (stackB != null) {
                    inventory.removeStack(slotB);
                }

                if (stackA != null) {
                    inventory.setStack(slotB, stackA);
                }
                if (stackB != null) {
                    inventory.setStack(slotA, stackB);
                }
            }
        }
    }
}
