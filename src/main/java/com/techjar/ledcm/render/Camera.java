
package com.techjar.ledcm.render;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.Main;
import com.techjar.ledcm.util.math.Angle;
import com.techjar.ledcm.util.MathHelper;
import com.techjar.ledcm.util.Util;
import com.techjar.ledcm.util.math.MutableVector3;
import com.techjar.ledcm.util.math.Vector2;
import com.techjar.ledcm.util.math.Vector3;
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
	private float moveSpeedMult = 1;
	private MutableVector3 velocity;

	public Camera() {
		this.moveSpeed = 0.2F;
		this.rotateMultiplier = 0.2F;
		this.position = new Vector3();
		this.velocity = new MutableVector3();
		this.angle = new Angle(Angle.Order.YXZ);

		if (!Main.isVrMode()) {
			InputBindingManager.addBinding(new InputBinding("camforward", "Forward", "Camera", true, new InputInfo(InputInfo.Type.KEYBOARD, Keyboard.KEY_W)) {
				@Override
				public boolean onPressed() {
					return false;
				}

				@Override
				public void whilePressed() {
					velocity.add(angle.forward());
				}

				@Override
				public boolean onReleased() {
					return false;
				}
			});
			InputBindingManager.addBinding(new InputBinding("camback", "Back", "Camera", true, new InputInfo(InputInfo.Type.KEYBOARD, Keyboard.KEY_S)) {
				@Override
				public boolean onPressed() {
					return false;
				}

				@Override
				public void whilePressed() {
					velocity.subtract(angle.forward());
				}

				@Override
				public boolean onReleased() {
					return false;
				}
			});
			InputBindingManager.addBinding(new InputBinding("camleft", "Left", "Camera", true, new InputInfo(InputInfo.Type.KEYBOARD, Keyboard.KEY_A)) {
				@Override
				public boolean onPressed() {
					return false;
				}

				@Override
				public void whilePressed() {
					velocity.subtract(angle.right());
				}

				@Override
				public boolean onReleased() {
					return false;
				}
			});
			InputBindingManager.addBinding(new InputBinding("camright", "Right", "Camera", true, new InputInfo(InputInfo.Type.KEYBOARD, Keyboard.KEY_D)) {
				@Override
				public boolean onPressed() {
					return false;
				}

				@Override
				public void whilePressed() {
					velocity.add(angle.right());
				}

				@Override
				public boolean onReleased() {
					return false;
				}
			});
			InputBindingManager.addBinding(new InputBinding("camdown", "Down", "Camera", true, new InputInfo(InputInfo.Type.KEYBOARD, Keyboard.KEY_Q)) {
				@Override
				public boolean onPressed() {
					return false;
				}

				@Override
				public void whilePressed() {
					velocity.subtract(angle.up());
				}

				@Override
				public boolean onReleased() {
					return false;
				}
			});
			InputBindingManager.addBinding(new InputBinding("camup", "Up", "Camera", true, new InputInfo(InputInfo.Type.KEYBOARD, Keyboard.KEY_E)) {
				@Override
				public boolean onPressed() {
					return false;
				}

				@Override
				public void whilePressed() {
					velocity.add(angle.up());
				}

				@Override
				public boolean onReleased() {
					return false;
				}
			});
			InputBindingManager.addBinding(new InputBinding("camturbo", "Turbo", "Camera", true, new InputInfo(InputInfo.Type.KEYBOARD, Keyboard.KEY_LSHIFT)) {
				@Override
				public boolean onPressed() {
					moveSpeedMult = 5;
					return false;
				}

				@Override
				public void whilePressed() {
				}

				@Override
				public boolean onReleased() {
					moveSpeedMult = 1;
					return false;
				}
			});
		}
	}

	public void update(float delta) {
		if (Mouse.isGrabbed()) {
			Vector2 offset = Util.getMouseCenterOffset();
			angle = angle.add(new Angle(-offset.getY() * rotateMultiplier, -offset.getX() * rotateMultiplier));
			angle.setPitch(MathHelper.clamp(angle.getPitch(), -90, 90));
			Mouse.setCursorPosition(LEDCubeManager.getWidth() / 2, LEDCubeManager.getHeight() / 2);
		}
		if (velocity.length() > 0) {
			velocity.normalized();
			velocity.multiply(moveSpeed * moveSpeedMult * LEDCubeManager.getFrameDelta());
			position = position.add(velocity);
			velocity.set(0, 0, 0);
		}
	}

	public void setAngle(Angle angle) {
		angle.setOrder(Angle.Order.YXZ);
		this.angle = angle;
	}
}
