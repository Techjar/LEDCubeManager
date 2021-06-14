
package com.techjar.ledcm.hardware.animation;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.util.math.Direction;
import com.techjar.ledcm.util.math.Vector3;
import org.lwjgl.util.Color;
import org.lwjgl.util.ReadableColor;

import java.util.LinkedList;
import java.util.Random;

/**
 *
 * @author Techjar
 */
public class AnimationFloodFill extends Animation {
	private Random random = new Random();
	private Vector3 startPoint = null;
	private LinkedList<Vector3> points = new LinkedList<>();
	private int colorMode = 0;
	private int startPointMode = 0;
	private int speed = 3;

	public AnimationFloodFill() {
		super();
	}

	@Override
	public String getName() {
		return "Flood Fill";
	}

	@Override
	public synchronized void refresh() {
		if (startPoint == null) {
			tryPopulatePoints();
		}

		if (ticks % speed == 0) {
			Vector3 pos = points.poll();
			if (pos != null) {
				Color color = getColor(pos);
				ledManager.setLEDColor((int)pos.getX(), (int)pos.getY(), (int)pos.getZ(), color);
			}
		}
	}

	@Override
	public synchronized void reset() {
		startPoint = null;
		points.clear();
	}

	@Override
	public boolean isFinished() {
		return startPoint != null && points.isEmpty();
	}

	@Override
	public AnimationOption[] getOptions() {
		return new AnimationOption[]{
				new AnimationOption("colormode", "Color", AnimationOption.OptionType.COMBOBOX, new Object[]{colorMode, 0, "Picker", 1, "Hue Spread", 2, "Random"}),
				new AnimationOption("startpointmode", "Start", AnimationOption.OptionType.COMBOBOX, new Object[]{startPointMode, 0, "Random", 1, "First Lit"}),
				new AnimationOption("speed", "Speed", AnimationOption.OptionType.SLIDER, new Object[]{(19 - (speed - 1)) / 19F, 1F / 19F}),
		};
	}

	@Override
	public synchronized void optionChanged(String name, String value) {
		switch (name) {
			case "colormode":
				colorMode = Integer.parseInt(value);
				break;
			case "startpointmode":
				startPointMode = Integer.parseInt(value);
				break;
			case "speed":
				speed = 1 + (19 - Math.round(19 * Float.parseFloat(value)));
				break;
		}
	}

	private Color getColor(Vector3 pos) {
		if (colorMode == 0) {
			return LEDCubeManager.getPaintColor();
		} else if (colorMode == 1) {
			int largestDim = Math.max(Math.max(dimension.x, dimension.y), dimension.z);
			Color color = new Color();
			color.fromHSB(((Math.abs(pos.distance(startPoint)) / largestDim) * (300F / 360F)) % 1, 1, 1);
			return color;
		}
		return new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
	}

	private void tryPopulatePoints() {
		if (startPointMode == 0) {
			startPoint = new Vector3(random.nextInt(dimension.x), random.nextInt(dimension.y), random.nextInt(dimension.z));
		} else if (startPointMode == 1) {
			outer: for (int z = 0; z < dimension.z; z++) {
				for (int y = 0; y < dimension.y; y++) {
					for (int x = 0; x < dimension.x; x++) {
						Color color = ledManager.getLEDColor(x, y, z);
						if (color.getRed() > 0 || color.getGreen() > 0 || color.getBlue() > 0) {
							startPoint = new Vector3(x, y, z);
							break outer;
						}
					}
				}
			}
		}

		if (startPoint != null) {
			LinkedList<Vector3> stack = new LinkedList<>();
			stack.push(startPoint);
			LinkedList<Vector3> nextStack = new LinkedList<>();
			boolean[] visited = new boolean[ledManager.getLEDCount()];
			while (true) {
				if (stack.isEmpty()) {
					if (nextStack.isEmpty())
						break;
					stack = nextStack;
					nextStack = new LinkedList<>();
				}

				Vector3 current = stack.pop();
				points.add(current);
				for (int i = 0; i < 6; i++) {
					Vector3 offset = Direction.values()[i].getVector();
					Vector3 node = current.add(offset);
					if (node.getX() >= 0 && node.getX() < dimension.x && node.getY() >= 0 && node.getY() < dimension.y && node.getZ() >= 0 && node.getZ() < dimension.z && !visited[ledManager.encodeVector(node)]) {
						nextStack.push(node);
						visited[ledManager.encodeVector(node)] = true;
					}
				}
			}
		}
	}
}
