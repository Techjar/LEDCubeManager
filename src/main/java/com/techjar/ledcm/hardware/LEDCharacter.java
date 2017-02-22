
package com.techjar.ledcm.hardware;

import com.techjar.ledcm.hardware.manager.LEDManager;
import com.google.common.collect.ImmutableList;
import com.techjar.ledcm.util.Util;
import com.techjar.ledcm.util.math.Vector3;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.Value;
import org.lwjgl.util.ReadableColor;

/**
 *
 * @author Techjar
 */
public class LEDCharacter {
	private static Map<String, Font> fonts = new HashMap<>();
	@Getter private final List<Vector3> vectors;
	@Getter private final String fontName;
	@Getter private final int fontSize;
	@Getter private int thickness;
	@Getter private List<Vector3> thicknessVectors;
	@Getter private List<Vector3> transformedVectors;

	public LEDCharacter(List<Vector3> vectors, String fontName, int fontSize) {
		this.vectors = vectors;
		this.fontName = fontName;
		this.fontSize = fontSize;
		setThickness(1);
	}

	public static LEDCharacter getChar(char ch, String fontName) {
		if (ch < 32 || ch > 127) ch = 127;
		Font font = getFont(fontName);
		return new LEDCharacter(font.getCharacters()[ch - 32], fontName, font.getSize());
	}

	public static LEDCharacter getChar(char ch) {
		return getChar(ch, "standard");
	}

	public void draw(LEDManager ledManager, Colorizer colorizer) {
		List<Vector3> list = transformedVectors == null ? thicknessVectors : transformedVectors;
		for (int i = 0; i < list.size(); i++) {
			Vector3 vec = list.get(i);
			int x = Math.round(vec.getX());
			int y = Math.round(vec.getY());
			int z = Math.round(vec.getZ());
			if (Util.isInsideCube(x, y, z)) {
				Vector3 origVec = transformedVectors == null ? vec : thicknessVectors.get(i);
				ledManager.setLEDColor(x, y, z, colorizer.getColorAt(origVec));
			}
		}
	}

	public void draw(LEDManager ledManager, final ReadableColor color) {
		draw(ledManager, vector -> color);
	}

	/**
	 * Calling this will clear any current transform.
	 */
	public void setThickness(int thickness) {
		if (thickness < 1) throw new IllegalArgumentException("Thickness must be greater than zero!");
		this.thickness = thickness;
		transformedVectors = null;
		thicknessVectors = new ArrayList<>();
		for (Vector3 vec : vectors) {
			for (int i = 0; i < thickness; i++) {
				thicknessVectors.add(new Vector3(i, vec.getY(), vec.getZ()));
			}
		}
	}

	/**
	 * Transformations are cumulative, so if you apply a second it will transform from the positions
	 */
	public void applyTransform(Transformer transformer) {
		List<Vector3> source = transformedVectors == null ? thicknessVectors : transformedVectors;
		List<Vector3> dest = source.stream().map(transformer::transform).collect(Collectors.toList());
		transformedVectors = dest;
	}

	public void clearTransform() {
		transformedVectors = null;
	}

	@SneakyThrows(IOException.class)
	private static Font getFont(String name) {
		if (fonts.containsKey(name)) {
			return fonts.get(name);
		}
		Font font = loadFont(name);
		fonts.put(name, font);
		return font;
	}

	/**
	 * Loads font into an array of Vector3 lists from a texture, using 96 ASCII characters starting from space (32).
	 * Texture should be 128x48 or a multiple thereof, and ordered column-major. 16 characters per row, 6 rows.
	 * We use Vector3 instead of Vector2 because it makes things easier, since we ultimately need to convert to Vector3 anyways for use in the cube.
	 */
	private static Font loadFont(String name) throws IOException {
		File file = new File("resources/textures/ledfont/" + name + ".png");
		if (!file.exists()) throw new IOException("Font \"" + name + "\" does not exist!");
		BufferedImage image = ImageIO.read(file);
		if (!Util.isPowerOfTwo(image.getWidth()) || Math.abs(((float)image.getHeight() / (float)image.getWidth()) - 0.375F) > 0.00001F) {
			throw new RuntimeException("Invalid texture dimensions! Must be 128x48 or a multiple thereof.");
		}
		int size = image.getWidth() / 16;
		List<Vector3>[] array = new List[96];
		for (int y = 0; y < 6; y++) {
			for (int x = 0; x < 16; x++) {
				ImmutableList.Builder<Vector3> list = ImmutableList.builder();
				int[] rgb = image.getRGB(x * size, y * size, size, size, null, 0, size);
				for(int py = 0; py < size; py++) {
					for (int px = 0; px < size; px++) {
						int value = rgb[px + (py * size)] & 0xFFFFFF;
						if (value != 0) {
							list.add(new Vector3(0, (size - 1 - py), px));
						}
					}
				}
				array[x + (y * 16)] = list.build();
			}
		}
		return new Font(array, size);
	}

	@Value public static class Font {
		private List<Vector3>[] characters;
		private int size;
	}

	/**
	 * Transforms the vectors passed to it. Must be stateless!
	 */
	@FunctionalInterface
	public interface Transformer {
		Vector3 transform(Vector3 vector);
	}

	/**
	 * This is passed vectors which account for the thickness but not the transformation. Must be stateless!
	 */
	@FunctionalInterface
	public interface Colorizer {
		ReadableColor getColorAt(Vector3 vector);
	}
}
