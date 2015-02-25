
package com.techjar.ledcm;

/**
 *
 * @author Techjar
 */
public class LongSleeperThread extends Thread {
    private static boolean started;
    
    private LongSleeperThread() {
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException ex) { // Whatever, it's daemon anyways
                ex.printStackTrace();
            }
        }
    }

    public static void startSleeper() {
        if (started) return;
        Thread thread = new LongSleeperThread();
        thread.setName("Long Sleeper");
        thread.setDaemon(true);
        thread.start();
    }
}
