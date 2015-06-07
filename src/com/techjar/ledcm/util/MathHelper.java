package com.techjar.ledcm.util;

import org.lwjgl.util.Color;

/**
 * Class containing extra math-based commands which don't exist in java.lang.Math, such as clamp().
 * @author Techjar
 */
public final class MathHelper {
    private static final float[] xyzWhiteRef = new float[]{0.95047F, 1F, 1.08883F};

    private MathHelper() {
    }

    public static double log2(double a) {
        return Math.log(a) / Math.log(2);
    }

    public static double log(double a, double base) {
        return Math.log(a) / Math.log(base);
    }

    public static double cie1931(double lum) {
        lum *= 100;
        if (lum <= 8) return lum / 902.3;
        else return Math.pow((lum + 16) / 116, 3);
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
    public static float lerp(float start, float end, float fraction) {
        return (start * (1.0F - fraction)) + (end * fraction);
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

    /**
     * Performs a linear interpolation between <em>start</em> and <em>end</em> using <em>fraction</em>.
     * @param start starting value
     * @param end ending value
     * @param fraction interpolation fraction from 0 to 1
     * @return the interpolated value
     */
    public static Color lerpXyz(Color start, Color end, float fraction) {
        return xyzToRgb(lerpColorSpace(rgbToXyz(start), rgbToXyz(end), fraction));
    }

    /**
     * Performs a linear interpolation between <em>start</em> and <em>end</em> using <em>fraction</em>.
     * @param start starting value
     * @param end ending value
     * @param fraction interpolation fraction from 0 to 1
     * @return the interpolated value
     */
    public static Color lerpLab(Color start, Color end, float fraction) {
        return xyzToRgb(labToXyz(lerpColorSpace(xyzToLab(rgbToXyz(start)), xyzToLab(rgbToXyz(end)), fraction)));
    }

    /**
     * Performs a linear interpolation between <em>start</em> and <em>end</em> using <em>fraction</em>.
     * @param start starting value
     * @param end ending value
     * @param fraction interpolation fraction from 0 to 1
     * @return the interpolated value
     */
    public static float[] lerpColorSpace(float[] start, float[] end, float fraction) {
        float[] ret = new float[3];
        float fractionInverse = 1 - fraction;
        //System.out.println(fraction);
        ret[0] = (start[0] * fractionInverse) + (end[0] * fraction);
        ret[1] = (start[1] * fractionInverse) + (end[1] * fraction);
        ret[2] = (start[2] * fractionInverse) + (end[2] * fraction);
        return ret;
    }

    private static float pivotRgb(float n) {
        return n > 0.04045F ? (float)Math.pow((n + 0.055F) / 1.055F, 2.4F) : n / 12.92F;
    }

    private static float pivotRgbInverse(float n) {
        return n > 0.0031308F ? 1.055F * (float)Math.pow(n, 1F / 2.4F) - 0.055F : 12.92F * n;
    }

    public static Color xyzToRgb(float[] xyz) {
        float x = xyz[0];
        float y = xyz[1];
        float z = xyz[2];

        // (Observer = 2°, Illuminant = D65)
        float r = pivotRgbInverse(x * 3.240479F + y * -1.537150F + z * -0.498535F);
        float g = pivotRgbInverse(x * -0.969256F + y * 1.875992F + z * 0.041556F);
        float b = pivotRgbInverse(x * 0.055648F + y * -0.204043F + z * 1.057311F);

        return new Color(Math.round(r * 255), Math.round(g * 255), Math.round(b * 255));
    }

    public static float[] rgbToXyz(Color color) {
        float[] xyz = new float[3];
        float r = pivotRgb(color.getRed() / 255F);
        float g = pivotRgb(color.getGreen() / 255F);
        float b = pivotRgb(color.getBlue() / 255F);

        // (Observer = 2°, Illuminant = D65)
        xyz[0] = r * 0.412453F + g * 0.357580F + b * 0.180423F;
        xyz[1] = r * 0.212671F + g * 0.715160F + b * 0.072169F;
        xyz[2] = r * 0.019334F + g * 0.119193F + b * 0.950227F;
        
        return xyz;
    }
    
    private static float pivotLab(float n) {
        return n > 0.008856F ? (float)Math.pow(n, 1F / 3F) : (1F / 3F) * (float)Math.pow(29F / 6F, 2) * n + (4F / 29F);
    }

    private static float pivotLabInverse(float n) {
        return n > 6F / 29F ? (float)Math.pow(n, 3) : 3F * (float)Math.pow(6F / 29F, 2) * (n - (4F / 29F));
    }

    public static float[] xyzToLab(float[] xyz) {
        float[] lab = new float[3];
        lab[0] = 116 * pivotLab(xyz[1] / xyzWhiteRef[1]) - 16;
        lab[1] = 500 * (pivotLab(xyz[0] / xyzWhiteRef[0]) - pivotLab(xyz[1] / xyzWhiteRef[1]));
        lab[2] = 200 * (pivotLab(xyz[1] / xyzWhiteRef[1]) - pivotLab(xyz[2] / xyzWhiteRef[2]));
        return lab;
    }

    public static float[] labToXyz(float[] lab) {
        float[] xyz = new float[3];
        float p = (lab[0] + 16F) / 116F;
        xyz[0] = xyzWhiteRef[0] * pivotLabInverse(p + (lab[1] / 500F));
        xyz[1] = xyzWhiteRef[1] * pivotLabInverse(p);
        xyz[2] = xyzWhiteRef[2] * pivotLabInverse(p - (lab[2] / 200F));
        return xyz;
    }
}
