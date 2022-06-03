package com.awakenedredstone.cubecontroller.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.command.argument.ArgumentTypes;
import net.minecraft.command.argument.serialize.ArgumentSerializer;

@Deprecated(forRemoval = true) //Temporary while FAPI don't have it
@Mixin(ArgumentTypes.class)
public interface ArgumentTypesAccessor {
    @Accessor("CLASS_MAP")
    static Map<Class<?>, ArgumentSerializer<?, ?>> fabric_getClassMap() {
        throw new AssertionError("");
    }
}
