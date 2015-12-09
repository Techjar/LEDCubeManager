
package com.techjar.ledcm.render.pipeline;

import com.hackoeur.jglm.Matrices;
import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.render.InstancedRenderer;
import com.techjar.ledcm.util.ShaderProgram;

/**
 *
 * @author Techjar
 */
public class RenderPipelineStandard implements RenderPipeline {
    private ShaderProgram mainShader;

    public RenderPipelineStandard() {
    }

    @Override
    public int get3DPasses() {
        return 1;
    }

    @Override
    public int get2DPasses() {
        return 0;
    }

    @Override
    public void render3D(int pass) {
        LEDCubeManager ledcm = LEDCubeManager.getInstance();

        mainShader.use();
        ledcm.setupView(Matrices.perspective(ledcm.getFieldOfView(), (float)LEDCubeManager.getDisplayMode().getWidth() / (float)LEDCubeManager.getDisplayMode().getHeight(), 0.1F, ledcm.getViewDistance()), LEDCubeManager.getCamera().getPosition(), LEDCubeManager.getCamera().getAngle());
        ledcm.sendMatrixToProgram();
        ledcm.getLightingHandler().sendToShader();

        InstancedRenderer.prepareItems();
        InstancedRenderer.renderAll();
        ShaderProgram.useNone();
    }

    @Override
    public void render2D(int pass) {
    }

    @Override
    public void loadShaders() {
        mainShader = new ShaderProgram().loadShader("main").link();
    }
}
