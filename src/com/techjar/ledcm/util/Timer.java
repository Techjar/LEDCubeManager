
package com.techjar.ledcm.util;

/**
 *
 * @author Techjar
 */
public class Timer {
    private long time;

    public Timer() {
        time = System.nanoTime();
    }

    public void restart() {
        time = System.nanoTime();
    }

    public double getMicroseconds() {
        return (System.nanoTime() - time) / 1000D;
    }

    public double getMilliseconds() {
        return (System.nanoTime() - time) / 1000000D;
    }

    public double getSeconds() {
        return (System.nanoTime() - time) / 1000000000D;
    }

    public double getMinutes() {
        return (System.nanoTime() - time) / (1000000000D * 60D);
    }

    public double getHours() {
        return (System.nanoTime() - time) / (1000000000D * 60D * 60D);
    }
}
