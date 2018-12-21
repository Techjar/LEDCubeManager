package com.techjar.ledcm.gui;

import static org.lwjgl.opengl.GL11.*;

import com.techjar.ledcm.gui.GUIScrollBox.ScrollMode;
import com.techjar.ledcm.render.RenderHelper;
import com.techjar.ledcm.util.Timer;
import com.techjar.ledcm.util.Util;
import com.techjar.ledcm.vr.VRInputEvent;
import org.lwjgl.input.Controller;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.Color;
import org.lwjgl.util.Dimension;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Shape;

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.util.*;

public class GUIFileChooser extends GUIWindow {
	public static final int FILES_ONLY = 0;
	public static final int DIRECTORIES_ONLY = 1;
	public static final int FILES_AND_DIRECTORIES = 2;

	protected UnicodeFont font;
	protected Color color;
	protected GUIBackground scrollBoxBg;
	protected List<FileFilter> filters = new ArrayList<>();
	//protected GUIWindow window;
	protected GUIScrollBox scrollBox;
	protected GUIComboBox fileFilterComboBox;
	protected GUIComboBox rootDirsComboBox;
	//protected GUITextField fileNameTextField;
	protected GUIButton chooseBtn;
	//protected GUIButton cancelBtn;
	protected int buttonHeight;
	protected boolean multiSelection;
	protected int selectionMode = FILES_AND_DIRECTORIES;

	protected File currentDir;
	protected boolean saveDialog;
	protected Set<File> selectedFiles = new HashSet<>();
	protected List<File> listedFiles = new ArrayList<>();
	protected GUICallback chooseCallback;

	public GUIFileChooser(UnicodeFont font, Color color, Color buttonBorderColor, int buttonHeight, GUIBackground guiBg, GUIBackground scrollBoxBg) {
		super(guiBg);
		this.font = font;
		this.color = color;
		this.buttonHeight = buttonHeight;
		this.scrollBoxBg = scrollBoxBg;
		scrollBox = new GUIScrollBox(scrollBoxBg.borderColor, scrollBoxBg.bgColor, scrollBoxBg);
		//scrollBox.setScrollXMode(ScrollMode.DISABLED);
		scrollBox.setPosition(5 + scrollBoxBg.borderSize, 10 + scrollBoxBg.borderSize + buttonHeight);
		addComponent(scrollBox);
		fileFilterComboBox = new GUIComboBox(font, color, new GUIBackground(scrollBoxBg.bgColor, scrollBoxBg.borderColor, scrollBoxBg.borderSize, scrollBoxBg.texture));
		fileFilterComboBox.setParentAlignment(GUIAlignment.BOTTOM_LEFT);
		fileFilterComboBox.setPosition(5, -5);
		fileFilterComboBox.setDimension(0, buttonHeight);
		fileFilterComboBox.setChangeHandler(component -> {
			updateFileList();
		});
		addComponent(fileFilterComboBox);
		rootDirsComboBox = new GUIComboBox(font, color, new GUIBackground(scrollBoxBg.bgColor, scrollBoxBg.borderColor, scrollBoxBg.borderSize, scrollBoxBg.texture));
		rootDirsComboBox.setPosition(5, 5);
		rootDirsComboBox.setDimension(0, buttonHeight);
		for (File dir : File.listRoots()) {
			rootDirsComboBox.addItem(dir.getPath());
		}
		File workingDir = new File(System.getProperty("user.dir"));
		for (Object obj : rootDirsComboBox.getAllItems()) {
			if (workingDir.getPath().startsWith(obj.toString())) {
				rootDirsComboBox.setSelectedItem(obj);
				break;
			}
		}
		rootDirsComboBox.setChangeHandler(component -> {
			setCurrentDirectory(new File(rootDirsComboBox.getSelectedItem().toString()));
		});
		addComponent(rootDirsComboBox);
		chooseBtn = new GUIButton(font, color, "Open", new GUIBackground(guiBg.borderColor, buttonBorderColor, guiBg.borderSize, guiBg.texture));
		chooseBtn.setParentAlignment(GUIAlignment.BOTTOM_RIGHT);
		chooseBtn.setPosition(-5, -5);
		chooseBtn.setDimension(0, buttonHeight);
		chooseBtn.setClickHandler(component -> {
			if (selectedFiles.size() == 1) {
				File file = selectedFiles.iterator().next();
				if (file.isDirectory()) {
					setCurrentDirectory(file);
					return;
				}
			}
			chooseFile();
		});
		addComponent(chooseBtn);
		currentDir = workingDir;
		updateFilterList();
		visible = false;
	}

	@Override
	public void update(float delta) {
		super.update(delta);
		if (!visible) chooseCallback = null;
	}

	@Override
	public void setDimension(Dimension dimension) {
		super.setDimension(dimension);
		updateDimensions();
	}

	protected void updateDimensions() {
		Rectangle rect = getContainerBox();
		scrollBox.setDimension((int)rect.getWidth() - 10 - scrollBoxBg.borderSize * 2, (int)rect.getHeight() - buttonHeight * 2 - 20 - scrollBoxBg.borderSize * 2);
		fileFilterComboBox.setDimension((int)rect.getWidth() - 15 - 100, buttonHeight);
		rootDirsComboBox.setDimension((int)rect.getWidth() - 10, buttonHeight);
		chooseBtn.setDimension(100, buttonHeight);
		updateFileList();
	}

	public void showOpenDialog(GUICallback callback) {
		chooseBtn.setText("Open");
		chooseCallback = callback;
		saveDialog = false;
		setVisible(true);
	}

	public void showSaveDialog(GUICallback callback) {
		chooseBtn.setText("Save");
		chooseCallback = callback;
		saveDialog = true;
		setVisible(true);
	}

	public File getSelectedFile() {
		return selectedFiles.iterator().next();
	}

	public File[] getSelectedFiles() {
		return selectedFiles.toArray(new File[selectedFiles.size()]);
	}

	public void setSelectedFile(File file) {
		if (!file.getParentFile().equals(currentDir))
			setCurrentDirectory(file.getParentFile());
		selectedFiles.clear();
		selectedFiles.add(file);
	}

	public void setSelectedFiles(File[] files) {
		if (files.length < 1) {
			selectedFiles.clear();
			return;
		}
		File parent = files[0].getParentFile();
		for (File file : files) {
			if (!file.getParentFile().equals(parent)) throw new IllegalArgumentException("Cannot select files from multiple directories");
		}
		if (!parent.equals(currentDir))
			setCurrentDirectory(parent);
		selectedFiles.clear();
		selectedFiles.addAll(Arrays.asList(files));
	}

	public File getCurrentDirectory() {
		return currentDir;
	}

	public void setCurrentDirectory(File directory) {
		if (directory == null || !directory.exists()) currentDir = new File(System.getProperty("user.dir"));
		else if (!directory.isDirectory()) throw new IllegalArgumentException("Not a directory");
		else currentDir = directory;
		updateFileList();
	}

	public int getFileSelectionMode() {
		return selectionMode;
	}

	public void setFileSelectionMode(int selectionMode) {
		this.selectionMode = selectionMode;
	}

	public FileFilter[] getFileFilters(FileFilter filter) {
		return filters.toArray(new FileFilter[filters.size()]);
	}

	public void addFileFilter(FileFilter filter) {
		filters.add(filter);
		updateFilterList();
	}

	public boolean removeFileFilter(FileFilter filter) {
		boolean ret = filters.remove(filter);
		updateFilterList();
		return ret;
	}

	public void clearFileFilters(FileFilter filter) {
		filters.clear();
		updateFilterList();
	}

	public boolean isMultiSelectionEnabled() {
		return multiSelection;
	}

	public void setMultiSelectionEnabled(boolean multiSelection) {
		this.multiSelection = multiSelection;
	}

	protected void updateFileList() {
		listedFiles.clear();
		selectedFiles.clear();
		scrollBox.removeAllComponents();
		scrollBox.setScrollOffset(0, 0);
		int offset = 0;
		if (currentDir.getParentFile() != null) {
			FileItem parentItem = createItem(currentDir.getParentFile(), true);
			scrollBox.addComponent(parentItem);
			offset += buttonHeight;
			listedFiles.add(currentDir.getParentFile());
		}
		File[] files = currentDir.listFiles();
		if (files != null) {
			Arrays.sort(files, (File f1, File f2) -> {
				if (f1.isDirectory() && !f2.isDirectory())
					return -1;
				if (!f1.isDirectory() && f2.isDirectory())
					return 1;
				return f1.compareTo(f2);
			});
			for (int i = 0; i < files.length; i++) {
				File file = files[i];
				if (selectionMode == FILES_ONLY && file.isDirectory()) {
					offset -= buttonHeight;
					continue;
				}
				if (selectionMode == DIRECTORIES_ONLY && !file.isDirectory()) {
					offset -= buttonHeight;
					continue;
				}
				if (!file.isDirectory() && fileFilterComboBox.getSelectedIndex() > 0) {
					if (!filters.get(fileFilterComboBox.getSelectedIndex() - 1).accept(file)) {
						offset -= buttonHeight;
						continue;
					}
				}
				FileItem item = createItem(file, false);
				item.setY(i * buttonHeight + offset);
				scrollBox.addComponent(item);
				listedFiles.add(file);
			}
		}
	}

	protected void updateFilterList() {
		fileFilterComboBox.clearItems();
		fileFilterComboBox.addItem("All Files");
		for (FileFilter filter : filters) {
			fileFilterComboBox.addItem(filter.getDescription());
		}
		fileFilterComboBox.setSelectedItem(filters.size() > 0 ? 1 : 0);
	}

	protected void chooseFile() {
		if (selectedFiles.size() > 0) {
			setVisible(false);
			if (chooseCallback != null) {
				chooseCallback.run(this);
				chooseCallback = null;
			}
		}
	}

	protected FileItem createItem(File file, boolean parent) {
		FileItem newItem = new FileItem(font, color, Util.addColors(scrollBoxBg.getBackgroundColor(), new Color(50, 50, 50)), Util.addColors(scrollBoxBg.getBackgroundColor(), new Color(25, 25, 25)), file, parent);
		newItem.setDimension(Math.max(scrollBox.getWidth() - scrollBox.getScrollbarWidth() - 2, font.getWidth(file.getName())), buttonHeight);
		return newItem;
	}

	protected class FileItem extends GUIText {
		protected File file;
		protected Color hoverBgColor;
		protected Color selectedBgColor;
		protected boolean parentDir;

		protected boolean pressed;
		protected boolean clicked;
		protected Timer clickTimer = new Timer();

		public FileItem(UnicodeFont font, Color color, Color hoverBgColor, Color selectedBgColor, File file, boolean parentDir) {
			super(font, color, parentDir ? "../" : file.getName() + (file.isDirectory() ? "/" : ""));
			this.file = file;
			this.hoverBgColor = hoverBgColor;
			this.selectedBgColor = selectedBgColor;
			this.parentDir = parentDir;
		}

		@Override
		protected boolean mouseEvent(int button, boolean state, int dwheel) {
			if (button == 0) {
				if (state) {
					Rectangle box = new Rectangle(getPosition().getX(), getPosition().getY(), dimension.getWidth(), dimension.getHeight());
					if (checkMouseIntersect(box)) {
						pressed = true;
						if (multiSelection && !saveDialog) {
							if (keyState[Keyboard.KEY_LCONTROL] || keyState[Keyboard.KEY_RCONTROL]) {
								if (selectedFiles.contains(file)) selectedFiles.remove(file);
								else selectedFiles.add(file);
								return false;
							} else if (keyState[Keyboard.KEY_LSHIFT] || keyState[Keyboard.KEY_RSHIFT]) {
								int selIndex = listedFiles.indexOf(file);
								int minIndex = Integer.MAX_VALUE;
								int maxIndex = Integer.MIN_VALUE;
								for (File selFile : selectedFiles) {
									int index = listedFiles.indexOf(selFile);
									if (index > maxIndex) maxIndex = index;
									if (index < minIndex) minIndex = index;
								}
								if (selIndex > maxIndex) {
									for (int i = minIndex; i <= selIndex; i++)
										selectedFiles.add(listedFiles.get(i));
								} else {
									for (int i = maxIndex; i >= selIndex; i--)
										selectedFiles.add(listedFiles.get(i));
								}
								return false;
							}
						}
						selectedFiles.clear();
						selectedFiles.add(file);
						if (clicked && clickTimer.getMilliseconds() <= 500) {
							if (file.isDirectory()) setCurrentDirectory(file);
							else chooseFile();
						}
						clickTimer.restart();
						clicked = true;
						return false;
					} else clicked = false;
				}
				else pressed = false;
			}
			return true;
		}

		@Override
		public void render() {
			Shape box = getComponentBox();
			if (checkMouseIntersect(box)) RenderHelper.drawSquare(getPosition().getX(), getPosition().getY(), dimension.getWidth(), dimension.getHeight(), hoverBgColor);
			else if (selectedFiles.contains(file)) RenderHelper.drawSquare(getPosition().getX(), getPosition().getY(), dimension.getWidth(), dimension.getHeight(), selectedBgColor);
			glTranslatef(3, 0, 0);
			super.render();
			glTranslatef(-3, 0, 0);
		}

		public File getFile() {
			return file;
		}
	}
}
