package com.techjar.ledcm.gui;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Techjar
 */
@FunctionalInterface
public interface GUICallback<T extends GUI> {
	void run(T component);
}
