
package com.techjar.ledcm.hardware.animation;

import com.techjar.ledcm.hardware.LEDUtil;
import com.techjar.ledcm.util.math.Vector3;
import java.util.Random;
import org.lwjgl.util.Color;

/**
 *
 * @author Techjar
 */
public class AnimationTwinkle extends Animation {
	private Random random = new Random();
	private int[] twinkles;
	private Color[] colors;

	public AnimationTwinkle() {
		super();
		twinkles = new int[dimension.x * dimension.y * dimension.z];
		colors = new Color[dimension.x * dimension.y * dimension.z];
	}

	@Override
	public String getName() {
		return "Twinkle";
	}

	@Override
	public synchronized void refresh() {
		for (int i = 0; i < 2; i++) {
			int x = random.nextInt(dimension.x);
			int y = random.nextInt(dimension.y);
			int z = random.nextInt(dimension.z);
			int index = ledManager.encodeVector(x, y, z);
			twinkles[index] = 3 + random.nextInt(5);
			colors[index] = new Color(random.nextInt(ledManager.getResolution() + 1), random.nextInt(ledManager.getResolution() + 1), random.nextInt(ledManager.getResolution() + 1));
		}
		for (int i = 0; i < twinkles.length; i++) {
			Vector3 pos = ledManager.decodeVector(i);
			ledManager.setLEDColorReal((int)pos.getX(), (int)pos.getY(), (int)pos.getZ(), twinkles[i]-- > 0 ? colors[i] : new Color());
		}
	}

	@Override
	public synchronized void reset() {
		LEDUtil.clear(ledManager);
	}
}
