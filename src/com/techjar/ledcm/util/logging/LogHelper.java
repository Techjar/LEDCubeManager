
package com.techjar.ledcm.util.logging;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Logger;
import java.util.logging.Level;
import lombok.Getter;

/**
 *
 * @author Techjar
 */
public final class LogHelper {
    private static Logger logger;
    private static Level logLevel = Level.CONFIG;
    @Getter private static PrintStream realSystemOut;
    @Getter private static PrintStream realSystemErr;
    @Getter private static File directory;

    private LogHelper() {
    }

    public static void init(File dir) {
        if (logger != null) throw new IllegalStateException("Already initialized!");
        directory = dir;
        realSystemOut = System.out;
        realSystemErr = System.err;
        logger = Logger.getLogger("JFOS2");
        logger.setLevel(logLevel);
        try {
            directory.mkdirs();
            logger.addHandler(new LogHandler(new File(directory, new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime()) + ".txt")).setSystemOut(System.out));
            logger.setUseParentHandlers(false);
            System.setOut(new PrintStream(new LogOutputStream(logger, Level.INFO), true));
            System.setErr(new PrintStream(new LogOutputStream(logger, Level.SEVERE), true));
        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
    }

    public static Level getLevel() {
        if (logger == null) return logLevel;
        return logger.getLevel();
    }

    public static void setLevel(Level level) {
        if (logger == null) logLevel = level;
        else logger.setLevel(level);
    }

    public static void error(Object message, Throwable error) {
        logger.log(Level.SEVERE, message.toString(), error);
    }

    public static void severe(Object message, Object... params) {
        logger.log(Level.SEVERE, String.format(message.toString(), params));
    }

    public static void warning(Object message, Object... params) {
        logger.log(Level.WARNING, String.format(message.toString(), params));
    }

    public static void info(Object message, Object... params) {
        logger.log(Level.INFO, String.format(message.toString(), params));
    }

    public static void config(Object message, Object... params) {
        logger.log(Level.CONFIG, String.format(message.toString(), params));
    }

    public static void fine(Object message, Object... params) {
        logger.log(Level.FINE, String.format(message.toString(), params));
    }

    public static void finer(Object message, Object... params) {
        logger.log(Level.FINER, String.format(message.toString(), params));
    }

    public static void finest(Object message, Object... params) {
        logger.log(Level.FINEST, String.format(message.toString(), params));
    }
}
