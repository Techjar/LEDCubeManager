
package com.techjar.ledcm.util;

import static org.lwjgl.opengl.GL20.*;

import org.lwjgl.util.vector.Vector3f;

/**
 *
 * @author Techjar
 */
public class Material {
    public final Vector3f ambient;
    public final Vector3f diffuse;
    public final Vector3f specular;
    public final float shininess;

    public Material() {
        this.ambient = new Vector3f(0.2F, 0.2F, 0.2F);
        this.diffuse = new Vector3f(0.8F, 0.8F, 0.8F);
        this.specular = new Vector3f(1.0F, 1.0F, 1.0F);
        this.shininess = 20.0F;
    }

    public Material(Vector3f ambient, Vector3f diffuse, Vector3f specular, float shininess) {
        this.ambient = ambient;
        this.diffuse = diffuse;
        this.specular = specular;
        this.shininess = shininess;
    }

    public void sendToShader(int index) {
        glUniform3f(index++, ambient.x, ambient.y, ambient.z);
        glUniform3f(index++, diffuse.x, diffuse.y, diffuse.z);
        glUniform3f(index++, specular.x, specular.y, specular.z);
        glUniform1f(index++, shininess);
    }
}
