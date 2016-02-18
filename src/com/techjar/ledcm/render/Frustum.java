
package com.techjar.ledcm.render;

import static org.lwjgl.opengl.GL11.*;

import java.nio.FloatBuffer;
import lombok.Getter;
import org.lwjgl.BufferUtils;

/**
 * This is pretty much just copied from the internet because matrix math = what
 * @author Techjar
 */
public class Frustum {
    @Getter private float[][] frustum = new float[6][4];
    //private FloatBuffer buffer = BufferUtils.createFloatBuffer(16);

    public void update(float[] projection, float[] view) {
        //float[] projection = new float[16];
        //float[] view = new float[16];
        float[] clip = new float[16];
        float t;

        /*
        // Get the current PROJECTION matrix from OpenGL
        buffer.rewind();
        glGetFloat(GL_PROJECTION_MATRIX, buffer);
        buffer.rewind();
        for (int i = 0; i < 16; i++) projection[i] = buffer.get();

        // Get the current MODELVIEW matrix from OpenGL
        buffer.rewind();
        glGetFloat(GL_MODELVIEW_MATRIX, buffer);
        buffer.rewind();
        for (int i = 0; i < 16; i++) view[i] = buffer.get();
        */

        // Combine the two matrices (multiply projection by view)
        clip[0] = view[0] * projection[0] + view[1] * projection[4] + view[2] * projection[8] + view[3] * projection[12];
        clip[1] = view[0] * projection[1] + view[1] * projection[5] + view[2] * projection[9] + view[3] * projection[13];
        clip[2] = view[0] * projection[2] + view[1] * projection[6] + view[2] * projection[10] + view[3] * projection[14];
        clip[3] = view[0] * projection[3] + view[1] * projection[7] + view[2] * projection[11] + view[3] * projection[15];

        clip[4] = view[4] * projection[0] + view[5] * projection[4] + view[6] * projection[8] + view[7] * projection[12];
        clip[5] = view[4] * projection[1] + view[5] * projection[5] + view[6] * projection[9] + view[7] * projection[13];
        clip[6] = view[4] * projection[2] + view[5] * projection[6] + view[6] * projection[10] + view[7] * projection[14];
        clip[7] = view[4] * projection[3] + view[5] * projection[7] + view[6] * projection[11] + view[7] * projection[15];

        clip[8] = view[8] * projection[0] + view[9] * projection[4] + view[10] * projection[8] + view[11] * projection[12];
        clip[9] = view[8] * projection[1] + view[9] * projection[5] + view[10] * projection[9] + view[11] * projection[13];
        clip[10] = view[8] * projection[2] + view[9] * projection[6] + view[10] * projection[10] + view[11] * projection[14];
        clip[11] = view[8] * projection[3] + view[9] * projection[7] + view[10] * projection[11] + view[11] * projection[15];

        clip[12] = view[12] * projection[0] + view[13] * projection[4] + view[14] * projection[8] + view[15] * projection[12];
        clip[13] = view[12] * projection[1] + view[13] * projection[5] + view[14] * projection[9] + view[15] * projection[13];
        clip[14] = view[12] * projection[2] + view[13] * projection[6] + view[14] * projection[10] + view[15] * projection[14];
        clip[15] = view[12] * projection[3] + view[13] * projection[7] + view[14] * projection[11] + view[15] * projection[15];

        // Extract the numbers for the RIGHT plane
        frustum[0][0] = clip[3] - clip[0];
        frustum[0][1] = clip[7] - clip[4];
        frustum[0][2] = clip[11] - clip[8];
        frustum[0][3] = clip[15] - clip[12];

        // Normalize the result
        t = (float)Math.sqrt(frustum[0][0] * frustum[0][0] + frustum[0][1] * frustum[0][1] + frustum[0][2] * frustum[0][2]);
        frustum[0][0] /= t;
        frustum[0][1] /= t;
        frustum[0][2] /= t;
        frustum[0][3] /= t;

        // Extract the numbers for the LEFT plane
        frustum[1][0] = clip[3] + clip[0];
        frustum[1][1] = clip[7] + clip[4];
        frustum[1][2] = clip[11] + clip[8];
        frustum[1][3] = clip[15] + clip[12];

        // Normalize the result
        t = (float)Math.sqrt(frustum[1][0] * frustum[1][0] + frustum[1][1] * frustum[1][1] + frustum[1][2] * frustum[1][2]);
        frustum[1][0] /= t;
        frustum[1][1] /= t;
        frustum[1][2] /= t;
        frustum[1][3] /= t;

        // Extract the BOTTOM plane
        frustum[2][0] = clip[3] + clip[1];
        frustum[2][1] = clip[7] + clip[5];
        frustum[2][2] = clip[11] + clip[9];
        frustum[2][3] = clip[15] + clip[13];

        // Normalize the result
        t = (float)Math.sqrt(frustum[2][0] * frustum[2][0] + frustum[2][1] * frustum[2][1] + frustum[2][2] * frustum[2][2]);
        frustum[2][0] /= t;
        frustum[2][1] /= t;
        frustum[2][2] /= t;
        frustum[2][3] /= t;

        // Extract the TOP plane
        frustum[3][0] = clip[3] - clip[1];
        frustum[3][1] = clip[7] - clip[5];
        frustum[3][2] = clip[11] - clip[9];
        frustum[3][3] = clip[15] - clip[13];

        // Normalize the result
        t = (float)Math.sqrt(frustum[3][0] * frustum[3][0] + frustum[3][1] * frustum[3][1] + frustum[3][2] * frustum[3][2]);
        frustum[3][0] /= t;
        frustum[3][1] /= t;
        frustum[3][2] /= t;
        frustum[3][3] /= t;

        // Extract the FAR plane
        frustum[4][0] = clip[3] - clip[2];
        frustum[4][1] = clip[7] - clip[6];
        frustum[4][2] = clip[11] - clip[10];
        frustum[4][3] = clip[15] - clip[14];

        // Normalize the result
        t = (float)Math.sqrt(frustum[4][0] * frustum[4][0] + frustum[4][1] * frustum[4][1] + frustum[4][2] * frustum[4][2]);
        frustum[4][0] /= t;
        frustum[4][1] /= t;
        frustum[4][2] /= t;
        frustum[4][3] /= t;

        // Extract the NEAR plane
        frustum[5][0] = clip[3] + clip[2];
        frustum[5][1] = clip[7] + clip[6];
        frustum[5][2] = clip[11] + clip[10];
        frustum[5][3] = clip[15] + clip[14];

        // Normalize the result
        t = (float)Math.sqrt(frustum[5][0] * frustum[5][0] + frustum[5][1] * frustum[5][1] + frustum[5][2] * frustum[5][2]);
        frustum[5][0] /= t;
        frustum[5][1] /= t;
        frustum[5][2] /= t;
        frustum[5][3] /= t;
    }

    /**
     * Check if a sphere is in the view frustum.
     * @param x
     * @param y
     * @param z
     * @param radius
     * @return 0 if not in frustum, 1 if partially in frustum and 2 if fully in frustum
     */
    public int sphereInFrustum(float x, float y, float z, float radius) {
        int c = 0;
        for (int p = 0; p < 6; p++) {
            float d = frustum[p][0] * x + frustum[p][1] * y + frustum[p][2] * z + frustum[p][3];
            if(d <= -radius) return 0;
            if(d > radius) c++;
        }
        return (c == 6) ? 2 : 1;
    }
}
