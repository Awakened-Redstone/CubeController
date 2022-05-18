package com.awakenedredstone.cubecontroller.mixin;

import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SimpleRegistry.class)
public interface SimpleRegistryMixin<T> {
    @Invoker void callAssertNotFrozen(RegistryKey<T> key);
}
