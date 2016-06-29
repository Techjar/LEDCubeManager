package com.techjar.ledcm.render;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.util.Util;
import static org.lwjgl.opengl.GL11.*;

import com.techjar.ledcm.util.logging.LogHelper;
import java.nio.FloatBuffer;
import java.util.Deque;
import java.util.LinkedList;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.Color;
import org.lwjgl.util.vector.Matrix4f;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.opengl.Texture;

/**
 *
 * @author Techjar
 */
public final class RenderHelper {
    private static final Deque<Rectangle> scissorStack = new LinkedList<>();
    private static final FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);

    private RenderHelper() {
    }

    public static void setGlColor(Color color) {
        glColor4ub(color.getRedByte(), color.getGreenByte(), color.getBlueByte(), color.getAlphaByte());
    }

    public static void drawSquare(float x, float y, float width, float height, Color color, Texture texture) {
        if (texture == null) glDisable(GL_TEXTURE_2D);
        glTranslatef(x, y, 0);
        if (color != null) {
            setGlColor(color);
        } else glColor4f(1, 1, 1, 1);
        glBegin(GL_QUADS);
        if (texture != null) glTexCoord2f(0, 0); glVertex2f(0, 0);
        if (texture != null) glTexCoord2f(0, texture.getHeight()); glVertex2f(0, height);
        if (texture != null) glTexCoord2f(texture.getWidth(), texture.getHeight()); glVertex2f(width, height);
        if (texture != null) glTexCoord2f(texture.getWidth(), 0); glVertex2f(width, 0);
        glEnd();
        glTranslatef(-x, -y, 0);
        if (texture == null) glEnable(GL_TEXTURE_2D);
    }
    
    public static void drawSquare(float x, float y, float width, float height, Color color) {
        drawSquare(x, y, width, height, color, null);
    }
    
    public static void drawSquare(float x, float y, float width, float height, Texture texture) {
        drawSquare(x, y, width, height, null, texture);
    }
    
    public static void drawBorder(float x, float y, float width, float height, float thickness, Color color, boolean top, boolean bottom, boolean left, boolean right) {
        if (left) drawSquare(x, y, thickness, height, color);
        if (top) drawSquare(x, y, width, thickness, color);
        if (right) drawSquare(x + width - thickness, y, thickness, height, color);
        if (bottom) drawSquare(x, y + height - thickness, width, thickness, color);
    }

    public static void drawBorder(float x, float y, float width, float height, float thickness, Color color) {
        drawBorder(x, y, width, height, thickness, color, true, true, true, true);
    }

    public static void drawFace(float x, float y, float z, float width, float height, float normalX, float normalY, float normalZ, Texture texture) {
        throw new UnsupportedOperationException("fuck it, use models");
        /*glTranslatef(x, y, z);
        glBegin(GL_TRIANGLES);
        if (texture != null) glTexCoord2f(0, 0); glNormal3f(normalX, normalY, normalZ); glVertex3f(0, 0, 0);
        if (texture != null) glTexCoord2f(texture.getWidth(), 0); glNormal3f(normalX, normalY, normalZ); glVertex3f(0, 0, 0);
        if (texture != null) glTexCoord2f(texture.getWidth(), texture.getHeight()); glNormal3f(normalX, normalY, normalZ); glVertex3f(0, 0, 0);
        if (texture != null) glTexCoord2f(0, texture.getHeight()); glVertex2f(0, height); glNormal3f(normalX, normalY, normalZ); glVertex3f(0, 0, 0);
        glEnd();
        glTranslatef(-x, -y, -z);*/
    }

    public static void drawCube(float x, float y, float z, float width, float length, float height) {
        throw new UnsupportedOperationException("fuck it, use models");
    }

    public static void matrixMultiply(Matrix4f matrix) {
        matrixBuffer.rewind();
        matrix.store(matrixBuffer);
        matrixBuffer.rewind();
        glMultMatrix(matrixBuffer);
    }

    public static Matrix4f getMatrix(int pname) {
        matrixBuffer.rewind();
        glGetFloat(pname, matrixBuffer);
        matrixBuffer.rewind();
        Matrix4f matrix = new Matrix4f();
        matrix.load(matrixBuffer);
        return matrix;
    }

    private static void performScissor(Rectangle rect) {
        glScissor((int)rect.getX(), LEDCubeManager.getHeight() - (int)rect.getHeight() - (int)rect.getY(), (int)rect.getWidth(), (int)rect.getHeight());
    }
    
    public static void beginScissor(Rectangle rect, boolean clipToPrevious) {
        if (scissorStack.isEmpty()) glEnable(GL_SCISSOR_TEST);
        else if (clipToPrevious) rect = Util.clipRectangle(rect, scissorStack.peek());
        scissorStack.push(rect);
        performScissor(rect);
    }

    public static void beginScissor(Rectangle rect) {
        beginScissor(rect, true);
    }
    
    public static void endScissor() {
        if (!scissorStack.isEmpty()) {
            scissorStack.pop();
            if (scissorStack.isEmpty()) glDisable(GL_SCISSOR_TEST);
            else performScissor(scissorStack.peek());
        }
    }

    public static Rectangle getPreviousScissor() {
        if (!scissorStack.isEmpty()) {
            Rectangle prev = scissorStack.peek();
            return new Rectangle(prev.getX(), prev.getY(), prev.getWidth(), prev.getHeight());
        }
        return null;
    }
}
