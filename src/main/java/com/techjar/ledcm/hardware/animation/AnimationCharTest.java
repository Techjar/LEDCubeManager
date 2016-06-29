
package com.techjar.ledcm.hardware.animation;

import com.techjar.ledcm.hardware.LEDCharacter;
import com.techjar.ledcm.hardware.LEDUtil;
import com.techjar.ledcm.util.Timer;
import com.techjar.ledcm.util.Util;
import com.techjar.ledcm.util.Vector2;
import com.techjar.ledcm.util.Vector3;
import org.lwjgl.util.Color;
import org.lwjgl.util.ReadableColor;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

/**
 *
 * @author Techjar
 */
public class AnimationCharTest extends Animation {
    private Timer timer = new Timer();

    public AnimationCharTest() {
        super();
    }

    @Override
    public String getName() {
        return "Character Test";
    }

    @Override
    public synchronized void refresh() {
        LEDUtil.clear(ledManager);
        final LEDCharacter ch = LEDCharacter.getChar((char)(timer.getSeconds() + 32));
        ch.setThickness(2);
        final Matrix4f matrix = new Matrix4f();
        final Matrix4f matrix2 = new Matrix4f();
        matrix.translate(new Vector3f((ch.getThickness() - 1) / 2F, 0, (ch.getFontSize() - 1) / 2F));
        matrix.rotate((float)Math.PI * 2 * (((float)timer.getSeconds() / 2) % 1), new Vector3f(0, -1, 0));
        matrix.translate(new Vector3f(-(ch.getThickness() - 1) / 2F, 0, -(ch.getFontSize() - 1) / 2F));
        matrix2.translate(new Vector3f(4 - (ch.getThickness() / 2F), 0, 0));
        ch.applyTransform(new LEDCharacter.Transformer() {
            @Override
            public Vector3 transform(Vector3 vector) {
                return Util.transformVector(Util.transformVector(vector, matrix, false), matrix2, true);
            }
        });
        ch.draw(ledManager, new LEDCharacter.Colorizer() {
            @Override
            public ReadableColor getColorAt(Vector3 vector) {
                Color color = new Color();
                color.fromHSB((vector.getY() / (ch.getFontSize() - 1)) * (300F / 360F), 1, 1 - (vector.getX() / ch.getThickness()));
                return color;
            }
        });
    }

    @Override
    public synchronized void reset() {
        LEDUtil.clear(ledManager);
        timer.restart();
    }
}
