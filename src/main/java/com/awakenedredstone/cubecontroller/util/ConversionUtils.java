package com.awakenedredstone.cubecontroller.util;

import net.minecraft.util.math.MathHelper;

public class ConversionUtils {

    public static String ticksToTime(int totalTicks) {
        byte ticks = toByte(totalTicks % 20);
        byte seconds = toByte((totalTicks /= 20) % 60);
        byte minutes = toByte((totalTicks /= 60) % 60);
        short hours = toShort(totalTicks / 60);
        return String.format("%02d:%02d:%02d.%02d", hours, minutes, seconds, ticks);
    }

    public static String ticksToSimpleTime(int _totalTicks) {
        double totalTicks = _totalTicks;
        byte seconds = toByte((totalTicks /= 20) % 60);
        byte minutes = toByte((totalTicks /= 60) % 60);
        short hours = toShort(totalTicks / 60);
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public static byte toByte(double value) {
        return (byte) MathHelper.clamp(value, Byte.MIN_VALUE, Byte.MAX_VALUE);
    }

    public static byte toByte(float value) {
        return (byte) MathHelper.clamp(value, Byte.MIN_VALUE, Byte.MAX_VALUE);
    }

    public static byte toByte(long value) {
        return (byte) MathHelper.clamp(value, Byte.MIN_VALUE, Byte.MAX_VALUE);
    }

    public static byte toByte(int value) {
        return (byte) MathHelper.clamp(value, Byte.MIN_VALUE, Byte.MAX_VALUE);
    }

    public static byte toByte(short value) {
        return (byte) MathHelper.clamp(value, Byte.MIN_VALUE, Byte.MAX_VALUE);
    }

    public static short toShort(double value) {
        return (short) MathHelper.clamp(value, Short.MIN_VALUE, Short.MAX_VALUE);
    }

    public static short toShort(float value) {
        return (short) MathHelper.clamp(value, Short.MIN_VALUE, Short.MAX_VALUE);
    }

    public static short toShort(long value) {
        return (short) MathHelper.clamp(value, Short.MIN_VALUE, Short.MAX_VALUE);
    }

    public static short toShort(int value) {
        return (short) MathHelper.clamp(value, Short.MIN_VALUE, Short.MAX_VALUE);
    }

    public static int toInt(double value) {
        return (int) MathHelper.clamp(value, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public static int toInt(float value) {
        return (int) MathHelper.clamp(value, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public static int toInt(long value) {
        return (int) MathHelper.clamp(value, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public static long toLong(double value) {
        return (long) MathHelper.clamp(value, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public static long toLong(float value) {
        return (long) MathHelper.clamp(value, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public static float toFloat(double value) {
        return (float) MathHelper.clamp(value, Float.MIN_VALUE, Float.MAX_VALUE);
    }
}
