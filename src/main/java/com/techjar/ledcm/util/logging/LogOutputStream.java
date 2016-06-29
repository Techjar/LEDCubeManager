package com.techjar.ledcm.util.logging;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Techjar
 */
public class LogOutputStream extends ByteArrayOutputStream {
    private final Logger logger;
    private final Level level;
    private final String lineSeparator = System.getProperty("line.separator");
    
    
    public LogOutputStream(Logger logger, Level level) {
        super();
        this.logger = logger;
        this.level = level;
    }
    
    @Override
    public void flush() throws IOException {
        synchronized(this) {
            super.flush();
            String record = this.toString();
            super.reset();
            if (record.length() > 0 && !record.equals(lineSeparator) && !record.toLowerCase().contains("failed to poll device")) {
                logger.log(level, record);
            }
        }
    }
}
