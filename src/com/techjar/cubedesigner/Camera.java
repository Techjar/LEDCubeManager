
package com.techjar.cubedesigner;

import com.techjar.cubedesigner.util.Angle;
import com.techjar.cubedesigner.util.MathHelper;
import com.techjar.cubedesigner.util.Util;
import com.techjar.cubedesigner.util.Vector2;
import com.techjar.cubedesigner.util.Vector3;
import lombok.Getter;
import lombok.Setter;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

/**
 *
 * @author Techjar
 */
public class Camera {
    @Getter @Setter private float moveSpeed;
    @Getter @Setter private float rotateMultiplier;
    @Getter @Setter private Vector3 position;
    @Getter private Angle angle;
    private boolean pW, pS, pA, pD, pShift;

    public Camera() {
        this.moveSpeed = 20;
        this.rotateMultiplier = 0.2F;
        this.position = new Vector3();
        this.angle = new Angle(Angle.Order.YXZ);
    }

    public boolean processKeyboardEvent() {
        boolean pressed = Keyboard.getEventKeyState();
        switch (Keyboard.getEventKey()) {
            case Keyboard.KEY_W:
                pW = pressed;
                break;
            case Keyboard.KEY_S:
                pS = pressed;
                break;
            case Keyboard.KEY_A:
                pA = pressed;
                break;
            case Keyboard.KEY_D:
                pD = pressed;
                break;
            case Keyboard.KEY_LSHIFT:
                pShift = pressed;
                break;
        }
        return true;
    }

    public void update(float delta) {
        //position = position.add(velocity);
        //angle = angle.add(angularVelocity);
        float moveMult = 1;
        if (pShift) {
            moveMult = 5;
        }
        if (pW) {
            position = position.add(angle.forward().multiply(moveSpeed * moveMult * delta));
        }
        if (pS) {
            position = position.subtract(angle.forward().multiply(moveSpeed * moveMult * delta));
        }
        if (pD) {
            position = position.add(angle.right().multiply(moveSpeed * moveMult * delta));
        }
        if (pA) {
            position = position.subtract(angle.right().multiply(moveSpeed * moveMult * delta));
        }
        if (Mouse.isGrabbed()) {
            Vector2 offset = Util.getMouseCenterOffset();
            angle = angle.add(new Angle(-offset.getY() * rotateMultiplier, -offset.getX() * rotateMultiplier));
            angle.setPitch(MathHelper.clamp(angle.getPitch(), -90, 90));
            Mouse.setCursorPosition(CubeDesigner.getWidth() / 2, CubeDesigner.getHeight() / 2);
        }
    }

    public void setAngle(Angle angle) {
        angle.setOrder(Angle.Order.YXZ);
        this.angle = angle;
    }
}
