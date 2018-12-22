
package com.techjar.ledcm;

import com.techjar.ledcm.util.ArgumentParser;
import com.techjar.ledcm.util.OperatingSystem;
import com.techjar.ledcm.util.Util;
import com.techjar.ledcm.util.logging.LogHelper;
import lombok.Getter;

import java.io.File;
import java.util.logging.Level;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author Techjar
 */
public class Main {
	@Getter private static File dataDirectory = OperatingSystem.getDataDirectory("ledstripmanager");
	public static boolean renderFPS;
	public static boolean debugMode;
	public static boolean debugGL;
	public static boolean debugGUI;
	public static boolean wireframe;
	@Getter private static boolean vrMode;
	@Getter private static String serialPortName = "COM3";
	@Getter private static String portHandlerName = "SerialPortHandler";
	@Getter private static String ledManagerName = null;
	@Getter private static String[] ledManagerArgs = new String[0];
	@Getter private static int serverPort = 7545;

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

			ArgumentParser.parse(args, new ArgumentParser.Argument(true, "\nSpecify logging detail level", "--loglevel") {
				@Override
				public void runAction(String parameter) {
					LogHelper.setLevel(Level.parse(parameter));
				}
			}, new ArgumentParser.Argument(false, "\nDisplay frames per second", "--showfps") {
				@Override
				public void runAction(String parameter) {
					renderFPS = true;
				}
			}, new ArgumentParser.Argument(false, "\nDisplay debug output", "--debug") {
				@Override
				public void runAction(String parameter) {
					debugMode = true;
				}
			}, new ArgumentParser.Argument(false, "\nDisplay OpenGL errors", "--debug-gl") {
				@Override
				public void runAction(String parameter) {
					debugGL = true;
				}
			}, new ArgumentParser.Argument(false, "\nRender GUI boxes", "--debug-gui") {
				@Override
				public void runAction(String parameter) {
					debugGUI = true;
				}
			}, new ArgumentParser.Argument(false, "\nEnable wireframe rendering", "--wireframe") {
				@Override
				public void runAction(String parameter) {
					wireframe = true;
				}
			}, new ArgumentParser.Argument(false, "\nEnable virtual reality render mode", "--vr") {
				@Override
				public void runAction(String parameter) {
					vrMode = true;
				}
			}, new ArgumentParser.Argument(true, "<name>\nSpecify serial port name", "--serialport") {
				@Override
				public void runAction(String parameter) {
					serialPortName = parameter;
				}
			}, new ArgumentParser.Argument(true, "<port number>\nSpecify internal TCP server port", "--serverport") {
				@Override
				public void runAction(String parameter) {
					serverPort = Integer.parseInt(parameter);
				}
			}, new ArgumentParser.Argument(true, "<class name>\nSpecify PortHandler class", "--porthandler") {
				@Override
				public void runAction(String parameter) {
					portHandlerName = parameter;
				}
			}, new ArgumentParser.Argument(true, "<class name and constructor parameters (comma-separated)>\nSpecify LEDManager class", "--ledmanager") {
				@Override
				public void runAction(String parameter) {
					String[] split = parameter.split("(?<!,),");
					for (int i = 0; i < split.length; i++) split[i] = split[i].replaceAll(",,", ",");
					ledManagerName = split[0];
					ledManagerArgs = new String[split.length - 1];
					System.arraycopy(split, 1, ledManagerArgs, 0, split.length - 1);
				}
			}, new ArgumentParser.Argument(true, "<directory path>\nSpecify a custom directory for config/logs/etc.", "--datadir") {
				@Override
				public void runAction(String parameter) {
					dataDirectory = new File(parameter);
					if (!dataDirectory.exists()) {
						if (!dataDirectory.mkdirs()) {
							System.out.println("Failed to create directory: " + dataDirectory);
							System.exit(0);
						}
					}
				}
			});

			LEDCubeManager ledcm = new LEDCubeManager();
			ledcm.start();
			System.exit(0);
		} catch (Throwable ex) {
			ex.printStackTrace();
			if (LEDCubeManager.getInstance() != null)
				LEDCubeManager.getInstance().shutdownInternal();
			System.exit(-1);
		}
	}
}
