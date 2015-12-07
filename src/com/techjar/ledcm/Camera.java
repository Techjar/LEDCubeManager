
package com.techjar.ledcm;

import com.techjar.ledcm.util.Angle;
import com.techjar.ledcm.util.MathHelper;
import com.techjar.ledcm.util.Util;
import com.techjar.ledcm.util.Vector2;
import com.techjar.ledcm.util.Vector3;
import com.techjar.ledcm.util.input.InputBinding;
import com.techjar.ledcm.util.input.InputBindingManager;
import com.techjar.ledcm.util.input.InputInfo;
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
    private boolean pForward, pBack, pLeft, pRight, pDown, pUp, pTurbo;

    public Camera() {
        this.moveSpeed = 20;
        this.rotateMultiplier = 0.2F;
        this.position = new Vector3();
        this.angle = new Angle(Angle.Order.YXZ);

        InputBindingManager.addBinding(new InputBinding("camforward", "Forward", "Camera", true, new InputInfo(InputInfo.Type.KEYBOARD, Keyboard.KEY_W)) {
            @Override
            public boolean onPressed() {
                pForward = true;
                return false;
            }

            @Override
            public boolean onReleased() {
                pForward = false;
                return false;
            }
        });
        InputBindingManager.addBinding(new InputBinding("camback", "Back", "Camera", true, new InputInfo(InputInfo.Type.KEYBOARD, Keyboard.KEY_S)) {
            @Override
            public boolean onPressed() {
                pBack = true;
                return false;
            }

            @Override
            public boolean onReleased() {
                pBack = false;
                return false;
            }
        });
        InputBindingManager.addBinding(new InputBinding("camleft", "Left", "Camera", true, new InputInfo(InputInfo.Type.KEYBOARD, Keyboard.KEY_A)) {
            @Override
            public boolean onPressed() {
                pLeft = true;
                return false;
            }

            @Override
            public boolean onReleased() {
                pLeft = false;
                return false;
            }
        });
        InputBindingManager.addBinding(new InputBinding("camright", "Right", "Camera", true, new InputInfo(InputInfo.Type.KEYBOARD, Keyboard.KEY_D)) {
            @Override
            public boolean onPressed() {
                pRight = true;
                return false;
            }

            @Override
            public boolean onReleased() {
                pRight = false;
                return false;
            }
        });
        InputBindingManager.addBinding(new InputBinding("camdown", "Down", "Camera", true, new InputInfo(InputInfo.Type.KEYBOARD, Keyboard.KEY_Q)) {
            @Override
            public boolean onPressed() {
                pDown = true;
                return false;
            }

            @Override
            public boolean onReleased() {
                pDown = false;
                return false;
            }
        });
        InputBindingManager.addBinding(new InputBinding("camup", "Up", "Camera", true, new InputInfo(InputInfo.Type.KEYBOARD, Keyboard.KEY_E)) {
            @Override
            public boolean onPressed() {
                pUp = true;
                return false;
            }

            @Override
            public boolean onReleased() {
                pUp = false;
                return false;
            }
        });
        InputBindingManager.addBinding(new InputBinding("camturbo", "Turbo", "Camera", true, new InputInfo(InputInfo.Type.KEYBOARD, Keyboard.KEY_LSHIFT)) {
            @Override
            public boolean onPressed() {
                pTurbo = true;
                return false;
            }

            @Override
            public boolean onReleased() {
                pTurbo = false;
                return false;
            }
        });
    }

    public void update(float delta) {
        //position = position.add(velocity);
        //angle = angle.add(angularVelocity);
        float moveMult = 1;
        if (pTurbo) {
            moveMult = 5;
        }
        if (pForward) {
            position = position.add(angle.forward().multiply(moveSpeed * moveMult * delta));
        }
        if (pBack) {
            position = position.subtract(angle.forward().multiply(moveSpeed * moveMult * delta));
        }
        if (pRight) {
            position = position.add(angle.right().multiply(moveSpeed * moveMult * delta));
        }
        if (pLeft) {
            position = position.subtract(angle.right().multiply(moveSpeed * moveMult * delta));
        }
        if (pDown) {
            position = position.subtract(angle.up().multiply(moveSpeed * moveMult * delta));
        }
        if (pUp) {
            position = position.add(angle.up().multiply(moveSpeed * moveMult * delta));
        }
        if (Mouse.isGrabbed()) {
            Vector2 offset = Util.getMouseCenterOffset();
            angle = angle.add(new Angle(-offset.getY() * rotateMultiplier, -offset.getX() * rotateMultiplier));
            angle.setPitch(MathHelper.clamp(angle.getPitch(), -90, 90));
            Mouse.setCursorPosition(LEDCubeManager.getWidth() / 2, LEDCubeManager.getHeight() / 2);
        }
    }

    public void setAngle(Angle angle) {
        angle.setOrder(Angle.Order.YXZ);
        this.angle = angle;
    }
}
