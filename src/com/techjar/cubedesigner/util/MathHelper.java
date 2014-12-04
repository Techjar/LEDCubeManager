package com.techjar.cubedesigner.util;

import org.lwjgl.util.Color;

/**
 * Class containing extra math-based commands which don't exist in java.lang.Math, such as clamp().
 * @author Techjar
 */
public final class MathHelper {
    private MathHelper() {
    }

    /**
     * Converts input number to a number within the specified range.
     * @param i input number
     * @param low minimum range
     * @param high maximum range
     * @return clamped number
     */
    public static int clamp(int i, int low, int high) {
        return Math.max(Math.min(i, high), low);
    }

    /**
     * Converts input number to a number within the specified range.
     * @param i input number
     * @param low minimum range
     * @param high maximum range
     * @return clamped number
     */
    public static long clamp(long i, long low, long high) {
        return Math.max(Math.min(i, high), low);
    }

    /**
     * Converts input number to a number within the specified range.
     * @param i input number
     * @param low minimum range
     * @param high maximum range
     * @return clamped number
     */
    public static double clamp(double i, double low, double high) {
        return Math.max(Math.min(i, high), low);
    }

    /**
     * Converts input number to a number within the specified range.
     * @param i input number
     * @param low minimum range
     * @param high maximum range
     * @return clamped number
     */
    public static float clamp(float i, float low, float high) {
        return Math.max(Math.min(i, high), low);
    }

    /**
     * Returns a number indicating the sign (+/-) of a number, as either -1, 0, or 1.
     * @param i input number
     * @return sign of number
     */
    public static int sign(int i) {
        return clamp(i, -1, 1);
    }

    /**
     * Returns a number indicating the sign (+/-) of a number, as either -1, 0, or 1.
     * @param i input number
     * @return sign of number
     */
    public static long sign(long i) {
        return clamp(i, -1, 1);
    }

    /**
     * Returns a number indicating the sign (+/-) of a number, as either -1, 0, or 1.
     * @param i input number
     * @return sign of number
     */
    public static double sign(double i) {
        return clamp(i < 0 ? Math.floor(i) : Math.ceil(i), -1, 1);
    }

    /**
     * Returns a number indicating the sign (+/-) of a number, as either -1, 0, or 1.
     * @param i input number
     * @return sign of number
     */
    public static float sign(float i) {
        return clamp(i < 0 ? (float)Math.floor(i) : (float)Math.ceil(i), -1, 1);
    }

    /**
     * Performs a linear interpolation between <em>start</em> and <em>end</em> using <em>fraction</em>.
     * @param start starting value
     * @param end ending value
     * @param fraction interpolation fraction from 0 to 1
     * @return the interpolated value
     */
    public static double lerp(double start, double end, double fraction) {
        return (start * (1.0D - fraction)) + (end * fraction);
    }

    /**
     * Performs a linear interpolation between <em>start</em> and <em>end</em> using <em>fraction</em>.
     * @param start starting value
     * @param end ending value
     * @param fraction interpolation fraction from 0 to 1
     * @return the interpolated value
     */
    public static Vector2 lerp(Vector2 start, Vector2 end, float fraction) {
        return start.multiply(1.0F - fraction).add(end.multiply(fraction));
    }

    /**
     * Performs a linear interpolation between <em>start</em> and <em>end</em> using <em>fraction</em>.
     * @param start starting value
     * @param end ending value
     * @param fraction interpolation fraction from 0 to 1
     * @return the interpolated value
     */
    public static Vector3 lerp(Vector3 start, Vector3 end, float fraction) {
        return start.multiply(1.0F - fraction).add(end.multiply(fraction));
    }

    /**
     * Performs a linear interpolation between <em>start</em> and <em>end</em> using <em>fraction</em>.
     * @param start starting value
     * @param end ending value
     * @param fraction interpolation fraction from 0 to 1
     * @return the interpolated value
     */
    public static Angle lerp(Angle start, Angle end, float fraction) {
        float fractionInverse = 1.0F - fraction;
        float pitch = (start.getPitch() * fractionInverse) + (end.getPitch() * fraction);
        float yaw = (start.getYaw() * fractionInverse) + (end.getYaw() * fraction);
        float roll = (start.getRoll() * fractionInverse) + (end.getRoll() * fraction);
        return new Angle(pitch, yaw, roll);
    }

    /**
     * Performs a linear interpolation between <em>start</em> and <em>end</em> using <em>fraction</em>.
     * @param start starting value
     * @param end ending value
     * @param fraction interpolation fraction from 0 to 1
     * @return the interpolated value
     */
    public static Color lerp(Color start, Color end, float fraction) {
        float fractionInverse = 1 - fraction;
        //System.out.println(fraction);
        float red = (start.getRed() * fractionInverse) + (end.getRed() * fraction);
        float green = (start.getGreen() * fractionInverse) + (end.getGreen() * fraction);
        float blue = (start.getBlue() * fractionInverse) + (end.getBlue() * fraction);
        float alpha = (start.getAlpha() * fractionInverse) + (end.getAlpha() * fraction);
        return new Color((int)red, (int)green, (int)blue, (int)alpha);
    }
}
