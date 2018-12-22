package com.techjar.ledcm.render;

import com.hackoeur.jglm.Matrices;
import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.util.Model;
import com.techjar.ledcm.util.ShaderProgram;
import com.techjar.ledcm.util.Util;
import com.techjar.ledcm.util.math.Angle;
import com.techjar.ledcm.util.math.Quaternion;
import com.techjar.ledcm.util.math.Vector3;
import lombok.Setter;
import org.lwjgl.util.Color;
import org.lwjgl.util.vector.Matrix4f;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL41.*;

public class BloomProcessor {
	private final int width;
	private final int height;
	@Setter private int baseFramebuffer;
	@Setter private int bloomTexture;
	private int[] pingpongFBO = new int[2];
	private int[] pingpongTexture = new int[2];
	private int[] intermediateFBO = new int[2];
	private int[] intermediateTexture = new int[2];
	private ShaderProgram blurShader;
	private ShaderProgram blendShader;
	private Model planeModel;

	public BloomProcessor(int baseFramebuffer, int bloomTexture, int width, int height) {
		this.baseFramebuffer = baseFramebuffer;
		this.bloomTexture = bloomTexture;
		this.width = width;
		this.height = height;
		init();
	}

	private void init() {
		for (int i = 0; i < 2; i++) {
			pingpongFBO[i] = glGenFramebuffers();
			pingpongTexture[i] = glGenTextures();

			glBindFramebuffer(GL_FRAMEBUFFER, pingpongFBO[i]);
			glBindTexture(GL_TEXTURE_2D, pingpongTexture[i]);
			glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB16F, width, height, 0, GL_RGB, GL_FLOAT, (ByteBuffer)null);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
			glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, pingpongTexture[i], 0);

			intermediateFBO[i] = glGenFramebuffers();
			intermediateTexture[i] = glGenTextures();

			glBindFramebuffer(GL_FRAMEBUFFER, intermediateFBO[i]);
			glBindTexture(GL_TEXTURE_2D, intermediateTexture[i]);
			glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB16F, width, height, 0, GL_RGB, GL_FLOAT, (ByteBuffer)null);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
			glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, intermediateTexture[i], 0);
		}

		planeModel = LEDCubeManager.getModelManager().getModel("plane.model");
		loadShaders();
	}

	public void loadShaders() {
		blurShader = new ShaderProgram().loadShader("main_nolighting", GL_VERTEX_SHADER_BIT).loadShader("blur").link();
		blendShader = new ShaderProgram().loadShader("main_nolighting", GL_VERTEX_SHADER_BIT).loadShader("blend").link();
	}

	public void apply(int amount) {
		LEDCubeManager ledcm = LEDCubeManager.getInstance();

		glBindFramebuffer(GL_READ_FRAMEBUFFER, baseFramebuffer);
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, intermediateFBO[0]);
		glReadBuffer(GL_COLOR_ATTACHMENT0);
		glBlitFramebuffer(0, 0, width, height, 0, 0, width, height, GL_COLOR_BUFFER_BIT, GL_NEAREST);
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, intermediateFBO[1]);
		glReadBuffer(GL_COLOR_ATTACHMENT1);
		glBlitFramebuffer(0, 0, width, height, 0, 0, width, height, GL_COLOR_BUFFER_BIT, GL_NEAREST);
		glReadBuffer(GL_COLOR_ATTACHMENT0);

		glDisable(GL_DEPTH_TEST);
		glDepthMask(false);
		ledcm.resizeGL(width, height);
		blurShader.use();
		for (int i = 0; i < amount * 2; i++) {
			glBindFramebuffer(GL_FRAMEBUFFER, pingpongFBO[1 - (i % 2)]);
			glBindTexture(GL_TEXTURE_2D, i == 0 ? intermediateTexture[1] : pingpongTexture[i % 2]);
			glUniform1i(blurShader.getUniformLocation("horizontal"), 1 - (i % 2));

			LEDCubeManager.sendMatrixToProgram(Util.convertMatrix(Matrices.ortho(0, width, height, 0, -1, 1)), new Matrix4f());
			planeModel.render(new Vector3(width / 2, height / 2, 0), new Quaternion(90, 0, 0, Angle.Order.XYZ), new Color(), new Vector3(width, 0, height), false, false, -1);
		}

		blendShader.use();
		glBindFramebuffer(GL_FRAMEBUFFER, baseFramebuffer);
		glBindTexture(GL_TEXTURE_2D, intermediateTexture[0]);
		glActiveTexture(GL_TEXTURE1);
		glBindTexture(GL_TEXTURE_2D, pingpongTexture[0]);
		glActiveTexture(GL_TEXTURE0);

		LEDCubeManager.sendMatrixToProgram(Util.convertMatrix(Matrices.ortho(0, width, height, 0, -1, 1)), new Matrix4f());
		planeModel.render(new Vector3(width / 2, height / 2, 0), new Quaternion(90, 0, 0, Angle.Order.XYZ), new Color(), new Vector3(width, 0, height), false, false, -1);

		glEnable(GL_DEPTH_TEST);
		glDepthMask(true);
		ShaderProgram.useNone();
	}

	public void cleanup() {
		for (int i = 0; i < 2; i++) {
			glDeleteFramebuffers(pingpongFBO[i]);
			glDeleteTextures(pingpongTexture[i]);
		}
		blurShader.release();
		blendShader.release();
	}
}
