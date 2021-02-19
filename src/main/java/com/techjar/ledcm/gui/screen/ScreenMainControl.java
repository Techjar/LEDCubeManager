
package com.techjar.ledcm.gui.screen;

import com.google.common.collect.Lists;
import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.Main;
import com.techjar.ledcm.gui.*;
import com.techjar.ledcm.render.RenderHelper;
import com.techjar.ledcm.hardware.manager.LEDManager;
import com.techjar.ledcm.hardware.animation.Animation;
import com.techjar.ledcm.hardware.animation.sequence.AnimationSequence;
import com.techjar.ledcm.util.math.Dimension3D;
import com.techjar.ledcm.util.MathHelper;
import com.techjar.ledcm.util.OperatingSystem;
import com.techjar.ledcm.util.math.Vector3;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.util.Color;
import org.lwjgl.util.Dimension;
import org.newdawn.slick.UnicodeFont;

/**
 *
 * @author Techjar
 */
public class ScreenMainControl extends Screen {
	public final UnicodeFont font;
	public final UnicodeFont fontTabbed;
	public final GUISlider progressSlider;
	public final GUIWindow layersWindow;
	public final GUIWindow sequenceWindow;
	public final GUIWindow animOptionsWindow;
	public final GUIWindow settingsWindow;
	public final GUIWindow controlsWindow;
	public final GUIWindow transformWindow;
	public final GUIScrollBox animOptionsScrollBox;
	public final GUIScrollBox settingsScrollBox;
	public final GUITabbed controlsTabbed;
	public final GUIButton animOptionsBtn;
	public final GUIButton settingsBtn;
	public final GUIButton audioInputBtn;
	public final GUIBackground audioInputBtnBg;
	public final GUISpinner mixerGainSpinner;
	public final GUILabel mixerGainLabel;
	public final GUISlider layerSlider;
	public final GUIComboBox animComboBox;
	public final GUIComboBox resolutionComboBox;
	public final GUIComboBox audioInputComboBox;
	public final GUIComboButton antiAliasingComboBtn;
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
	public final GUIButton settingsApplyBtn;
	public final GUIButton controlsBtn;
	public final GUIButton transformBtn;
	public final GUILabel mirrorLabel;
	public final GUICheckBox mirrorX;
	public final GUILabel mirrorXLabel;
	public final GUICheckBox mirrorY;
	public final GUILabel mirrorYLabel;
	public final GUICheckBox mirrorZ;
	public final GUILabel mirrorZLabel;
	public final GUILabel rotateLabel;
	public final GUISpinner rotateXSpinner;
	public final GUIButton rotateXButton;
	public final GUILabel rotateXLabel;
	public final GUISpinner rotateYSpinner;
	public final GUIButton rotateYButton;
	public final GUILabel rotateYLabel;
	public final GUISpinner rotateZSpinner;
	public final GUIButton rotateZButton;
	public final GUILabel rotateZLabel;
	public final GUICheckBox previewTransform;
	public final GUILabel previewTransformLabel;
	public final GUIComboBox windowModeComboBox;
	public final GUILabel limitFramerateLabel;
	public final GUISlider limitFramerateSlider;
	public final GUILabel fovLabel;
	public final GUISlider fovSlider;
	public final GUILabel bloomLabel;
	public final GUICheckBox bloomCheckbox;
	public final GUIButton exitBtn;
	public final GUIFileChooser fileChooser;

	public ScreenMainControl() {
		super();
		final LEDManager ledManager = LEDCubeManager.getLEDCube().getLEDManager();
		final Dimension3D ledDim = ledManager.getDimensions();

		font = LEDCubeManager.getFontManager().getFont("chemrea", 30, false, false).getUnicodeFont();
		fontTabbed = LEDCubeManager.getFontManager().getFont("chemrea", 22, false, false).getUnicodeFont();
		fileChooser = new GUIFileChooser(font, new Color(255, 255, 255), new Color(50, 50, 50), 35, new GUIBackground(new Color(10, 10, 10), new Color(255, 0, 0), 2), new GUIBackground(new Color(0, 0, 0), new Color(255, 0, 0), 2));
		fileChooser.setDimension(700, 500);
		fileChooser.addFileFilter(new FileNameExtensionFilter("Audio Files (*.wav, *.mp3, *.ogg, *.flac, *.m4a, *.aac)", "wav", "mp3", "ogg", "flac", "m4a", "aac"));
		fileChooser.setMultiSelectionEnabled(false);
		if (LEDCubeManager.getConfig().propertyExists("misc.filechooserpath") && new File(LEDCubeManager.getConfig().getString("misc.filechooserpath")).exists())
			fileChooser.setCurrentDirectory(new File(LEDCubeManager.getConfig().getString("misc.filechooserpath")));
		else if (new File(System.getProperty("user.home"), "Music").exists())
			fileChooser.setCurrentDirectory(new File(System.getProperty("user.home"), "Music"));
		container.addComponent(fileChooser);

		playBtn = new GUIButton(font, new Color(255, 255, 255), "Play", new GUIBackground(new Color(255, 0, 0), new Color(50, 50, 50), 2));
		playBtn.setParentAlignment(GUIAlignment.BOTTOM_LEFT);
		playBtn.setDimension(100, 40);
		playBtn.setPosition(5, -5);
		playBtn.setClickHandler(component -> {
			if (LEDCubeManager.getLEDCube().getCommThread().getCurrentSequence() == null || !LEDCubeManager.getLEDCube().getSpectrumAnalyzer().isPlaying()) {
				LEDCubeManager.getLEDCube().getSpectrumAnalyzer().play();
			}
		});
		container.addComponent(playBtn);
		pauseBtn = new GUIButton(font, new Color(255, 255, 255), "Pause", new GUIBackground(new Color(255, 0, 0), new Color(50, 50, 50), 2));
		pauseBtn.setParentAlignment(GUIAlignment.BOTTOM_LEFT);
		pauseBtn.setDimension(100, 40);
		pauseBtn.setPosition(110, -5);
		pauseBtn.setClickHandler(component -> {
			LEDCubeManager.getLEDCube().getSpectrumAnalyzer().pause();
		});
		container.addComponent(pauseBtn);
		stopBtn = new GUIButton(font, new Color(255, 255, 255), "Stop", new GUIBackground(new Color(255, 0, 0), new Color(50, 50, 50), 2));
		stopBtn.setParentAlignment(GUIAlignment.BOTTOM_LEFT);
		stopBtn.setDimension(100, 40);
		stopBtn.setPosition(215, -5);
		stopBtn.setClickHandler(component -> {
			LEDCubeManager.getLEDCube().getSpectrumAnalyzer().stop();
		});
		container.addComponent(stopBtn);
		chooseFileBtn = new GUIButton(font, new Color(255, 255, 255), "Choose File...", new GUIBackground(new Color(255, 0, 0), new Color(50, 50, 50), 2));
		chooseFileBtn.setParentAlignment(GUIAlignment.BOTTOM_LEFT);
		chooseFileBtn.setDimension(200, 40);
		chooseFileBtn.setPosition(320, -5);
		chooseFileBtn.setClickHandler(component -> {
			if (LEDCubeManager.isConvertingAudio()) return;
			if (Main.isVrMode() || LEDCubeManager.getInstance().isFullscreen() || OperatingSystem.isLinux()) {
				fileChooser.showOpenDialog(component2 -> {
					final File file = fileChooser.getSelectedFile();
					new Thread(() -> {
						try {
							LEDCubeManager.getConfig().setProperty("misc.filechooserpath", file.getParentFile().getAbsolutePath());
							LEDCubeManager.getLEDCube().getSpectrumAnalyzer().loadFile(file);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}, "File Chooser").start();
				});
			} else {
				new Thread(() -> {
					int option = LEDCubeManager.getFileChooser().showOpenDialog(LEDCubeManager.getFrame());
					if (option == JFileChooser.APPROVE_OPTION) {
						try {
							File file = LEDCubeManager.getFileChooser().getSelectedFile();
							LEDCubeManager.getConfig().setProperty("misc.filechooserpath", file.getParentFile().getAbsolutePath());
							LEDCubeManager.getLEDCube().getSpectrumAnalyzer().loadFile(file);
						} catch (Exception ex) {
							ex.printStackTrace();
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
		volumeSlider.setChangeHandler(component -> {
			LEDCubeManager.getLEDCube().getSpectrumAnalyzer().setVolume(volumeSlider.getValue());
		});
		container.addComponent(volumeSlider);
		progressSlider = new GUISlider(new Color(255, 0, 0), new Color(50, 50, 50));
		progressSlider.setParentAlignment(GUIAlignment.BOTTOM_LEFT);
		progressSlider.setDimension(310, 30);
		progressSlider.setPosition(5, -55);
		progressSlider.setChangeHandler(component -> {
			LEDCubeManager.getLEDCube().getSpectrumAnalyzer().setPosition(progressSlider.getValue());
		});
		container.addComponent(progressSlider);
		togglePortBtn = new GUIButton(font, new Color(255, 255, 255), "Toggle Port", new GUIBackground(new Color(255, 0, 0), new Color(50, 50, 50), 2));
		togglePortBtn.setParentAlignment(GUIAlignment.TOP_RIGHT);
		togglePortBtn.setDimension(190, 35);
		togglePortBtn.setPosition(-410, 5);
		togglePortBtn.setClickHandler(component -> {
			if (LEDCubeManager.getLEDCube().getCommThread().isPortOpen()) LEDCubeManager.getLEDCube().getCommThread().closePort();
			else LEDCubeManager.getLEDCube().getCommThread().openPort();
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
		redColorSlider.setChangeHandler(component -> {
			LEDCubeManager.getPaintColor().setRed(Math.round(255 * redColorSlider.getValue()));
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
		greenColorSlider.setChangeHandler(component -> {
			LEDCubeManager.getPaintColor().setGreen(Math.round(255 * greenColorSlider.getValue()));
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
		blueColorSlider.setChangeHandler(component -> {
			if (LEDCubeManager.getLEDCube().getLEDManager().isMonochrome()) {
				Color color = LEDCubeManager.getLEDCube().getLEDManager().getMonochromeColor();
				LEDCubeManager.getPaintColor().setRed(Math.round(color.getRed() * blueColorSlider.getValue()));
				LEDCubeManager.getPaintColor().setGreen(Math.round(color.getGreen() * blueColorSlider.getValue()));
				LEDCubeManager.getPaintColor().setBlue(Math.round(color.getBlue() * blueColorSlider.getValue()));
			} else {
				LEDCubeManager.getPaintColor().setBlue(Math.round(255 * blueColorSlider.getValue()));
			}
		});
		container.addComponent(blueColorSlider);
		if (LEDCubeManager.getLEDCube().getLEDManager().isMonochrome()) {
			Color color = LEDCubeManager.getLEDCube().getLEDManager().getMonochromeColor();
			LEDCubeManager.getPaintColor().setColor(color);
			redColorSlider.setVisible(false);
			greenColorSlider.setVisible(false);
			blueColorSlider.setColor(color);
		}
		xScaleSlider = new GUISlider(new Color(255, 0, 0), new Color(50, 50, 50));
		xScaleSlider.setParentAlignment(GUIAlignment.BOTTOM_RIGHT);
		xScaleSlider.setDimension(120, 30);
		xScaleSlider.setPosition(-10, -240);
		xScaleSlider.setIncrement(1F / (ledDim.x - 1));
		xScaleSlider.setChangeHandler(component -> {
			LEDCubeManager.getLEDCube().getPaintSize().setX(Math.round((ledDim.x - 1) * xScaleSlider.getValue()));
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
		yScaleSlider.setChangeHandler(component -> {
			LEDCubeManager.getLEDCube().getPaintSize().setY(Math.round((ledDim.y - 1) * yScaleSlider.getValue()));
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
		zScaleSlider.setChangeHandler(component -> {
			LEDCubeManager.getLEDCube().getPaintSize().setZ(Math.round((ledDim.z - 1) * zScaleSlider.getValue()));
		});
		container.addComponent(zScaleSlider);
		zScaleLabel = new GUILabel(font, new Color(255, 255, 255), "Z");
		zScaleLabel.setParentAlignment(GUIAlignment.BOTTOM_RIGHT);
		zScaleLabel.setDimension(20, 30);
		zScaleLabel.setPosition(-135, -170);
		container.addComponent(zScaleLabel);

		layersWindow = new GUIWindow(new GUIBackground(new Color(10, 10, 10), new Color(255, 0, 0), 2));
		layersWindow.setDimension(150, 167);
		layersWindow.setResizable(false);
		layersWindow.setCloseAction(GUIWindow.HIDE_ON_CLOSE);
		layersWindow.setVisible(false);
		container.addComponent(layersWindow);
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
		layersBtn = new GUIButton(font, new Color(255, 255, 255), "Layers", new GUIBackground(new Color(255, 0, 0), new Color(50, 50, 50), 2));
		layersBtn.setParentAlignment(GUIAlignment.BOTTOM_RIGHT);
		layersBtn.setDimension(120, 35);
		layersBtn.setPosition(-10, -275);
		layersBtn.setClickHandler(component -> {
			layersWindow.setVisible(!layersWindow.isVisible());
			if (layersWindow.isVisible()) layersWindow.setToBePutOnTop(true);
		});
		container.addComponent(layersBtn);
		radioOff = new GUIRadioButton(new Color(255, 255, 255), new GUIBackground(new Color(0, 0, 0), new Color(255, 0, 0), 2));
		radioOff.setDimension(30, 30);
		radioOff.setPosition(5, 5);
		radioOff.setSelected(true);
		radioOff.setSelectHandler(component -> {
			LEDCubeManager.getLEDCube().setLayerIsolation(0);
			layerSlider.setEnabled(false);
			layerSlider.setValue(0);
			layerSlider.setIncrement(0);
		});
		radioOffLabel = new GUILabel(font, new Color(255, 255, 255), "Off");
		radioOffLabel.setDimension(60, 30);
		radioOffLabel.setPosition(40, 5);
		radioOff.setLabel(radioOffLabel);
		layersWindow.addComponent(radioOffLabel);
		layersWindow.addComponent(radioOff);
		radioX = new GUIRadioButton(new Color(255, 255, 255), new GUIBackground(new Color(0, 0, 0), new Color(255, 0, 0), 2));
		radioX.setDimension(30, 30);
		radioX.setPosition(5, 40);
		radioX.setSelectHandler(component -> {
			LEDCubeManager.getLEDCube().setLayerIsolation(1);
			layerSlider.setEnabled(true);
			layerSlider.setValue(0);
			layerSlider.setIncrement(1F / (ledDim.x - 1));
			layerSlider.setChangeHandler(component2 -> {
				LEDCubeManager.getLEDCube().setSelectedLayer(Math.round((ledDim.x - 1) * layerSlider.getValue()));
			});
		});
		radioXLabel = new GUILabel(font, new Color(255, 255, 255), "X");
		radioXLabel.setDimension(20, 30);
		radioXLabel.setPosition(40, 40);
		radioX.setLabel(radioXLabel);
		layersWindow.addComponent(radioXLabel);
		layersWindow.addComponent(radioX);
		radioY = new GUIRadioButton(new Color(255, 255, 255), new GUIBackground(new Color(0, 0, 0), new Color(255, 0, 0), 2));
		radioY.setDimension(30, 30);
		radioY.setPosition(5, 75);
		radioY.setSelectHandler(component -> {
			LEDCubeManager.getLEDCube().setLayerIsolation(2);
			layerSlider.setEnabled(true);
			layerSlider.setValue(0);
			layerSlider.setIncrement(1F / (ledDim.y - 1));
			layerSlider.setChangeHandler(component2 -> {
				LEDCubeManager.getLEDCube().setSelectedLayer(Math.round((ledDim.y - 1) * layerSlider.getValue()));
			});
		});
		radioYLabel = new GUILabel(font, new Color(255, 255, 255), "Y");
		radioYLabel.setDimension(20, 30);
		radioYLabel.setPosition(40, 75);
		radioY.setLabel(radioYLabel);
		layersWindow.addComponent(radioYLabel);
		layersWindow.addComponent(radioY);
		radioZ = new GUIRadioButton(new Color(255, 255, 255), new GUIBackground(new Color(0, 0, 0), new Color(255, 0, 0), 2));
		radioZ.setDimension(30, 30);
		radioZ.setPosition(5, 110);
		radioZ.setSelectHandler(component -> {
			LEDCubeManager.getLEDCube().setLayerIsolation(3);
			layerSlider.setEnabled(true);
			layerSlider.setValue(0);
			layerSlider.setIncrement(1F / (ledDim.z - 1));
			layerSlider.setChangeHandler(component2 -> {
				LEDCubeManager.getLEDCube().setSelectedLayer(Math.round((ledDim.z - 1) * layerSlider.getValue()));
			});
		});
		radioZLabel = new GUILabel(font, new Color(255, 255, 255), "Z");
		radioZLabel.setDimension(20, 30);
		radioZLabel.setPosition(40, 110);
		radioZ.setLabel(radioZLabel);
		layersWindow.addComponent(radioZLabel);
		layersWindow.addComponent(radioZ);


		sequenceComboBox = new GUIComboBox(font, new Color(255, 255, 255), new GUIBackground(new Color(0, 0, 0), new Color(255, 0, 0), 2));
		sequenceComboBox.setParentAlignment(GUIAlignment.TOP_CENTER);
		sequenceComboBox.setDimension(300, 35);
		sequenceComboBox.setPosition(0, 10);
		sequenceComboBox.setVisibleItems(2);
		sequenceWindow = new GUIWindow(new GUIBackground(new Color(10, 10, 10), new Color(255, 0, 0), 2));
		sequenceWindow.setDimension(350, 150);
		sequenceWindow.setResizable(false);
		sequenceWindow.setCloseAction(GUIWindow.HIDE_ON_CLOSE);
		sequenceWindow.setVisible(false);
		container.addComponent(sequenceWindow);
		sequenceLoadBtn = new GUIButton(font, new Color(255, 255, 255), "Sequence", new GUIBackground(new Color(255, 0, 0), new Color(50, 50, 50), 2));
		sequenceLoadBtn.setParentAlignment(GUIAlignment.BOTTOM_LEFT);
		sequenceLoadBtn.setDimension(200, 40);
		sequenceLoadBtn.setPosition(320, -50);
		sequenceLoadBtn.setClickHandler(component -> {
			sequenceWindow.setVisible(!sequenceWindow.isVisible());
			if (sequenceWindow.isVisible()) {
				sequenceWindow.setToBePutOnTop(true);
				sequenceComboBox.setSelectedItem(-1);
				sequenceComboBox.clearItems();
				File dir = new File("resources/sequences/");
				File[] files = dir.listFiles();
				if (files != null) {
					for (File file : files) {
						String name = file.getName();
						if (name.toLowerCase().endsWith(".sequence")) {
							sequenceComboBox.addItem(name.substring(0, name.lastIndexOf('.')));
						}
					}
				}
			}
		});
		container.addComponent(sequenceLoadBtn);
		audioInputBtnBg = new GUIBackground(new Color(255, 0, 0), new Color(50, 50, 50), 2);
		audioInputBtn = new GUIButton(font, new Color(255, 255, 255), "Audio Input", audioInputBtnBg);
		audioInputBtn.setParentAlignment(GUIAlignment.TOP_RIGHT);
		audioInputBtn.setDimension(200, 35);
		audioInputBtn.setPosition(-5, 85);
		audioInputBtn.setClickHandler(component -> {
			boolean running = LEDCubeManager.getLEDCube().getSpectrumAnalyzer().isRunningAudioInput();
			audioInputBtnBg.setBackgroundColor(running ? new Color(255, 0, 0) : new Color(0, 255, 0));
			if (running) LEDCubeManager.getLEDCube().getSpectrumAnalyzer().stopAudioInput();
			else LEDCubeManager.getLEDCube().getSpectrumAnalyzer().startAudioInput();
		});
		container.addComponent(audioInputBtn);
		sequencePlayBtn = new GUIButton(font, new Color(255, 255, 255), "Start", new GUIBackground(new Color(255, 0, 0), new Color(50, 50, 50), 2));
		sequencePlayBtn.setParentAlignment(GUIAlignment.TOP_CENTER);
		sequencePlayBtn.setDimension(100, 40);
		sequencePlayBtn.setPosition(-55, 65);
		sequencePlayBtn.setClickHandler(component -> {
			Object item = sequenceComboBox.getSelectedItem();
			if (item != null) {
				try {
					File file = new File("resources/sequences/" + item.toString() + ".sequence");
					AnimationSequence sequence = AnimationSequence.loadFromFile(file);
					sequence.setName(item.toString());
					LEDCubeManager.getLEDCube().getCommThread().setCurrentSequence(sequence);
					sequenceWindow.setVisible(false);
					chooseFileBtn.setEnabled(!sequence.isMusicSynced());
					progressSlider.setEnabled(!sequence.isMusicSynced());
					stopBtn.setEnabled(!sequence.isMusicSynced());
					audioInputBtn.setEnabled(!sequence.isMusicSynced());
					if (sequence.isMusicSynced()) fileChooser.setVisible(false);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		sequenceWindow.addComponent(sequencePlayBtn);
		sequenceStopBtn = new GUIButton(font, new Color(255, 255, 255), "Stop", new GUIBackground(new Color(255, 0, 0), new Color(50, 50, 50), 2));
		sequenceStopBtn.setParentAlignment(GUIAlignment.TOP_CENTER);
		sequenceStopBtn.setDimension(100, 40);
		sequenceStopBtn.setPosition(55, 65);
		sequenceStopBtn.setClickHandler(component -> {
			LEDCubeManager.getLEDCube().getCommThread().setCurrentSequence(null);
			chooseFileBtn.setEnabled(true);
			progressSlider.setEnabled(true);
			stopBtn.setEnabled(true);
			audioInputBtn.setEnabled(true);
		});
		sequenceWindow.addComponent(sequenceStopBtn);
		sequenceWindow.addComponent(sequenceComboBox);

		animOptionsWindow = new GUIWindow(new GUIBackground(new Color(10, 10, 10), new Color(255, 0, 0), 2));
		animOptionsWindow.setDimension(500, 300);
		animOptionsWindow.setResizable(false, true);
		animOptionsWindow.setCloseAction(GUIWindow.HIDE_ON_CLOSE);
		animOptionsWindow.setVisible(false);
		container.addComponent(animOptionsWindow);
		animOptionsScrollBox = new GUIScrollBox(new Color(255, 0, 0));
		animOptionsScrollBox.setDimension((int)animOptionsWindow.getContainerBox().getWidth(), (int)animOptionsWindow.getContainerBox().getHeight());
		animOptionsScrollBox.setScrollXMode(GUIScrollBox.ScrollMode.DISABLED);
		animOptionsScrollBox.setScrollYMode(GUIScrollBox.ScrollMode.AUTOMATIC);
		animOptionsWindow.setDimensionChangeHandler(component -> {
			animOptionsScrollBox.setDimension((int)animOptionsWindow.getContainerBox().getWidth(), (int)animOptionsWindow.getContainerBox().getHeight());
		});
		animOptionsWindow.addComponent(animOptionsScrollBox);
		animOptionsBtn = new GUIButton(font, new Color(255, 255, 255), "Options", new GUIBackground(new Color(255, 0, 0), new Color(50, 50, 50), 2));
		animOptionsBtn.setParentAlignment(GUIAlignment.TOP_RIGHT);
		animOptionsBtn.setDimension(140, 35);
		animOptionsBtn.setPosition(-605, 5);
		animOptionsBtn.setClickHandler(component -> {
			animOptionsWindow.setVisible(!animOptionsWindow.isVisible());
			if (animOptionsWindow.isVisible()) animOptionsWindow.setToBePutOnTop(true);
		});
		container.addComponent(animOptionsBtn);

		animComboBox = new GUIComboBox(font, new Color(255, 255, 255), new GUIBackground(new Color(0, 0, 0), new Color(255, 0, 0), 2));
		animComboBox.setParentAlignment(GUIAlignment.TOP_RIGHT);
		animComboBox.setDimension(400, 35);
		animComboBox.setPosition(-5, 5);
		animComboBox.setVisibleItems(8);
		animComboBox.setChangeHandler(component -> {
			if (animComboBox.getSelectedItem() != null) {
				Animation animation = LEDCubeManager.getLEDCube().getAnimations().get(animComboBox.getSelectedItem().toString());
				LEDCubeManager.getLEDCube().getCommThread().setCurrentAnimation(animation);
			}
		});
		//populateAnimationList();
		container.addComponent(animComboBox);

		settingsWindow = new GUIWindow(new GUIBackground(new Color(10, 10, 10), new Color(255, 0, 0), 2));
		settingsWindow.setDimension(450, 450);
		settingsWindow.setResizable(false, true);
		settingsWindow.setCloseAction(GUIWindow.HIDE_ON_CLOSE);
		settingsWindow.setMinimumSize(new Dimension(50, 150));
		settingsWindow.setVisible(false);
		container.addComponent(settingsWindow);
		settingsScrollBox = new GUIScrollBox(new Color(255, 0, 0));
		settingsScrollBox.setDimension((int)settingsWindow.getContainerBox().getWidth(), (int)settingsWindow.getContainerBox().getHeight() - 60);
		settingsScrollBox.setScrollXMode(GUIScrollBox.ScrollMode.DISABLED);
		settingsScrollBox.setScrollYMode(GUIScrollBox.ScrollMode.AUTOMATIC);
		settingsWindow.setDimensionChangeHandler(component -> {
			settingsScrollBox.setDimension((int)settingsWindow.getContainerBox().getWidth(), (int)settingsWindow.getContainerBox().getHeight() - 60);
		});
		settingsWindow.addComponent(settingsScrollBox);
		resolutionComboBox = new GUIComboBox(font, new Color(255, 255, 255), new GUIBackground(new Color(0, 0, 0), new Color(255, 0, 0), 2));
		resolutionComboBox.setParentAlignment(GUIAlignment.TOP_CENTER);
		resolutionComboBox.setDimension(400, 35);
		resolutionComboBox.setPosition(0, 10);
		resolutionComboBox.setVisibleItems(5);
		for (DisplayMode displayMode : LEDCubeManager.getInstance().getDisplayModeList()) {
			resolutionComboBox.addItem(displayMode.getWidth() + "x" + displayMode.getHeight() + " @ " + displayMode.getFrequency() + "Hz");
		}
		resolutionComboBox.setSelectedItem(LEDCubeManager.getWidth() + "x" + LEDCubeManager.getHeight() + " @ " + LEDCubeManager.getDisplayMode().getFrequency() + "Hz");
		settingsScrollBox.addComponent(resolutionComboBox);
		windowModeComboBox = new GUIComboBox(font, new Color(255, 255, 255), new GUIBackground(new Color(0, 0, 0), new Color(255, 0, 0), 2));
		windowModeComboBox.setParentAlignment(GUIAlignment.TOP_CENTER);
		windowModeComboBox.setDimension(400, 35);
		windowModeComboBox.setPosition(0, 55);
		windowModeComboBox.addAllItems(Lists.newArrayList("Windowed", "Fullscreen", "Borderless"));
		windowModeComboBox.setSelectedItem(LEDCubeManager.getInstance().isFullscreen() ? (LEDCubeManager.getInstance().isBorderless() ? "Borderless" : "Fullscreen") : "Windowed");
		settingsScrollBox.addComponent(windowModeComboBox);
		audioInputComboBox = new GUIComboBox(font, new Color(255, 255, 255), new GUIBackground(new Color(0, 0, 0), new Color(255, 0, 0), 2));
		audioInputComboBox.setParentAlignment(GUIAlignment.TOP_CENTER);
		audioInputComboBox.setDimension(400, 35);
		audioInputComboBox.setPosition(0, 100);
		audioInputComboBox.setVisibleItems(5);
		audioInputComboBox.addAllItems(LEDCubeManager.getLEDCube().getSpectrumAnalyzer().getMixers().keySet());
		audioInputComboBox.setSelectedItem(LEDCubeManager.getLEDCube().getSpectrumAnalyzer().getCurrentMixerName());
		settingsScrollBox.addComponent(audioInputComboBox);
		antiAliasingComboBtn = new GUIComboButton(font, new Color(255, 255, 255), new GUIBackground(new Color(255, 0, 0), new Color(50, 50, 50), 2));
		antiAliasingComboBtn.setParentAlignment(GUIAlignment.TOP_CENTER);
		antiAliasingComboBtn.setDimension(400, 35);
		antiAliasingComboBtn.setPosition(0, 145);
		antiAliasingComboBtn.addItem("Off");
		for (int i = 2; i <= LEDCubeManager.getInstance().antiAliasingMaxSamples; i *= 2) {
			antiAliasingComboBtn.addItem(i + "x");
		}
		antiAliasingComboBtn.setSelectedItem(LEDCubeManager.getInstance().isAntiAliasing() ? LEDCubeManager.getInstance().getAntiAliasingSamples() + "x" : "Off");
		settingsScrollBox.addComponent(antiAliasingComboBtn);
		fovLabel = new GUILabel(font, new Color(255, 255, 255), "FOV");
		fovLabel.setParentAlignment(GUIAlignment.TOP_CENTER);
		fovLabel.setDimension(font.getWidth(fovLabel.getText()), 30);
		fovLabel.setPosition(-200 + (fovLabel.getWidth() / 2), 190);
		settingsScrollBox.addComponent(fovLabel);
		fovSlider = new GUISlider(new Color(255, 0, 0), new Color(50, 50, 50));
		fovSlider.setParentAlignment(GUIAlignment.TOP_CENTER);
		fovSlider.setDimension(400 - fovLabel.getWidth() - 10, 30);
		fovSlider.setPosition(fovLabel.getWidth() / 2 + 5, 190);
		fovSlider.setValue((LEDCubeManager.getInstance().getFieldOfView() - 30) / 60);
		fovSlider.setChangeHandler(component -> {
			LEDCubeManager.getInstance().setFieldOfView(fovSlider.getValue() * 60 + 30);
			LEDCubeManager.getConfig().setProperty("display.fieldofview", LEDCubeManager.getInstance().getFieldOfView());
		});
		settingsScrollBox.addComponent(fovSlider);
		limitFramerateLabel = new GUILabel(font, new Color(255, 255, 255), "Max FPS");
		limitFramerateLabel.setParentAlignment(GUIAlignment.TOP_CENTER);
		limitFramerateLabel.setDimension(font.getWidth(limitFramerateLabel.getText()), 30);
		limitFramerateLabel.setPosition(-200 + (limitFramerateLabel.getWidth() / 2), 235);
		settingsScrollBox.addComponent(limitFramerateLabel);
		limitFramerateSlider = new GUISlider(new Color(255, 0, 0), new Color(50, 50, 50));
		limitFramerateSlider.setParentAlignment(GUIAlignment.TOP_CENTER);
		limitFramerateSlider.setDimension(400 - limitFramerateLabel.getWidth() - 10, 30);
		limitFramerateSlider.setPosition(limitFramerateLabel.getWidth() / 2 + 5, 235);
		limitFramerateSlider.setIncrement(1F / 171F);
		limitFramerateSlider.setShowNotches(false);
		limitFramerateSlider.setValue(LEDCubeManager.getInstance().getFramerateCap() == 0 ? 0 : (LEDCubeManager.getInstance().getFramerateCap() - 29) / 171F);
		limitFramerateSlider.setChangeHandler(component -> {
			if (limitFramerateSlider.getValue() > 0)
				LEDCubeManager.getInstance().setFramerateCap(Math.round(limitFramerateSlider.getValue() * 171) + 29);
			else
				LEDCubeManager.getInstance().setFramerateCap(0);
		});
		if (Main.isVrMode()) limitFramerateSlider.setEnabled(false);
		settingsScrollBox.addComponent(limitFramerateSlider);
		bloomLabel = new GUILabel(font, new Color(255, 255, 255), "Bloom");
		bloomLabel.setParentAlignment(GUIAlignment.TOP_CENTER);
		bloomLabel.setDimension(font.getWidth(bloomLabel.getText()), 30);
		bloomLabel.setPosition(-165 + (bloomLabel.getWidth() / 2), 280);
		settingsScrollBox.addComponent(bloomLabel);
		bloomCheckbox = new GUICheckBox(new Color(255, 255, 255), new GUIBackground(new Color(0, 0, 0), new Color(255, 0, 0), 2));
		bloomCheckbox.setParentAlignment(GUIAlignment.TOP_CENTER);
		bloomCheckbox.setDimension(30, 30);
		bloomCheckbox.setPosition(-185, 280);
		bloomCheckbox.setLabel(bloomLabel);
		bloomCheckbox.setChecked(LEDCubeManager.getInstance().isEnableBloom());
		settingsScrollBox.addComponent(bloomCheckbox);
		settingsApplyBtn = new GUIButton(font, new Color(255, 255, 255), "Apply", new GUIBackground(new Color(255, 0, 0), new Color(50, 50, 50), 2));
		settingsApplyBtn.setParentAlignment(GUIAlignment.BOTTOM_CENTER);
		settingsApplyBtn.setDimension(200, 40);
		settingsApplyBtn.setPosition(-105, -10);
		settingsApplyBtn.setClickHandler(component -> {
			settingsWindow.setVisible(false);
			Object item = resolutionComboBox.getSelectedItem();
			if (item != null) {
				String[] split = item.toString().split(" @ ");
				String[] split2 = split[0].split("x");
				if (split.length == 2 && split2.length == 2) {
					LEDCubeManager.getInstance().setDisplayMode(Integer.parseInt(split2[0]), Integer.parseInt(split2[1]), Integer.parseInt(split[1].substring(0, split[1].length() - 2)));
				}
			}
			LEDCubeManager.getInstance().setFullscreen(windowModeComboBox.getSelectedItem().equals("Fullscreen") || windowModeComboBox.getSelectedItem().equals("Borderless"));
			LEDCubeManager.getInstance().setBorderless(windowModeComboBox.getSelectedItem().equals("Borderless"));
			LEDCubeManager.getInstance().setEnableBloom(bloomCheckbox.isChecked());
			item = audioInputComboBox.getSelectedItem();
			if (item != null) {
				LEDCubeManager.getLEDCube().getSpectrumAnalyzer().setMixer(item.toString());
			}
			item = antiAliasingComboBtn.getSelectedItem();
			if (item != null) {
				switch (item.toString()) {
					case "Off":
						LEDCubeManager.getInstance().setAntiAliasing(false, 2);
						break;
					default:
						LEDCubeManager.getInstance().setAntiAliasing(true, Integer.parseInt(Character.toString(item.toString().charAt(0))));
						break;
				}
			}
		});
		settingsWindow.addComponent(settingsApplyBtn);

		controlsWindow = new GUIWindow(new GUIBackground(new Color(10, 10, 10), new Color(255, 0, 0), 2));
		controlsWindow.setDimension(500, 450);
		controlsWindow.setResizable(false, true);
		controlsWindow.setCloseAction(GUIWindow.HIDE_ON_CLOSE);
		controlsWindow.setMinimumSize(new Dimension(50, 150));
		controlsWindow.setVisible(false);
		container.addComponent(controlsWindow);
		controlsTabbed = new GUITabbed(fontTabbed, new Color(255, 255, 255), new GUIBackground(new Color(50, 50, 50), new Color(255, 0, 0), 2));
		controlsTabbed.setDimension((int)controlsWindow.getContainerBox().getWidth(), (int)controlsWindow.getContainerBox().getHeight());
		controlsWindow.setDimensionChangeHandler(component -> {
			controlsTabbed.setDimension((int)controlsWindow.getContainerBox().getWidth(), (int)controlsWindow.getContainerBox().getHeight());
		});
		controlsWindow.addComponent(controlsTabbed);

		controlsBtn = new GUIButton(font, new Color(255, 255, 255), "Controls", new GUIBackground(new Color(255, 0, 0), new Color(50, 50, 50), 2));
		controlsBtn.setParentAlignment(GUIAlignment.BOTTOM_CENTER);
		controlsBtn.setDimension(200, 40);
		controlsBtn.setPosition(105, -10);
		controlsBtn.setClickHandler(component -> {
			controlsWindow.setVisible(!controlsWindow.isVisible());
			if (controlsWindow.isVisible()) controlsWindow.setToBePutOnTop(true);
		});
		settingsWindow.addComponent(controlsBtn);
		settingsBtn = new GUIButton(font, new Color(255, 255, 255), "Settings", new GUIBackground(new Color(255, 0, 0), new Color(50, 50, 50), 2));
		settingsBtn.setParentAlignment(GUIAlignment.TOP_RIGHT);
		settingsBtn.setDimension(160, 35);
		settingsBtn.setPosition(-5, 45);
		settingsBtn.setClickHandler(component -> {
			settingsWindow.setVisible(!settingsWindow.isVisible());
			if (settingsWindow.isVisible()) settingsWindow.setToBePutOnTop(true);
		});
		container.addComponent(settingsBtn);

		mixerGainSpinner = new GUISpinner(font, new Color(255, 255, 255), new GUIBackground(new Color(0, 0, 0), new Color(255, 0, 0), 2));
		mixerGainSpinner.setParentAlignment(GUIAlignment.TOP_RIGHT);
		mixerGainSpinner.setDimension(200, 30);
		mixerGainSpinner.setPosition(-5, 125);
		mixerGainSpinner.setMinValue(-100);
		mixerGainSpinner.setMaxValue(100);
		mixerGainSpinner.setValue(0);
		mixerGainSpinner.setIncrement(1);
		mixerGainSpinner.setDecimalPlaces(1);
		mixerGainSpinner.setChangeHandler(component -> {
			float gain = (float)Math.pow(4, mixerGainSpinner.getValue() / 20F);
			LEDCubeManager.getLEDCube().getSpectrumAnalyzer().setMixerGain(gain);
			LEDCubeManager.getConfig().setProperty("sound.inputgain", gain);
		});
		mixerGainSpinner.setValue((float)MathHelper.log(LEDCubeManager.getConfig().getFloat("sound.inputgain"), 4) * 20);
		container.addComponent(mixerGainSpinner);
		mixerGainLabel = new GUILabel(font, new Color(255, 255, 255), "Gain (dBA)");
		mixerGainLabel.setParentAlignment(GUIAlignment.TOP_RIGHT);
		mixerGainLabel.setDimension(150, 30);
		mixerGainLabel.setPosition(-215, 125);
		container.addComponent(mixerGainLabel);
		exitBtn = new GUIButton(font, new Color(255, 255, 255), "Exit", new GUIBackground(new Color(255, 0, 0), new Color(50, 50, 50), 2));
		exitBtn.setParentAlignment(GUIAlignment.TOP_RIGHT);
		exitBtn.setDimension(90, 35);
		exitBtn.setPosition(-170, 45);
		exitBtn.setClickHandler(component -> {
			LEDCubeManager.getInstance().shutdown();
		});
		container.addComponent(exitBtn);

		transformWindow = new GUIWindow(new GUIBackground(new Color(10, 10, 10), new Color(255, 0, 0), 2));
		transformWindow.setDimension(310, 272);
		transformWindow.setResizable(false);
		transformWindow.setCloseAction(GUIWindow.HIDE_ON_CLOSE);
		transformWindow.setMinimumSize(new Dimension(50, 150));
		transformWindow.setVisible(false);
		container.addComponent(transformWindow);
		transformBtn = new GUIButton(font, new Color(255, 255, 255), "Transform", new GUIBackground(new Color(255, 0, 0), new Color(50, 50, 50), 2));
		transformBtn.setParentAlignment(GUIAlignment.BOTTOM_RIGHT);
		transformBtn.setDimension(165, 35);
		transformBtn.setPosition(-10, -315);
		transformBtn.setClickHandler(component -> {
			transformWindow.setVisible(!transformWindow.isVisible());
			if (transformWindow.isVisible()) transformWindow.setToBePutOnTop(true);
		});
		container.addComponent(transformBtn);
		mirrorLabel = new GUILabel(font, new Color(255, 255, 255), "Mirror");
		mirrorLabel.setDimension(20, 105);
		mirrorLabel.setPosition(10, 10);
		transformWindow.addComponent(mirrorLabel);
		mirrorX = new GUICheckBox(new Color(255, 255, 255), new GUIBackground(new Color(0, 0, 0), new Color(255, 0, 0), 2));
		mirrorY = new GUICheckBox(new Color(255, 255, 255), new GUIBackground(new Color(0, 0, 0), new Color(255, 0, 0), 2));
		mirrorZ = new GUICheckBox(new Color(255, 255, 255), new GUIBackground(new Color(0, 0, 0), new Color(255, 0, 0), 2));
		mirrorX.setDimension(30, 30);
		mirrorX.setPosition(115, 10);
		mirrorX.setChangeHandler(component -> {
			LEDCubeManager.getLEDCube().setReflection(mirrorX.isChecked(), mirrorY.isChecked(), mirrorZ.isChecked());
		});
		transformWindow.addComponent(mirrorX);
		mirrorXLabel = new GUILabel(font, new Color(255, 255, 255), "X");
		mirrorXLabel.setDimension(20, 30);
		mirrorXLabel.setPosition(150, 10);
		mirrorX.setLabel(mirrorXLabel);
		transformWindow.addComponent(mirrorXLabel);
		mirrorY.setDimension(30, 30);
		mirrorY.setPosition(180, 10);
		mirrorY.setChangeHandler(component -> {
			LEDCubeManager.getLEDCube().setReflection(mirrorX.isChecked(), mirrorY.isChecked(), mirrorZ.isChecked());
		});
		transformWindow.addComponent(mirrorY);
		mirrorYLabel = new GUILabel(font, new Color(255, 255, 255), "Y");
		mirrorYLabel.setDimension(20, 30);
		mirrorYLabel.setPosition(215, 10);
		mirrorY.setLabel(mirrorYLabel);
		transformWindow.addComponent(mirrorYLabel);
		mirrorZ.setDimension(30, 30);
		mirrorZ.setPosition(245, 10);
		mirrorZ.setChangeHandler(component -> {
			LEDCubeManager.getLEDCube().setReflection(mirrorX.isChecked(), mirrorY.isChecked(), mirrorZ.isChecked());
		});
		transformWindow.addComponent(mirrorZ);
		mirrorZLabel = new GUILabel(font, new Color(255, 255, 255), "Z");
		mirrorZLabel.setDimension(20, 30);
		mirrorZLabel.setPosition(280, 10);
		mirrorZ.setLabel(mirrorZLabel);
		transformWindow.addComponent(mirrorZLabel);
		rotateLabel = new GUILabel(font, new Color(255, 255, 255), "Rotate (Degrees)");
		rotateLabel.setDimension(font.getWidth(rotateLabel.getText()), 30);
		rotateLabel.setPosition(10, 50);
		transformWindow.addComponent(rotateLabel);
		rotateXLabel = new GUILabel(font, new Color(255, 255, 255), "X");
		rotateXLabel.setDimension(20, 30);
		rotateXLabel.setPosition(10, 93);
		transformWindow.addComponent(rotateXLabel);
		rotateXSpinner = new GUISpinner(font, new Color(255, 255, 255), new GUIBackground(new Color(0, 0, 0), new Color(255, 0, 0), 2));
		rotateXSpinner.setDimension(150, 35);
		rotateXSpinner.setPosition(35, 90);
		rotateXSpinner.setMinValue(-360);
		rotateXSpinner.setMaxValue(360);
		rotateXSpinner.setValue(90);
		rotateXSpinner.setIncrement(90);
		transformWindow.addComponent(rotateXSpinner);
		rotateXButton = new GUIButton(font, new Color(255, 255, 255), "Apply", new GUIBackground(new Color(255, 0, 0), new Color(50, 50, 50), 2));
		rotateXButton.setDimension(100, 35);
		rotateXButton.setPosition(195, 90);
		rotateXButton.setClickHandler(component -> {
			LEDCubeManager.getLEDCube().rotateTransform((float)Math.toRadians(rotateXSpinner.getValue()), new Vector3(1, 0, 0));
		});
		transformWindow.addComponent(rotateXButton);
		rotateYLabel = new GUILabel(font, new Color(255, 255, 255), "Y");
		rotateYLabel.setDimension(20, 30);
		rotateYLabel.setPosition(10, 133);
		transformWindow.addComponent(rotateYLabel);
		rotateYSpinner = new GUISpinner(font, new Color(255, 255, 255), new GUIBackground(new Color(0, 0, 0), new Color(255, 0, 0), 2));
		rotateYSpinner.setDimension(150, 35);
		rotateYSpinner.setPosition(35, 130);
		rotateYSpinner.setMinValue(-360);
		rotateYSpinner.setMaxValue(360);
		rotateYSpinner.setValue(90);
		rotateYSpinner.setIncrement(90);
		transformWindow.addComponent(rotateYSpinner);
		rotateYButton = new GUIButton(font, new Color(255, 255, 255), "Apply", new GUIBackground(new Color(255, 0, 0), new Color(50, 50, 50), 2));
		rotateYButton.setDimension(100, 35);
		rotateYButton.setPosition(195, 130);
		rotateYButton.setClickHandler(component -> {
			LEDCubeManager.getLEDCube().rotateTransform((float)Math.toRadians(rotateYSpinner.getValue()), new Vector3(0, 1, 0));
		});
		transformWindow.addComponent(rotateYButton);
		rotateZLabel = new GUILabel(font, new Color(255, 255, 255), "Z");
		rotateZLabel.setDimension(20, 30);
		rotateZLabel.setPosition(10, 173);
		transformWindow.addComponent(rotateZLabel);
		rotateZSpinner = new GUISpinner(font, new Color(255, 255, 255), new GUIBackground(new Color(0, 0, 0), new Color(255, 0, 0), 2));
		rotateZSpinner.setDimension(150, 35);
		rotateZSpinner.setPosition(35, 170);
		rotateZSpinner.setMinValue(-360);
		rotateZSpinner.setMaxValue(360);
		rotateZSpinner.setValue(90);
		rotateZSpinner.setIncrement(90);
		transformWindow.addComponent(rotateZSpinner);
		rotateZButton = new GUIButton(font, new Color(255, 255, 255), "Apply", new GUIBackground(new Color(255, 0, 0), new Color(50, 50, 50), 2));
		rotateZButton.setDimension(100, 35);
		rotateZButton.setPosition(195, 170);
		rotateZButton.setClickHandler(component -> {
			LEDCubeManager.getLEDCube().rotateTransform((float)Math.toRadians(rotateZSpinner.getValue()), new Vector3(0, 0, 1));
		});
		transformWindow.addComponent(rotateZButton);
		previewTransform = new GUICheckBox(new Color(255, 255, 255), new GUIBackground(new Color(0, 0, 0), new Color(255, 0, 0), 2));
		previewTransform.setDimension(30, 30);
		previewTransform.setPosition(10, 210);
		previewTransform.setChecked(true);
		previewTransform.setChangeHandler(component -> {
			LEDCubeManager.getLEDCube().setPreviewTransform(previewTransform.isChecked());
		});
		transformWindow.addComponent(previewTransform);
		previewTransformLabel = new GUILabel(font, new Color(255, 255, 255), "Preview");
		previewTransformLabel.setDimension(font.getWidth(previewTransformLabel.getText()), 30);
		previewTransformLabel.setPosition(45, 210);
		previewTransform.setLabel(previewTransformLabel);
		transformWindow.addComponent(previewTransformLabel);

		positionWindows();
	}

	@Override
	public void update(float delta) {
		super.update(delta);
		progressSlider.setValueWithoutNotify(LEDCubeManager.getLEDCube().getSpectrumAnalyzer().getPosition());
	}

	@Override
	public void render() {
		RenderHelper.drawSquare(LEDCubeManager.getWidth() - 40 - 10, LEDCubeManager.getHeight() - 150 - 10, 40, 150, LEDCubeManager.getLEDCube().getPaintColor());
		super.render();
	}

	@Override
	protected void onResized() {
		positionWindows();
	}

	public final void populateAnimationList() {
		animComboBox.clearItems();
		LEDCubeManager.getLEDCube().getAnimationNames().forEach(animComboBox::addItem);
		animComboBox.setSelectedItem(1);
	}

	private void positionWindows() {
		fileChooser.setPosition(container.getWidth() / 2 - fileChooser.getWidth() / 2, container.getHeight() / 2 - fileChooser.getHeight() / 2);
		layersWindow.setPosition(container.getWidth() - layersWindow.getWidth() - 10, container.getHeight() - layersWindow.getHeight() - 360);
		sequenceWindow.setPosition(container.getWidth() / 2 - sequenceWindow.getWidth() / 2, container.getHeight() / 2 - sequenceWindow.getHeight() / 2);
		animOptionsWindow.setPosition(container.getWidth() / 2 - animOptionsWindow.getWidth() / 2, container.getHeight() / 2 - animOptionsWindow.getHeight() / 2);
		settingsWindow.setPosition(container.getWidth() / 2 - settingsWindow.getWidth() / 2, container.getHeight() / 2 - settingsWindow.getHeight() / 2);
		controlsWindow.setPosition(container.getWidth() / 2 - controlsWindow.getWidth() / 2, container.getHeight() / 2 - controlsWindow.getHeight() / 2);
		transformWindow.setPosition(container.getWidth() - transformWindow.getWidth() - 10, container.getHeight() - transformWindow.getHeight() - 360);
	}
}
