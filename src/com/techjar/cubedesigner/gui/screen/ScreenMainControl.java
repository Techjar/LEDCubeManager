
package com.techjar.cubedesigner.gui.screen;

import com.techjar.cubedesigner.CubeDesigner;
import com.techjar.cubedesigner.RenderHelper;
import com.techjar.cubedesigner.gui.GUIAlignment;
import com.techjar.cubedesigner.gui.GUIBackground;
import com.techjar.cubedesigner.gui.GUIButton;
import com.techjar.cubedesigner.gui.GUICallback;
import com.techjar.cubedesigner.gui.GUIComboBox;
import com.techjar.cubedesigner.gui.GUILabel;
import com.techjar.cubedesigner.gui.GUIRadioButton;
import com.techjar.cubedesigner.gui.GUISlider;
import com.techjar.cubedesigner.gui.GUITextField;
import com.techjar.cubedesigner.gui.GUIWindow;
import com.techjar.cubedesigner.hardware.animation.Animation;
import java.io.File;
import javax.swing.JFileChooser;
import org.lwjgl.util.Color;
import org.lwjgl.util.ReadableColor;
import org.newdawn.slick.UnicodeFont;

/**
 *
 * @author Techjar
 */
public class ScreenMainControl extends Screen {
    private final GUISlider progressSlider;
    private final GUIWindow layersWindow;
    
    public ScreenMainControl() {
        super();
        UnicodeFont font = CubeDesigner.getFontManager().getFont("chemrea", 30, false, false).getUnicodeFont();
        GUIButton playBtn = new GUIButton(font, new Color(255, 255, 255), "Play", new GUIBackground(new Color(255, 0, 0), new Color(50, 50, 50), 2));
        playBtn.setParentAlignment(GUIAlignment.BOTTOM_LEFT);
        playBtn.setDimension(100, 40);
        playBtn.setPosition(5, -5);
        playBtn.setClickHandler(new GUICallback() {
            @Override
            public void run() {
                CubeDesigner.getSpectrumAnalyzer().play();
            }
        });
        container.addComponent(playBtn);
        GUIButton pauseBtn = new GUIButton(font, new Color(255, 255, 255), "Pause", new GUIBackground(new Color(255, 0, 0), new Color(50, 50, 50), 2));
        pauseBtn.setParentAlignment(GUIAlignment.BOTTOM_LEFT);
        pauseBtn.setDimension(100, 40);
        pauseBtn.setPosition(110, -5);
        pauseBtn.setClickHandler(new GUICallback() {
            @Override
            public void run() {
                CubeDesigner.getSpectrumAnalyzer().pause();
            }
        });
        container.addComponent(pauseBtn);
        GUIButton stopBtn = new GUIButton(font, new Color(255, 255, 255), "Stop", new GUIBackground(new Color(255, 0, 0), new Color(50, 50, 50), 2));
        stopBtn.setParentAlignment(GUIAlignment.BOTTOM_LEFT);
        stopBtn.setDimension(100, 40);
        stopBtn.setPosition(215, -5);
        stopBtn.setClickHandler(new GUICallback() {
            @Override
            public void run() {
                CubeDesigner.getSpectrumAnalyzer().stop();
            }
        });
        container.addComponent(stopBtn);
        GUIButton chooseFileBtn = new GUIButton(font, new Color(255, 255, 255), "Choose File...", new GUIBackground(new Color(255, 0, 0), new Color(50, 50, 50), 2));
        chooseFileBtn.setParentAlignment(GUIAlignment.BOTTOM_LEFT);
        chooseFileBtn.setDimension(200, 40);
        chooseFileBtn.setPosition(320, -5);
        chooseFileBtn.setClickHandler(new GUICallback() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int option = CubeDesigner.getFileChooser().showOpenDialog(CubeDesigner.getFrame());
                        if (option == JFileChooser.APPROVE_OPTION) {
                            File file = CubeDesigner.getFileChooser().getSelectedFile();
                            CubeDesigner.getSpectrumAnalyzer().loadFile(file.getAbsolutePath());
                        }
                    }
                }, "File Chooser Thread").start();
            }
        });
        container.addComponent(chooseFileBtn);
        final GUISlider volumeSlider = new GUISlider(new Color(255, 0, 0), new Color(50, 50, 50));
        volumeSlider.setParentAlignment(GUIAlignment.BOTTOM_LEFT);
        volumeSlider.setDimension(150, 30);
        volumeSlider.setPosition(525, -10);
        volumeSlider.setValue(1);
        volumeSlider.setChangeHandler(new GUICallback() {
            @Override
            public void run() {
                CubeDesigner.getSpectrumAnalyzer().setVolume(volumeSlider.getValue());
            }
        });
        container.addComponent(volumeSlider);
        progressSlider = new GUISlider(new Color(255, 0, 0), new Color(50, 50, 50));
        progressSlider.setParentAlignment(GUIAlignment.BOTTOM_LEFT);
        progressSlider.setDimension(310, 30);
        progressSlider.setPosition(5, -50);
        progressSlider.setChangeHandler(new GUICallback() {
            @Override
            public void run() {
                CubeDesigner.getSpectrumAnalyzer().setPosition(progressSlider.getValue());
            }
        });
        container.addComponent(progressSlider);
        final GUIComboBox animComboBox = new GUIComboBox(font, new Color(255, 255, 255), new GUIBackground(new Color(0, 0, 0), new Color(255, 0, 0), 2));
        animComboBox.setParentAlignment(GUIAlignment.TOP_RIGHT);
        animComboBox.setDimension(400, 35);
        animComboBox.setPosition(-5, 5);
        animComboBox.setVisibleItems(8);
        for (String name : CubeDesigner.getInstance().getAnimationNames()) {
            animComboBox.addItem(name);
        }
        animComboBox.setChangeHandler(new GUICallback() {
            @Override
            public void run() {
                if (animComboBox.getSelectedItem() != null) {
                    Animation animation = CubeDesigner.getInstance().getAnimations().get(animComboBox.getSelectedItem().toString());
                    CubeDesigner.getSerialThread().setCurrentAnimation(animation);
                }
            }
        });
        animComboBox.setSelectedItem(1);
        container.addComponent(animComboBox);
        GUIButton togglePortBtn = new GUIButton(font, new Color(255, 255, 255), "Toggle Port", new GUIBackground(new Color(255, 0, 0), new Color(50, 50, 50), 2));
        togglePortBtn.setParentAlignment(GUIAlignment.TOP_RIGHT);
        togglePortBtn.setDimension(190, 35);
        togglePortBtn.setPosition(-410, 5);
        togglePortBtn.setClickHandler(new GUICallback() {
            @Override
            public void run() {
                if (CubeDesigner.getSerialThread().isPortOpen()) CubeDesigner.getSerialThread().closePort();
                else CubeDesigner.getSerialThread().openPort();
            }
        });
        container.addComponent(togglePortBtn);
        /*GUIButton openPortBtn = new GUIButton(font, new Color(255, 255, 255), "Open", new GUIBackground(new Color(255, 0, 0), new Color(50, 50, 50), 2));
        openPortBtn.setParentAlignment(GUIAlignment.TOP_RIGHT);
        openPortBtn.setDimension(100, 35);
        openPortBtn.setPosition(-515, 5);
        openPortBtn.setClickHandler(new GUICallback() {
            @Override
            public void run() {
                CubeDesigner.getSerialThread().openPort();
            }
        });
        container.addComponent(openPortBtn);*/
        final GUISlider redColorSlider = new GUISlider(new Color(255, 0, 0), new Color(50, 50, 50));
        redColorSlider.setParentAlignment(GUIAlignment.BOTTOM_RIGHT);
        redColorSlider.setDimension(30, 150);
        redColorSlider.setPosition(-125, -10);
        redColorSlider.setIncrement(1F / 16F);
        redColorSlider.setValue(1);
        redColorSlider.setVertical(true);
        redColorSlider.setShowNotches(false);
        redColorSlider.setChangeHandler(new GUICallback() {
            @Override
            public void run() {
                CubeDesigner.getPaintColor().setRed(Math.round(255 * redColorSlider.getValue()));
            }
        });
        container.addComponent(redColorSlider);
        final GUISlider greenColorSlider = new GUISlider(new Color(0, 255, 0), new Color(50, 50, 50));
        greenColorSlider.setParentAlignment(GUIAlignment.BOTTOM_RIGHT);
        greenColorSlider.setDimension(30, 150);
        greenColorSlider.setPosition(-90, -10);
        greenColorSlider.setIncrement(1F / 16F);
        greenColorSlider.setValue(1);
        greenColorSlider.setVertical(true);
        greenColorSlider.setShowNotches(false);
        greenColorSlider.setChangeHandler(new GUICallback() {
            @Override
            public void run() {
                CubeDesigner.getPaintColor().setGreen(Math.round(255 * greenColorSlider.getValue()));
            }
        });
        container.addComponent(greenColorSlider);
        final GUISlider blueColorSlider = new GUISlider(new Color(0, 0, 255), new Color(50, 50, 50));
        blueColorSlider.setParentAlignment(GUIAlignment.BOTTOM_RIGHT);
        blueColorSlider.setDimension(30, 150);
        blueColorSlider.setPosition(-55, -10);
        blueColorSlider.setIncrement(1F / 16F);
        blueColorSlider.setValue(1);
        blueColorSlider.setVertical(true);
        blueColorSlider.setShowNotches(false);
        blueColorSlider.setChangeHandler(new GUICallback() {
            @Override
            public void run() {
                CubeDesigner.getPaintColor().setBlue(Math.round(255 * blueColorSlider.getValue()));
            }
        });
        container.addComponent(blueColorSlider);
        final GUISlider xScaleSlider = new GUISlider(new Color(255, 0, 0), new Color(50, 50, 50));
        xScaleSlider.setParentAlignment(GUIAlignment.BOTTOM_RIGHT);
        xScaleSlider.setDimension(120, 30);
        xScaleSlider.setPosition(-10, -240);
        xScaleSlider.setIncrement(1F / 7F);
        xScaleSlider.setChangeHandler(new GUICallback() {
            @Override
            public void run() {
                CubeDesigner.getPaintSize().setX(Math.round(7 * xScaleSlider.getValue()));
            }
        });
        container.addComponent(xScaleSlider);
        GUILabel xScaleLabel = new GUILabel(font, new Color(255, 255, 255), "X");
        xScaleLabel.setParentAlignment(GUIAlignment.BOTTOM_RIGHT);
        xScaleLabel.setDimension(20, 30);
        xScaleLabel.setPosition(-135, -240);
        container.addComponent(xScaleLabel);
        final GUISlider yScaleSlider = new GUISlider(new Color(255, 0, 0), new Color(50, 50, 50));
        yScaleSlider.setParentAlignment(GUIAlignment.BOTTOM_RIGHT);
        yScaleSlider.setDimension(120, 30);
        yScaleSlider.setPosition(-10, -205);
        yScaleSlider.setIncrement(1F / 7F);
        yScaleSlider.setChangeHandler(new GUICallback() {
            @Override
            public void run() {
                CubeDesigner.getPaintSize().setY(Math.round(7 * yScaleSlider.getValue()));
            }
        });
        container.addComponent(yScaleSlider);
        GUILabel yScaleLabel = new GUILabel(font, new Color(255, 255, 255), "Y");
        yScaleLabel.setParentAlignment(GUIAlignment.BOTTOM_RIGHT);
        yScaleLabel.setDimension(20, 30);
        yScaleLabel.setPosition(-135, -205);
        container.addComponent(yScaleLabel);
        final GUISlider zScaleSlider = new GUISlider(new Color(255, 0, 0), new Color(50, 50, 50));
        zScaleSlider.setParentAlignment(GUIAlignment.BOTTOM_RIGHT);
        zScaleSlider.setDimension(120, 30);
        zScaleSlider.setPosition(-10, -170);
        zScaleSlider.setIncrement(1F / 7F);
        zScaleSlider.setChangeHandler(new GUICallback() {
            @Override
            public void run() {
                CubeDesigner.getPaintSize().setZ(Math.round(7 * zScaleSlider.getValue()));
            }
        });
        container.addComponent(zScaleSlider);
        GUILabel zScaleLabel = new GUILabel(font, new Color(255, 255, 255), "Z");
        zScaleLabel.setParentAlignment(GUIAlignment.BOTTOM_RIGHT);
        zScaleLabel.setDimension(20, 30);
        zScaleLabel.setPosition(-135, -170);
        container.addComponent(zScaleLabel);
        GUIButton layersBtn = new GUIButton(font, new Color(255, 255, 255), "Layers", new GUIBackground(new Color(255, 0, 0), new Color(50, 50, 50), 2));
        layersBtn.setParentAlignment(GUIAlignment.BOTTOM_RIGHT);
        layersBtn.setDimension(120, 35);
        layersBtn.setPosition(-10, -275);
        layersBtn.setClickHandler(new GUICallback() {
            @Override
            public void run() {
                layersWindow.setVisible(!layersWindow.isVisible());
            }
        });
        container.addComponent(layersBtn);

        layersWindow = new GUIWindow(new GUIBackground(new Color(0, 0, 0), new Color(255, 0, 0), 2));
        layersWindow.setDimension(150, 167);
        layersWindow.setPosition(container.getWidth() - layersWindow.getWidth() - 10, container.getHeight() - layersWindow.getHeight() - 320);
        layersWindow.setResizable(false);
        layersWindow.setCloseAction(GUIWindow.HIDE_ON_CLOSE);
        layersWindow.setVisible(false);
        container.addComponent(layersWindow);
        GUIRadioButton radioOff = new GUIRadioButton(new Color(255, 255, 255), new GUIBackground(new Color(0, 0, 0), new Color(255, 0, 0), 2));
        radioOff.setDimension(30, 30);
        radioOff.setPosition(5, 5);
        radioOff.setSelected(true);
        radioOff.setSelectHandler(new GUICallback() {
            @Override
            public void run() {
                CubeDesigner.setLayerIsolation(0);
            }
        });
        layersWindow.addComponent(radioOff);
        GUILabel radioOffLabel = new GUILabel(font, new Color(255, 255, 255), "Off");
        radioOffLabel.setDimension(60, 30);
        radioOffLabel.setPosition(40, 5);
        radioOff.setLabel(radioOffLabel);
        layersWindow.addComponent(radioOffLabel);
        GUIRadioButton radioX = new GUIRadioButton(new Color(255, 255, 255), new GUIBackground(new Color(0, 0, 0), new Color(255, 0, 0), 2));
        radioX.setDimension(30, 30);
        radioX.setPosition(5, 40);
        radioX.setSelectHandler(new GUICallback() {
            @Override
            public void run() {
                CubeDesigner.setLayerIsolation(1);
            }
        });
        layersWindow.addComponent(radioX);
        GUILabel radioXLabel = new GUILabel(font, new Color(255, 255, 255), "X");
        radioXLabel.setDimension(20, 30);
        radioXLabel.setPosition(40, 40);
        radioX.setLabel(radioXLabel);
        layersWindow.addComponent(radioXLabel);
        GUIRadioButton radioY = new GUIRadioButton(new Color(255, 255, 255), new GUIBackground(new Color(0, 0, 0), new Color(255, 0, 0), 2));
        radioY.setDimension(30, 30);
        radioY.setPosition(5, 75);
        radioY.setSelectHandler(new GUICallback() {
            @Override
            public void run() {
                CubeDesigner.setLayerIsolation(2);
            }
        });
        layersWindow.addComponent(radioY);
        GUILabel radioYLabel = new GUILabel(font, new Color(255, 255, 255), "Y");
        radioYLabel.setDimension(20, 30);
        radioYLabel.setPosition(40, 75);
        radioY.setLabel(radioYLabel);
        layersWindow.addComponent(radioYLabel);
        GUIRadioButton radioZ = new GUIRadioButton(new Color(255, 255, 255), new GUIBackground(new Color(0, 0, 0), new Color(255, 0, 0), 2));
        radioZ.setDimension(30, 30);
        radioZ.setPosition(5, 110);
        radioZ.setSelectHandler(new GUICallback() {
            @Override
            public void run() {
                CubeDesigner.setLayerIsolation(3);
            }
        });
        layersWindow.addComponent(radioZ);
        GUILabel radioZLabel = new GUILabel(font, new Color(255, 255, 255), "Z");
        radioZLabel.setDimension(20, 30);
        radioZLabel.setPosition(40, 110);
        radioZ.setLabel(radioZLabel);
        layersWindow.addComponent(radioZLabel);
        final GUISlider layerSlider = new GUISlider(new Color(255, 0, 0), new Color(50, 50, 50));
        layerSlider.setDimension(30, 125);
        layerSlider.setPosition(105, 10);
        layerSlider.setIncrement(1F / 7F);
        layerSlider.setVertical(true);
        layerSlider.setChangeHandler(new GUICallback() {
            @Override
            public void run() {
                CubeDesigner.setSelectedLayer(Math.round(7 * layerSlider.getValue()));
            }
        });
        layersWindow.addComponent(layerSlider);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        progressSlider.setValueWithoutNotify(CubeDesigner.getSpectrumAnalyzer().getPosition());
    }

    @Override
    public void render() {
        super.render();
        RenderHelper.drawSquare(CubeDesigner.getWidth() - 40 - 10, CubeDesigner.getHeight() - 150 - 10, 40, 150, CubeDesigner.getPaintColor());
    }
}
