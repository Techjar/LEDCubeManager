
package com.techjar.ledcm.util;

import static org.lwjgl.opengl.GL20.*;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

/**
 *
 * @author Techjar
 */
public class LightSource {
    public Vector3f diffuse = new Vector3f(1.0F, 1.0F, 1.0F);
    public Vector3f specular = new Vector3f(1.0F, 1.0F, 1.0F);
    public Vector4f position = new Vector4f(0.0F, 0.0F, 1.0F, 0.0F);
    public Vector3f spotDirection = new Vector3f(0.0F, 0.0F, -1.0F);
    public float spotExponent = 0.0F;
    public float spotCutoff = 180.0F;
    public float constantAttenuation = 1.0F;
    public float linearAttenuation = 0.0F;
    public float quadraticAttenuation = 0.0F;
    public float brightness = 1.0F;

    public void sendToShader(int index, int arrayIndex) {
        index += arrayIndex * 10;
        brightness = Math.max(brightness, Float.MIN_VALUE);
        glUniform3f(index++, diffuse.x, diffuse.y, diffuse.z);
        glUniform3f(index++, specular.x, specular.y, specular.z);
        glUniform4f(index++, position.x, position.y, position.z, position.w);
        glUniform3f(index++, spotDirection.x, spotDirection.y, spotDirection.z);
        glUniform2f(index++, spotExponent, spotCutoff);
        glUniform3f(index++, constantAttenuation / brightness, linearAttenuation / brightness, quadraticAttenuation / brightness);
    }
}
