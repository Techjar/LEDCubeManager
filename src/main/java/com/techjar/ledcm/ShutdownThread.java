package com.techjar.ledcm;

/**
 *
 * @author Techjar
 */
public class ShutdownThread extends Thread {
    @Override
    public void run() {
        LEDCubeManager.getInstance().shutdown();
    }
}
