
package com.techjar.ledcm.render;

import static org.lwjgl.opengl.GL20.*;

import com.techjar.ledcm.util.LightSource;
import com.techjar.ledcm.util.ShaderProgram;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.lwjgl.util.vector.Vector3f;

/**
 *
 * @author Techjar
 */
public class LightingHandler {
    public static final int MAX_LIGHTS = 10;
    private List<LightSource> lights = new ArrayList<>();
    @Getter @Setter private Vector3f sceneAmbient = new Vector3f(0.2F, 0.2F, 0.2F);

    public void sendToShader() {
        ShaderProgram program = ShaderProgram.getCurrent();
        glUniform3f(4, sceneAmbient.x, sceneAmbient.y, sceneAmbient.z);
        glUniform1i(5, lights.size());
        for (int i = 0; i < lights.size(); i++) {
            LightSource light = lights.get(i);
            light.sendToShader(6, i);
        }
    }

    public LightSource getLight(int index) {
        return lights.get(index);
    }

    public void addLight(LightSource light) {
        if (lights.size() >= MAX_LIGHTS) throw new IllegalStateException("Maximum light count reached: " + MAX_LIGHTS);
        lights.add(light);
    }

    public boolean removeLight(LightSource light) {
        return lights.remove(light);
    }

    public int getLightCount() {
        return lights.size();
    }
}
