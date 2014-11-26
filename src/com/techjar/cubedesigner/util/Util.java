package com.techjar.cubedesigner.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.techjar.cubedesigner.CubeDesigner;
import com.techjar.cubedesigner.util.json.ShapeInfo;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.FloatBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterOutputStream;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Controller;
import org.lwjgl.input.Mouse;
import org.newdawn.slick.geom.Circle;
import org.newdawn.slick.geom.Ellipse;
import org.newdawn.slick.geom.Point;
import org.newdawn.slick.geom.Polygon;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.RoundedRectangle;
import org.newdawn.slick.geom.Shape;
import org.newdawn.slick.geom.Vector2f;

/**
 *
 * @author Techjar
 */
public final class Util {
    private static final Map<String, ShapeInfo> shapeCache = new HashMap<>();
    public static final Gson GSON = new GsonBuilder().create();

    private Util() {
    }

    public static boolean isValidCharacter(char ch) {
        return ch >= 32 && ch <= 126;
    }
    
    public static String stackTraceToString(Throwable throwable) {
        StringWriter stackTrace = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stackTrace));
        return stackTrace.toString();
    }
    
    public static org.newdawn.slick.Color convertColor(org.lwjgl.util.Color color) {
        return new org.newdawn.slick.Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }
    
    public static org.lwjgl.util.Color convertColor(org.newdawn.slick.Color color) {
        return new org.lwjgl.util.Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    public static Vector2f convertVector2(Vector2 vector) {
        return new Vector2f(vector.getX(), vector.getY());
    }

    public static Vector2 convertVector2f(Vector2f vector) {
        return new Vector2(vector.getX(), vector.getY());
    }
    
    public static org.lwjgl.util.Color addColors(org.lwjgl.util.Color color1, org.lwjgl.util.Color color2) {
        return new org.lwjgl.util.Color(MathHelper.clamp(color1.getRed() + color2.getRed(), 0, 255), MathHelper.clamp(color1.getGreen() + color2.getGreen(), 0, 255), MathHelper.clamp(color1.getBlue() + color2.getBlue(), 0, 255));
    }

    public static org.lwjgl.util.Color subtractColors(org.lwjgl.util.Color color1, org.lwjgl.util.Color color2) {
        return new org.lwjgl.util.Color(MathHelper.clamp(color1.getRed() - color2.getRed(), 0, 255), MathHelper.clamp(color1.getGreen() - color2.getGreen(), 0, 255), MathHelper.clamp(color1.getBlue() - color2.getBlue(), 0, 255));
    }

    public static float getAxisValue(Controller con, String name) {
        if (name == null) return 0;
        for (int i = 0; i < con.getAxisCount(); i++) {
            if (name.equals(con.getAxisName(i))) return con.getAxisValue(i);
        }
        return 0;
    }

    public static int getMouseX() {
        //return (int)(Mouse.getX() / ((double)canvas.getWidth() / (double)displayMode.getWidth()));
        return Mouse.getX();
    }

    public static int getMouseY() {
        //return (int)((canvas.getHeight() - Mouse.getY()) / ((double)canvas.getHeight() / (double)displayMode.getHeight()));
        return (int)(CubeDesigner.getHeight() - Mouse.getY() - 1);
    }

    public static Vector2 getMousePos() {
        return new Vector2(getMouseX(), getMouseY());
    }

    public static Vector2 getMouseCenterOffset() {
        return new Vector2(getMouseX() - CubeDesigner.getWidth() / 2, getMouseY() - CubeDesigner.getHeight() / 2 + 1);
    }

    public static Shape getMouseHitbox() {
        return new Point(getMouseX(), getMouseY());
    }

    /**
     * Will parse a valid IPv4/IPv6 address and port, may return garbage for invalid address formats. If no port was parsed it will be -1.
     */
    public static IPInfo parseIPAddress(String str) throws UnknownHostException {
        String ip;
        int port = -1;
        boolean ipv6 = false;
        if (str.indexOf(':') != -1) {
            if (str.indexOf('[') != -1 && str.indexOf(']') != -1) {
                ip = str.substring(1, str.indexOf(']'));
                port = Integer.parseInt(str.substring(str.indexOf(']') + 2));
                ipv6 = true;
            } else if (str.indexOf(':') == str.lastIndexOf(':')) {
                ip = str.substring(0, str.indexOf(':'));
                port = Integer.parseInt(str.substring(str.indexOf(':') + 1));
            } else ip = str;
        } else ip = str;
        return new IPInfo(InetAddress.getByName(ip), port, ipv6);
    }

    public static String getFileMD5(File file) throws IOException, NoSuchAlgorithmException {
        @Cleanup FileInputStream fis = new FileInputStream(file);
        byte[] bytes = new byte[(int)file.length()];
        fis.read(bytes);
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(bytes);
        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < digest.length; i++) {
            sb.append(Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

    public static String getFileMD5(String file) throws IOException, NoSuchAlgorithmException {
        return getFileMD5(new File(file));
    }

    @SneakyThrows(FileNotFoundException.class)
    public static Shape loadShape(String file) {
        ShapeInfo info = shapeCache.get(file);
        if (info == null) {
            info = Util.GSON.fromJson(new FileReader(new File("resources/shapes/" + file + ".shape")), ShapeInfo.class);
            shapeCache.put(file, info);
        }

        switch (info.type.toLowerCase()) {
            case "circle":
                return new Circle(0, 0, info.radius);
            case "ellipse":
                return new Ellipse(0, 0, info.radius1, info.radius2);
            case "point":
                return new Point(0, 0);
            case "polygon":
                if (info.points.length % 2 != 0) throw new IllegalArgumentException("Invalid point array, must have even number of elements");
                float[] points = new float[info.points.length];
                for (int i = 0; i < points.length; i += 2) {
                    points[i] = info.points[i] + info.pointOffsetX;
                    points[i + 1] = info.points[i + 1] + info.pointOffsetY;
                }
                Vector2 pos = findMinimumPoint(points);
                Polygon poly = new Polygon(points);
                poly.setX(pos.getX());
                poly.setY(pos.getY());
                return poly;
            case "rectangle":
                Rectangle rect = new Rectangle(0, 0, info.width, info.height);
                rect.setCenterX(0);
                rect.setCenterY(0);
                return rect;
            case "roundedrectangle":
                if (info.cornerFlags != null) {
                    int flags = 0;
                    for (String flag : info.cornerFlags) {
                        switch (flag.toUpperCase()) {
                            case "TOP_LEFT":
                                flags |= RoundedRectangle.TOP_LEFT;
                                break;
                            case "TOP_RIGHT":
                                flags |= RoundedRectangle.TOP_RIGHT;
                                break;
                            case "BOTTOM_LEFT":
                                flags |= RoundedRectangle.BOTTOM_LEFT;
                                break;
                            case "BOTTOM_RIGHT":
                                flags |= RoundedRectangle.BOTTOM_RIGHT;
                                break;
                            case "ALL":
                                flags = RoundedRectangle.ALL;
                                break;
                            default:
                                throw new IllegalArgumentException("Invalid corner flag: " + flag);
                        }
                    }
                    rect = new RoundedRectangle(0, 0, info.width, info.height, info.cornerRadius, 25, flags);
                } else {
                    rect = new RoundedRectangle(0, 0, info.width, info.height, info.cornerRadius);
                }
                rect.setCenterX(0);
                rect.setCenterY(0);
                return rect;
            default:
                throw new IllegalArgumentException("Invalid shape type: " + info.type);
        }
    }

    private static Vector2 findMinimumPoint(float[] points) {
        if (points.length == 0) return new Vector2();
        float minX = points[0];
        float minY = points[1];
        for (int i = 0; i < points.length; i += 2) {
            minX = Math.min(points[i], minX);
            minY = Math.min(points[i + 1], minY);
        }
        return new Vector2(minX, minY);
    }

    /**
     * Compresses the byte array using deflate algorithm.
     */
    public static byte[] compresssBytes(byte[] bytes) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (DeflaterOutputStream dos = new DeflaterOutputStream(out)) {
            dos.write(bytes);
        }
        return out.toByteArray();
    }

    /**
     * Decompresses the byte array using deflate algorithm.
     */
    public static byte[] decompresssBytes(byte[] bytes) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (InflaterOutputStream dos = new InflaterOutputStream(out)) {
            dos.write(bytes);
        }
        return out.toByteArray();
    }

    public static float[] floatListToArray(List<Float> list) {
        float[] array = new float[list.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    public static short floatToShortBits(float fval) {
        int fbits = Float.floatToIntBits(fval);
        int sign = fbits >>> 16 & 0x8000;
        int val = (fbits & 0x7fffffff) + 0x1000;

        if(val >= 0x47800000) {
            if((fbits & 0x7fffffff) >= 0x47800000) {
                if(val < 0x7f800000) return (short)(sign | 0x7c00);
                return (short)(sign | 0x7c00 | (fbits & 0x007fffff) >>> 13);
            }
            return (short)(sign | 0x7bff);
        }
        if(val >= 0x38800000) return (short)(sign | val - 0x38000000 >>> 13);
        if(val < 0x33000000) return (short)(sign);
        val = (fbits & 0x7fffffff) >>> 23;
        return (short)(sign | ((fbits & 0x7fffff | 0x800000) + (0x800000 >>> val - 102) >>> 126 - val));
    }

    public static float shortBitsToFloat(short hbits) {
        int mant = hbits & 0x03ff;
        int exp =  hbits & 0x7c00;
        if(exp == 0x7c00) exp = 0x3fc00;
        else if(exp != 0) {
            exp += 0x1c000;
            if(mant == 0 && exp > 0x1c400) return Float.intBitsToFloat((hbits & 0x8000) << 16 | exp << 13 | 0x3ff);
        }
        else if(mant != 0) {
            exp = 0x1c400;
            do {
                mant <<= 1;
                exp -= 0x400;
            } while((mant & 0x400) == 0);
            mant &= 0x3ff;
        }
        return Float.intBitsToFloat((hbits & 0x8000) << 16 | (exp | mant) << 13);
    }

    public static byte[] readFully(InputStream in) throws IOException {
        @Cleanup ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] bytes = new byte[4096]; int count;
        while ((count = in.read(bytes, 0, bytes.length)) != -1) {
            out.write(bytes, 0, count);
        }
        return out.toByteArray();
    }

    public static String readFile(File file) throws FileNotFoundException, IOException {
        @Cleanup FileInputStream in = new FileInputStream(file);
        byte[] bytes = readFully(in);
        return new String(bytes, "UTF-8");
    }

    public static long microTime() {
        return System.nanoTime() / 1000L;
    }

    public static long milliTime() {
        return System.nanoTime() / 1000000L;
    }

    public static Rectangle clipRectangle(Rectangle toClip, Rectangle clipTo) {
        if (!toClip.intersects(clipTo)) return new Rectangle(0, 0, 0, 0);
        float newX = MathHelper.clamp(toClip.getX(), clipTo.getX(), clipTo.getMaxX());
        float newY = MathHelper.clamp(toClip.getY(), clipTo.getY(), clipTo.getMaxY());
        float newWidth = MathHelper.clamp(toClip.getWidth(), 0, clipTo.getWidth() - (newX - clipTo.getX()));
        float newHeight = MathHelper.clamp(toClip.getHeight(), 0, clipTo.getHeight() - (newY - clipTo.getY()));
        return new Rectangle(newX, newY, newWidth, newHeight);
    }

    public static long bytesToMB(long bytes) {
        return bytes / 1048576;
    }

    public static String bytesToMBString(long bytes) {
        return bytesToMB(bytes) + " MB";
    }

    public static int getNextPowerOfTwo(int number) {
        int ret = Integer.highestOneBit(number);
        return ret < number ? ret << 1 : ret;
    }

    public static boolean isPowerOfTwo(int number) {
        return (number != 0) && (number & (number - 1)) == 0;
    }

    public static final class IPInfo {
        private InetAddress address;
        private int port;
        private boolean ipv6;

        private IPInfo(InetAddress address, int port, boolean ipv6) {
            this.address = address;
            this.port = port;
            this.ipv6 = ipv6;
        }

        public InetAddress getAddress() {
            return address;
        }

        public int getPort() {
            return port;
        }

        public boolean isIPv6() {
            return ipv6;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final IPInfo other = (IPInfo)obj;
            if (this.address != other.address && (this.address == null || !this.address.equals(other.address))) {
                return false;
            }
            if (this.port != other.port) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 67 * hash + (this.address != null ? this.address.hashCode() : 0);
            hash = 67 * hash + this.port;
            return hash;
        }

        @Override
        public String toString() {
            return port < 0 ? address.getHostAddress() : ipv6 ? '[' + address.getHostAddress() + "]:" + port : address.getHostAddress() + ':' + port;
        }
    }
}
