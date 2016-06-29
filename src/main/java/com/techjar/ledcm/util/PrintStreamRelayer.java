/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.techjar.ledcm.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

/**
 *
 * @author Techjar
 */
public class PrintStreamRelayer extends Thread {
    private InputStream inputStream;
    private PrintStream[] printStreams;

    public PrintStreamRelayer(InputStream inputStream, PrintStream... printStreams) {
        super("Print Stream Relayer");
        this.inputStream = inputStream;
        this.printStreams = printStreams;
    }

    @Override
    public void run() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = br.readLine()) != null) {
                for (PrintStream printStream : printStreams) {
                    printStream.println(line);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
