
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
	public final float emissivity;

	public Material() {
		this(null, null, null, 20.0F, 0.0F);
	}

	public Material(Vector3f ambient, Vector3f diffuse, Vector3f specular, float shininess, float emissivity) {
		this.ambient = ambient != null ? ambient : new Vector3f(0.2F, 0.2F, 0.2F);
		this.diffuse = diffuse != null ? diffuse : new Vector3f(0.8F, 0.8F, 0.8F);
		this.specular = specular != null ? specular : new Vector3f(1.0F, 1.0F, 1.0F);
		this.shininess = shininess;
		this.emissivity = emissivity;
	}

	public void sendToShader(String uniform) {
		ShaderProgram program = ShaderProgram.getCurrent();
		glUniform3f(program.getUniformLocation(uniform + ".ambient"), ambient.x, ambient.y, ambient.z);
		glUniform3f(program.getUniformLocation(uniform + ".diffuse"), diffuse.x, diffuse.y, diffuse.z);
		glUniform3f(program.getUniformLocation(uniform + ".specular"), specular.x, specular.y, specular.z);
		glUniform1f(program.getUniformLocation(uniform + ".shininess"), shininess);
		glUniform1f(program.getUniformLocation(uniform + ".emissivity"), emissivity);
	}
}
