
package com.techjar.ledcm.gui.screen;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.RenderHelper;
import com.techjar.ledcm.gui.GUIAlignment;
import com.techjar.ledcm.gui.GUIBackground;
import com.techjar.ledcm.gui.GUIButton;
import com.techjar.ledcm.gui.GUICallback;
import com.techjar.ledcm.gui.GUIComboBox;
import com.techjar.ledcm.gui.GUILabel;
import com.techjar.ledcm.gui.GUIRadioButton;
import com.techjar.ledcm.gui.GUISlider;
import com.techjar.ledcm.gui.GUITextField;
import com.techjar.ledcm.gui.GUIWindow;
import com.techjar.ledcm.hardware.LEDManager;
import com.techjar.ledcm.hardware.animation.Animation;
import com.techjar.ledcm.hardware.animation.AnimationSequence;
import com.techjar.ledcm.util.Constants;
import com.techjar.ledcm.util.Dimension3D;
import com.techjar.ledcm.util.PrintStreamRelayer;
import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;
import org.lwjgl.util.Color;
import org.lwjgl.util.ReadableColor;
import org.newdawn.slick.UnicodeFont;

/**
 *
 * @author Techjar
 */
public class ScreenMainControl extends Screen {
    public final UnicodeFont font;
    public final GUISlider progressSlider;
    public final GUIWindow layersWindow;
    public final GUIWindow sequenceWindow;
    public final GUISlider layerSlider;
    public final GUIComboBox animComboBox;
    public final GUISlider redColorSlider;
    public final GUISlider greenColorSlider;
    public final GUISlider blueColorSlider;
    public final GUIButton chooseFileBtn;
    public final GUIButton playBtn;
    public final GUIButton pauseBtn;
    public final GUIButton stopBtn;
    public final GUISlider volumeSlider;
    public final GUIButton togglePortBtn;
    public final GUISlider xScaleSlider;
    public final GUILabel xScaleLabel;
    public final GUISlider yScaleSlider;
    public final GUILabel yScaleLabel;
    public final GUISlider zScaleSlider;
    public final GUILabel zScaleLabel;
    public final GUIButton layersBtn;
    public final GUIRadioButton radioOff;
    public final GUILabel radioOffLabel;
    public final GUIRadioButton radioX;
    public final GUILabel radioXLabel;
    public final GUIRadioButton radioY;
    public final GUILabel radioYLabel;
    public final GUIRadioButton radioZ;
    public final GUILabel radioZLabel;
    public final GUIComboBox sequenceComboBox;
    public final GUIButton sequenceLoadBtn;
    public final GUIButton sequencePlayBtn;
    public final GUIButton sequenceStopBtn;
    
    public ScreenMainControl() {
        super();
        final LEDManager ledManager = LEDCubeManager.getLEDManager();
        final Dimension3D ledDim = ledManager.getDimensions();

        font = LEDCubeManager.getFontManager().getFont("chemrea", 30, false, false).getUnicodeFont();
        playBtn = new GUIButton(font, new Color(255, 255, 255), "Play", new GUIBackground(new Color(255, 0, 0), new Color(50, 50, 50), 2));
        playBtn.setParentAlignment(GUIAlignment.BOTTOM_LEFT);
        playBtn.setDimension(100, 40);
        playBtn.setPosition(5, -5);
        playBtn.setClickHandler(new GUICallback() {
            @Override
            public void run() {
                LEDCubeManager.getSpectrumAnalyzer().play();
            }
        });
        container.addComponent(playBtn);
        pauseBtn = new GUIButton(font, new Color(255, 255, 255), "Pause", new GUIBackground(new Color(255, 0, 0), new Color(50, 50, 50), 2));
        pauseBtn.setParentAlignment(GUIAlignment.BOTTOM_LEFT);
        pauseBtn.setDimension(100, 40);
        pauseBtn.setPosition(110, -5);
        pauseBtn.setClickHandler(new GUICallback() {
            @Override
            public void run() {
                LEDCubeManager.getSpectrumAnalyzer().pause();
            }
        });
        container.addComponent(pauseBtn);
        stopBtn = new GUIButton(font, new Color(255, 255, 255), "Stop", new GUIBackground(new Color(255, 0, 0), new Color(50, 50, 50), 2));
        stopBtn.setParentAlignment(GUIAlignment.BOTTOM_LEFT);
        stopBtn.setDimension(100, 40);
        stopBtn.setPosition(215, -5);
        stopBtn.setClickHandler(new GUICallback() {
            @Override
            public void run() {
                LEDCubeManager.getSpectrumAnalyzer().stop();
            }
        });
        container.addComponent(stopBtn);
        chooseFileBtn = new GUIButton(font, new Color(255, 255, 255), "Choose File...", new GUIBackground(new Color(255, 0, 0), new Color(50, 50, 50), 2));
        chooseFileBtn.setParentAlignment(GUIAlignment.BOTTOM_LEFT);
        chooseFileBtn.setDimension(200, 40);
        chooseFileBtn.setPosition(320, -5);
        chooseFileBtn.setClickHandler(new GUICallback() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (LEDCubeManager.isConvertingAudio()) return;
                        int option = LEDCubeManager.getFileChooser().showOpenDialog(LEDCubeManager.getFrame());
                        if (option == JFileChooser.APPROVE_OPTION) {
                            try {
                                File file = LEDCubeManager.getFileChooser().getSelectedFile();
                                LEDCubeManager.getSpectrumAnalyzer().loadFile(file);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }, "File Chooser").start();
            }
        });
        container.addComponent(chooseFileBtn);
        volumeSlider = new GUISlider(new Color(255, 0, 0), new Color(50, 50, 50));
        volumeSlider.setParentAlignment(GUIAlignment.BOTTOM_LEFT);
        volumeSlider.setDimension(150, 30);
        volumeSlider.setPosition(525, -10);
        volumeSlider.setValue(1);
        volumeSlider.setChangeHandler(new GUICallback() {
            @Override
            public void run() {
                LEDCubeManager.getSpectrumAnalyzer().setVolume(volumeSlider.getValue());
            }
        });
        container.addComponent(volumeSlider);
        progressSlider = new GUISlider(new Color(255, 0, 0), new Color(50, 50, 50));
        progressSlider.setParentAlignment(GUIAlignment.BOTTOM_LEFT);
        progressSlider.setDimension(310, 30);
        progressSlider.setPosition(5, -55);
        progressSlider.setChangeHandler(new GUICallback() {
            @Override
            public void run() {
                LEDCubeManager.getSpectrumAnalyzer().setPosition(progressSlider.getValue());
            }
        });
        container.addComponent(progressSlider);
        togglePortBtn = new GUIButton(font, new Color(255, 255, 255), "Toggle Port", new GUIBackground(new Color(255, 0, 0), new Color(50, 50, 50), 2));
        togglePortBtn.setParentAlignment(GUIAlignment.TOP_RIGHT);
        togglePortBtn.setDimension(190, 35);
        togglePortBtn.setPosition(-410, 5);
        togglePortBtn.setClickHandler(new GUICallback() {
            @Override
            public void run() {
                if (LEDCubeManager.getCommThread().isPortOpen()) LEDCubeManager.getCommThread().closePort();
                else LEDCubeManager.getCommThread().openPort();
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
        redColorSlider = new GUISlider(new Color(255, 0, 0), new Color(50, 50, 50));
        redColorSlider.setParentAlignment(GUIAlignment.BOTTOM_RIGHT);
        redColorSlider.setDimension(30, 150);
        redColorSlider.setPosition(-125, -10);
        redColorSlider.setIncrement(1F / 255F);
        redColorSlider.setValue(1);
        redColorSlider.setVertical(true);
        redColorSlider.setShowNotches(false);
        redColorSlider.setChangeHandler(new GUICallback() {
            @Override
            public void run() {
                LEDCubeManager.getPaintColor().setRed(Math.round(255 * redColorSlider.getValue()));
            }
        });
        container.addComponent(redColorSlider);
        greenColorSlider = new GUISlider(new Color(0, 255, 0), new Color(50, 50, 50));
        greenColorSlider.setParentAlignment(GUIAlignment.BOTTOM_RIGHT);
        greenColorSlider.setDimension(30, 150);
        greenColorSlider.setPosition(-90, -10);
        greenColorSlider.setIncrement(1F / 255F);
        greenColorSlider.setValue(1);
        greenColorSlider.setVertical(true);
        greenColorSlider.setShowNotches(false);
        greenColorSlider.setChangeHandler(new GUICallback() {
            @Override
            public void run() {
                LEDCubeManager.getPaintColor().setGreen(Math.round(255 * greenColorSlider.getValue()));
            }
        });
        container.addComponent(greenColorSlider);
        blueColorSlider = new GUISlider(new Color(0, 0, 255), new Color(50, 50, 50));
        blueColorSlider.setParentAlignment(GUIAlignment.BOTTOM_RIGHT);
        blueColorSlider.setDimension(30, 150);
        blueColorSlider.setPosition(-55, -10);
        blueColorSlider.setIncrement(1F / 255F);
        blueColorSlider.setValue(1);
        blueColorSlider.setVertical(true);
        blueColorSlider.setShowNotches(false);
        blueColorSlider.setChangeHandler(new GUICallback() {
            @Override
            public void run() {
                LEDCubeManager.getPaintColor().setBlue(Math.round(255 * blueColorSlider.getValue()));
            }
        });
        container.addComponent(blueColorSlider);
        xScaleSlider = new GUISlider(new Color(255, 0, 0), new Color(50, 50, 50));
        xScaleSlider.setParentAlignment(GUIAlignment.BOTTOM_RIGHT);
        xScaleSlider.setDimension(120, 30);
        xScaleSlider.setPosition(-10, -240);
        xScaleSlider.setIncrement(1F / (ledDim.x - 1));
        xScaleSlider.setChangeHandler(new GUICallback() {
            @Override
            public void run() {
                LEDCubeManager.getPaintSize().setX(Math.round((ledDim.x - 1) * xScaleSlider.getValue()));
            }
        });
        container.addComponent(xScaleSlider);
        xScaleLabel = new GUILabel(font, new Color(255, 255, 255), "X");
        xScaleLabel.setParentAlignment(GUIAlignment.BOTTOM_RIGHT);
        xScaleLabel.setDimension(20, 30);
        xScaleLabel.setPosition(-135, -240);
        container.addComponent(xScaleLabel);
        yScaleSlider = new GUISlider(new Color(255, 0, 0), new Color(50, 50, 50));
        yScaleSlider.setParentAlignment(GUIAlignment.BOTTOM_RIGHT);
        yScaleSlider.setDimension(120, 30);
        yScaleSlider.setPosition(-10, -205);
        yScaleSlider.setIncrement(1F / (ledDim.y - 1));
        yScaleSlider.setChangeHandler(new GUICallback() {
            @Override
            public void run() {
                LEDCubeManager.getPaintSize().setY(Math.round((ledDim.y - 1) * yScaleSlider.getValue()));
            }
        });
        container.addComponent(yScaleSlider);
        yScaleLabel = new GUILabel(font, new Color(255, 255, 255), "Y");
        yScaleLabel.setParentAlignment(GUIAlignment.BOTTOM_RIGHT);
        yScaleLabel.setDimension(20, 30);
        yScaleLabel.setPosition(-135, -205);
        container.addComponent(yScaleLabel);
        zScaleSlider = new GUISlider(new Color(255, 0, 0), new Color(50, 50, 50));
        zScaleSlider.setParentAlignment(GUIAlignment.BOTTOM_RIGHT);
        zScaleSlider.setDimension(120, 30);
        zScaleSlider.setPosition(-10, -170);
        zScaleSlider.setIncrement(1F / (ledDim.z - 1));
        zScaleSlider.setChangeHandler(new GUICallback() {
            @Override
            public void run() {
                LEDCubeManager.getPaintSize().setZ(Math.round((ledDim.z - 1) * zScaleSlider.getValue()));
            }
        });
        container.addComponent(zScaleSlider);
        zScaleLabel = new GUILabel(font, new Color(255, 255, 255), "Z");
        zScaleLabel.setParentAlignment(GUIAlignment.BOTTOM_RIGHT);
        zScaleLabel.setDimension(20, 30);
        zScaleLabel.setPosition(-135, -170);
        container.addComponent(zScaleLabel);
        layersBtn = new GUIButton(font, new Color(255, 255, 255), "Layers", new GUIBackground(new Color(255, 0, 0), new Color(50, 50, 50), 2));
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

        layersWindow = new GUIWindow(new GUIBackground(new Color(10, 10, 10), new Color(255, 0, 0), 2));
        layersWindow.setDimension(150, 167);
        layersWindow.setPosition(container.getWidth() - layersWindow.getWidth() - 10, container.getHeight() - layersWindow.getHeight() - 320);
        layersWindow.setResizable(false);
        layersWindow.setCloseAction(GUIWindow.HIDE_ON_CLOSE);
        layersWindow.setVisible(false);
        container.addComponent(layersWindow);
        radioOff = new GUIRadioButton(new Color(255, 255, 255), new GUIBackground(new Color(0, 0, 0), new Color(255, 0, 0), 2));
        radioOff.setDimension(30, 30);
        radioOff.setPosition(5, 5);
        radioOff.setSelected(true);
        radioOff.setSelectHandler(new GUICallback() {
            @Override
            public void run() {
                LEDCubeManager.setLayerIsolation(0);
                layerSlider.setEnabled(false);
                layerSlider.setValue(0);
                layerSlider.setIncrement(0);
            }
        });
        layersWindow.addComponent(radioOff);
        radioOffLabel = new GUILabel(font, new Color(255, 255, 255), "Off");
        radioOffLabel.setDimension(60, 30);
        radioOffLabel.setPosition(40, 5);
        radioOff.setLabel(radioOffLabel);
        layersWindow.addComponent(radioOffLabel);
        radioX = new GUIRadioButton(new Color(255, 255, 255), new GUIBackground(new Color(0, 0, 0), new Color(255, 0, 0), 2));
        radioX.setDimension(30, 30);
        radioX.setPosition(5, 40);
        radioX.setSelectHandler(new GUICallback() {
            @Override
            public void run() {
                LEDCubeManager.setLayerIsolation(1);
                layerSlider.setEnabled(true);
                layerSlider.setValue(0);
                layerSlider.setIncrement(1F / (ledDim.x - 1));
                layerSlider.setChangeHandler(new GUICallback() {
                    @Override
                    public void run() {
                        LEDCubeManager.setSelectedLayer(Math.round((ledDim.x - 1) * layerSlider.getValue()));
                    }
                });
            }
        });
        layersWindow.addComponent(radioX);
        radioXLabel = new GUILabel(font, new Color(255, 255, 255), "X");
        radioXLabel.setDimension(20, 30);
        radioXLabel.setPosition(40, 40);
        radioX.setLabel(radioXLabel);
        layersWindow.addComponent(radioXLabel);
        radioY = new GUIRadioButton(new Color(255, 255, 255), new GUIBackground(new Color(0, 0, 0), new Color(255, 0, 0), 2));
        radioY.setDimension(30, 30);
        radioY.setPosition(5, 75);
        radioY.setSelectHandler(new GUICallback() {
            @Override
            public void run() {
                LEDCubeManager.setLayerIsolation(2);
                layerSlider.setEnabled(true);
                layerSlider.setValue(0);
                layerSlider.setIncrement(1F / (ledDim.y - 1));
                layerSlider.setChangeHandler(new GUICallback() {
                    @Override
                    public void run() {
                        LEDCubeManager.setSelectedLayer(Math.round((ledDim.y - 1) * layerSlider.getValue()));
                    }
                });
            }
        });
        layersWindow.addComponent(radioY);
        radioYLabel = new GUILabel(font, new Color(255, 255, 255), "Y");
        radioYLabel.setDimension(20, 30);
        radioYLabel.setPosition(40, 75);
        radioY.setLabel(radioYLabel);
        layersWindow.addComponent(radioYLabel);
        radioZ = new GUIRadioButton(new Color(255, 255, 255), new GUIBackground(new Color(0, 0, 0), new Color(255, 0, 0), 2));
        radioZ.setDimension(30, 30);
        radioZ.setPosition(5, 110);
        radioZ.setSelectHandler(new GUICallback() {
            @Override
            public void run() {
                LEDCubeManager.setLayerIsolation(3);
                layerSlider.setEnabled(true);
                layerSlider.setValue(0);
                layerSlider.setIncrement(1F / (ledDim.z - 1));
                layerSlider.setChangeHandler(new GUICallback() {
                    @Override
                    public void run() {
                        LEDCubeManager.setSelectedLayer(Math.round((ledDim.z - 1) * layerSlider.getValue()));
                    }
                });
            }
        });
        layersWindow.addComponent(radioZ);
        radioZLabel = new GUILabel(font, new Color(255, 255, 255), "Z");
        radioZLabel.setDimension(20, 30);
        radioZLabel.setPosition(40, 110);
        radioZ.setLabel(radioZLabel);
        layersWindow.addComponent(radioZLabel);
        layerSlider = new GUISlider(new Color(255, 0, 0), new Color(50, 50, 50));
        layerSlider.setDimension(30, 125);
        layerSlider.setPosition(105, 10);
        //layerSlider.setIncrement(1F / 7F);
        layerSlider.setVertical(true);
        layerSlider.setEnabled(false);
        /*layerSlider.setChangeHandler(new GUICallback() {
            @Override
            public void run() {
                CubeDesigner.setSelectedLayer(Math.round(7 * layerSlider.getValue()));
            }
        });*/
        layersWindow.addComponent(layerSlider);


        sequenceComboBox = new GUIComboBox(font, new Color(255, 255, 255), new GUIBackground(new Color(0, 0, 0), new Color(255, 0, 0), 2));
        sequenceComboBox.setParentAlignment(GUIAlignment.TOP_CENTER);
        sequenceComboBox.setDimension(300, 35);
        sequenceComboBox.setPosition(0, 10);
        sequenceComboBox.setVisibleItems(2);
        sequenceLoadBtn = new GUIButton(font, new Color(255, 255, 255), "Sequence", new GUIBackground(new Color(255, 0, 0), new Color(50, 50, 50), 2));
        sequenceLoadBtn.setParentAlignment(GUIAlignment.BOTTOM_LEFT);
        sequenceLoadBtn.setDimension(200, 40);
        sequenceLoadBtn.setPosition(320, -50);
        sequenceLoadBtn.setClickHandler(new GUICallback() {
            @Override
            public void run() {
                sequenceWindow.setVisible(!sequenceWindow.isVisible());
                if (sequenceWindow.isVisible()) {
                    sequenceComboBox.setSelectedItem(-1);
                    sequenceComboBox.clearItems();
                    File dir = new File("resources/sequences/");
                    for (File file : dir.listFiles()) {
                        String name = file.getName();
                        if (name.toLowerCase().endsWith(".sequence")) {
                            sequenceComboBox.addItem(name.substring(0, name.lastIndexOf('.')));
                        }
                    }
                }
            }
        });
        container.addComponent(sequenceLoadBtn);
        sequenceWindow = new GUIWindow(new GUIBackground(new Color(10, 10, 10), new Color(255, 0, 0), 2));
        sequenceWindow.setDimension(350, 150);
        sequenceWindow.setPosition(container.getWidth() / 2 - sequenceWindow.getWidth() / 2, container.getHeight() / 2 - sequenceWindow.getHeight() / 2);
        sequenceWindow.setResizable(false);
        sequenceWindow.setCloseAction(GUIWindow.HIDE_ON_CLOSE);
        sequenceWindow.setVisible(false);
        container.addComponent(sequenceWindow);
        sequencePlayBtn = new GUIButton(font, new Color(255, 255, 255), "Start", new GUIBackground(new Color(255, 0, 0), new Color(50, 50, 50), 2));
        sequencePlayBtn.setParentAlignment(GUIAlignment.TOP_CENTER);
        sequencePlayBtn.setDimension(100, 40);
        sequencePlayBtn.setPosition(-55, 65);
        sequencePlayBtn.setClickHandler(new GUICallback() {
            @Override
            public void run() {
                Object item = sequenceComboBox.getSelectedItem();
                if (item != null) {
                    try {
                        File file = new File("resources/sequences/" + item.toString() + ".sequence");
                        AnimationSequence sequence = AnimationSequence.loadFromFile(file);
                        LEDCubeManager.getCommThread().setCurrentSequence(sequence);
                        sequenceWindow.setVisible(false);
                        chooseFileBtn.setEnabled(!sequence.isMusicSynced());
                        progressSlider.setEnabled(!sequence.isMusicSynced());
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        sequenceWindow.addComponent(sequencePlayBtn);
        sequenceStopBtn = new GUIButton(font, new Color(255, 255, 255), "Stop", new GUIBackground(new Color(255, 0, 0), new Color(50, 50, 50), 2));
        sequenceStopBtn.setParentAlignment(GUIAlignment.TOP_CENTER);
        sequenceStopBtn.setDimension(100, 40);
        sequenceStopBtn.setPosition(55, 65);
        sequenceStopBtn.setClickHandler(new GUICallback() {
            @Override
            public void run() {
                LEDCubeManager.getCommThread().setCurrentSequence(null);
                chooseFileBtn.setEnabled(true);
                progressSlider.setEnabled(true);
            }
        });
        sequenceWindow.addComponent(sequenceStopBtn);
        sequenceWindow.addComponent(sequenceComboBox);
        
        animComboBox = new GUIComboBox(font, new Color(255, 255, 255), new GUIBackground(new Color(0, 0, 0), new Color(255, 0, 0), 2));
        animComboBox.setParentAlignment(GUIAlignment.TOP_RIGHT);
        animComboBox.setDimension(400, 35);
        animComboBox.setPosition(-5, 5);
        animComboBox.setVisibleItems(8);
        animComboBox.setChangeHandler(new GUICallback() {
            @Override
            public void run() {
                if (animComboBox.getSelectedItem() != null) {
                    Animation animation = LEDCubeManager.getInstance().getAnimations().get(animComboBox.getSelectedItem().toString());
                    LEDCubeManager.getCommThread().setCurrentAnimation(animation);
                }
            }
        });
        populateAnimationList();
        container.addComponent(animComboBox);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        progressSlider.setValueWithoutNotify(LEDCubeManager.getSpectrumAnalyzer().getPosition());
    }

    @Override
    public void render() {
        super.render();
        RenderHelper.drawSquare(LEDCubeManager.getWidth() - 40 - 10, LEDCubeManager.getHeight() - 150 - 10, 40, 150, LEDCubeManager.getPaintColor());
    }

    public final void populateAnimationList() {
        animComboBox.clearItems();
        for (String name : LEDCubeManager.getInstance().getAnimationNames()) {
            animComboBox.addItem(name);
        }
        animComboBox.setSelectedItem(1);
    }
}
