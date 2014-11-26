package com.techjar.cubedesigner.util;

import java.io.File;

/**
 *
 * @author Techjar
 */
public final class OperatingSystem {
    private static final Type osType;
    static {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.indexOf("win") >= 0) osType = Type.WINDOWS;
        else if (os.indexOf("mac") >= 0) osType = Type.MAC;
        else if (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0) osType = Type.LINUX;
        else if (os.indexOf("sunos") >= 0 || os.indexOf("solaris") >= 0) osType = Type.SOLARIS;
        else osType = Type.UNKNOWN;
    }
    
    public static boolean isWindows() {
        return osType == Type.WINDOWS;
    }

    public static boolean isMac() {
        return osType == Type.MAC;
    }

    public static boolean isLinux() {
        return osType == Type.LINUX;
    }

    public static boolean isSolaris() {
        return osType == Type.SOLARIS;
    }
    
    public static boolean isUnknown() {
        return osType == Type.UNKNOWN;
    }
    
    public static Type getType() {
        return osType;
    }
    
    public static String getTypeName() {
        switch (osType) {
            case WINDOWS: return "Windows";
            case MAC: return "Mac OS X";
            case LINUX: return "Linux/Unix";
            case SOLARIS: return "Solaris";
            default: return "";
        }
    }
    
    public static String getTypeString() {
        switch (osType) {
            case WINDOWS: return "windows";
            case MAC: return "macosx";
            case LINUX: return "linux";
            case SOLARIS: return "solaris";
            default: return "unknown";
        }
    }
    
    public static File getDataDirectory(String appName) {
        String userHome = System.getProperty("user.home", ".");
        File directory;
        switch (osType) {
            case WINDOWS:
                String appData = System.getenv("APPDATA");
                if (appData != null) directory = new File(appData, "." + appName + '/');
                else directory = new File(userHome, '.' + appName + '/');
                break;
            case MAC:
                directory = new File(userHome, "Library/Application Support/" + appName);
                break;
            case LINUX:
            case SOLARIS:
                directory = new File(userHome, '.' + appName + '/');
                break;
            default:
                directory = new File(userHome, appName + '/');
                break;
        }
        if (!directory.exists() && !directory.mkdirs()) throw new RuntimeException("The data directory could not be created: " + directory);
        return directory;
    }
    
    
    public enum Type {
        WINDOWS,
        MAC,
        LINUX,
        SOLARIS,
        UNKNOWN
    }
}
