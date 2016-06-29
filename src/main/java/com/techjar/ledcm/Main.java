
package com.techjar.ledcm;

import com.techjar.ledcm.util.OperatingSystem;
import com.techjar.ledcm.util.Util;
import java.io.File;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author Techjar
 */
public class Main {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (OperatingSystem.isUnknown()) {
            System.out.println("Unsupported OS detected, exiting...");
            System.exit(0);
        }
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        try {
            File workingDir = new File(System.getProperty("user.dir"));
            File natives = new File(workingDir, "build/natives/" + OperatingSystem.getTypeString());
            //Util.addLibraryPath(new File(workingDir, "build/webp-native/x" + OperatingSystem.getJavaArch()).getPath());
            System.setProperty("org.lwjgl.librarypath", natives.getPath());
            System.setProperty("net.java.games.input.librarypath", natives.getPath());

            LEDCubeManager ledcm = new LEDCubeManager(args);
            ledcm.start();
            System.exit(0);
        }
        catch (Throwable ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
    }
}
