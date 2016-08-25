
package com.techjar.ledcm.render.pipeline;

import org.newdawn.slick.UnicodeFont;

import com.hackoeur.jglm.Matrices;
import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.gui.screen.Screen;
import com.techjar.ledcm.render.InstancedRenderer;
import com.techjar.ledcm.util.ShaderProgram;
import com.techjar.ledcm.util.Tuple;
import com.techjar.ledcm.util.Util;

/**
 *
 * @author Techjar
 */
public class RenderPipelineGUI implements RenderPipeline {
	public RenderPipelineGUI() {
	}

	@Override
	public void update() {

	}

	@Override
	public void render3D() {
	}

	@Override
	public void render2D() {
		LEDCubeManager ledcm = LEDCubeManager.getInstance();

		for (Screen screen : ledcm.getScreenList())
			if (screen.isVisible()) screen.render();

		UnicodeFont debugFont = LEDCubeManager.getFontManager().getFont("chemrea", 20, false, false).getUnicodeFont();
		org.newdawn.slick.Color infoColor = org.newdawn.slick.Color.yellow;
		int y = 0;
		if (ledcm.renderFPS || ledcm.debugMode) {
			debugFont.drawString(5, 5 + y++ * 25, "FPS: " + ledcm.getFpsRender(), infoColor);
			debugFont.drawString(5, 5 + y++ * 25, "Animation FPS: " + LEDCubeManager.getLEDCube().getCommThread().getFPS(), infoColor);
		}
		for (Tuple<String, Integer> tuple : ledcm.getDebugText()) {
			debugFont.drawString(5, (y++ * 25) + 5, tuple.getA(), infoColor);
		}
	}

	@Override
	public void loadShaders() {
	}
}
