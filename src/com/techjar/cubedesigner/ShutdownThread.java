package com.techjar.cubedesigner;

/**
 *
 * @author Techjar
 */
public class ShutdownThread extends Thread {
    @Override
    public void run() {
        CubeDesigner.getInstance().shutdown();
    }
}
