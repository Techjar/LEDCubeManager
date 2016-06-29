
package com.techjar.ledcm.hardware.tcp.packet;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.gui.screen.ScreenMainControl;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 *
 * @author Techjar
 */
public class PacketSetColorPicker extends Packet {
    private float red;
    private float green;
    private float blue;

    public PacketSetColorPicker() {
    }

    public PacketSetColorPicker(float red, float green, float blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    @Override
    public void readData(DataInputStream stream) throws IOException {
        red = stream.readFloat();
        green = stream.readFloat();
        blue = stream.readFloat();
    }

    @Override
    public void writeData(DataOutputStream stream) throws IOException {
        stream.writeFloat(red);
        stream.writeFloat(green);
        stream.writeFloat(blue);
    }

    @Override
    public int getRequiredCapabilities() {
        return Capabilities.CONTROL_DATA;
    }

    @Override
    public void process() {
        ScreenMainControl screen = LEDCubeManager.getInstance().getScreenMainControl();
        screen.redColorSlider.setValue(red);
        screen.greenColorSlider.setValue(green);
        screen.blueColorSlider.setValue(blue);
    }
}
