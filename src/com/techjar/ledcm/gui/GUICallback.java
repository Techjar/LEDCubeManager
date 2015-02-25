package com.techjar.ledcm.gui;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Techjar
 */
public abstract class GUICallback implements Runnable {
    @Getter @Setter private GUI component;
    @Getter private Object[] args;
    
    public GUICallback() {
        this.args = new Object[0];
    }
    
    public GUICallback(Object... args) {
        this.args = args;
    }
}
