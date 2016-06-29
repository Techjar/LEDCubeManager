package com.techjar.ledcm.util.logging;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 *
 * @author Techjar
 */
public final class LogHandler extends Handler {
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    private PrintStream stream;
    private File file;
    private PrintStream systemOut;
    
    
    public LogHandler(File file) throws FileNotFoundException {
        super();
        this.file = file;
        this.setFormatter(new LogFormatter());
        this.openStream();
    }
    
    @Override
    public void publish(LogRecord record) {
        checkDateChanged();
        String line = getFormatter().format(record);
        stream.println(line);
        if (systemOut == null) System.out.println(line);
        else systemOut.println(line);
    }
    
    @Override
    public void flush() {
        stream.flush();
    }
    
    @Override
    public void close() {
        stream.close();
    }

    public PrintStream getSystemOut() {
        return systemOut;
    }

    public LogHandler setSystemOut(PrintStream systemOut) {
        this.systemOut = systemOut;
        return this;
    }

    public boolean checkDateChanged() {
        File newFile = new File(LogHelper.getDirectory(), new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime()) + ".txt");
        if (newFile.equals(this.file)) return false;
        this.file = newFile;
        this.openStream();
        return true;
    }

    public void openStream() {
        if (this.stream != null) {
            stream.flush();
            stream.close();
        }
        try { this.stream = new PrintStream(new FileOutputStream(this.file, true)); }
        catch (FileNotFoundException ex) { } // Meh
    }
    
    public class LogFormatter extends Formatter {
        public LogFormatter() {
            super();
        }
        
        @Override
        public String format(LogRecord record) {
            try {
                StringBuilder sb = new StringBuilder();
                sb.append('[').append(dateFormat.format(Calendar.getInstance().getTime())).append(']');
                sb.append(' ').append('[').append(record.getLevel().getLocalizedName()).append(']');
                sb.append(' ').append(formatMessage(record));
                return sb.toString();
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
            return record.getMessage();
        }
    }
}
