
package com.techjar.ledcm.util.input;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Techjar
 */
public abstract class InputBinding {
	@Getter private final String id;
	@Getter private final String name;
	@Getter private final String category;
	@Getter private final boolean unbindable;
	@Getter @Setter boolean pressed;
	private InputInfo bind;

	public InputBinding(String id, String name, String category, boolean unbindable, InputInfo defaultBind) {
		this.id = id;
		this.name = name;
		this.category = category;
		this.unbindable = unbindable;
		this.bind = defaultBind;
	}

	public abstract boolean onPressed();
	public abstract void whilePressed();
	public abstract boolean onReleased();

	public final InputInfo getBind() {
		return bind;
	}

	public final void setBind(InputInfo bind) {
		if (!unbindable && bind == null) throw new IllegalArgumentException("Binding is not unbindable");
		this.bind = bind;
	}
}
