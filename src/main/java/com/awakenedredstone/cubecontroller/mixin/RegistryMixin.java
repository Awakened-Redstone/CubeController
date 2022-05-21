package com.awakenedredstone.cubecontroller.mixin;

import com.awakenedredstone.cubecontroller.CubeController;
import com.awakenedredstone.cubecontroller.GameControl;
import com.awakenedredstone.cubecontroller.ModPersistentData;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Registry.class)
public class RegistryMixin {

    @Inject(method = "freezeRegistries", at = @At("HEAD"))
    private static void freezeRegistries(CallbackInfo ci) {
        CubeController.setModData(CubeController.getPersistentStateManager().getOrCreate(ModPersistentData::new, ModPersistentData::new, CubeController.MOD_ID));
        NbtCompound registry = new NbtCompound();
        for (GameControl control : CubeController.GAME_CONTROL) {
            NbtCompound data = new NbtCompound();
            data.put("data", control.getNbt());
            registry.put(control.identifier().toString(), data);
        }
        CubeController.getModData().getData().put("gameControlDefaultState", registry);
        CubeController.getModData().markDirty();
        CubeController.getPersistentStateManager().save();
    }
}
