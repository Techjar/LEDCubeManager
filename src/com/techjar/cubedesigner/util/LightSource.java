
package com.techjar.cubedesigner.util;

import static org.lwjgl.opengl.GL20.*;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

/**
 *
 * @author Techjar
 */
public class LightSource {
    public Vector4f ambient = new Vector4f(0, 0, 0, 1);
    public Vector4f diffuse = new Vector4f(1, 1, 1, 1);
    public Vector4f specular = new Vector4f(1, 1, 1, 1);
    public Vector4f position = new Vector4f(0, 0, 1, 0);
    public Vector3f spotDirection = new Vector3f(0, 0, -1);
    public float spotExponent = 0;
    public float spotCutoff = 180;
    public float constantAttenuation = 1;
    public float linearAttenuation = 0;
    public float quadraticAttenuation = 0;

    public void sendToShader(int index, int arrayIndex) {
        index += arrayIndex * 10;
        glUniform4f(index++, ambient.x, ambient.y, ambient.z, ambient.w);
        glUniform4f(index++, diffuse.x, diffuse.y, diffuse.z, diffuse.w);
        glUniform4f(index++, specular.x, specular.y, specular.z, specular.w);
        glUniform4f(index++, position.x, position.y, position.z, position.w);
        glUniform3f(index++, spotDirection.x, spotDirection.y, spotDirection.z);
        glUniform1f(index++, spotExponent);
        glUniform1f(index++, spotCutoff);
        glUniform1f(index++, constantAttenuation);
        glUniform1f(index++, linearAttenuation);
        glUniform1f(index++, quadraticAttenuation);
    }
}
