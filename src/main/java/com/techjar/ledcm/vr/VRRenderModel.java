package com.techjar.ledcm.vr;

import com.sun.jna.Pointer;
import com.techjar.ledcm.util.Model;
import jopenvr.RenderModel_t;
import org.lwjgl.util.vector.Matrix4f;

public class VRRenderModel {
	public final String modelName;
	final Pointer modelNamePtr;
	public final String componentName;
	final Pointer componentNamePtr;
	public final Model model;
	public boolean visible;
	public Matrix4f transform;
	public Matrix4f transformLocal;

	public VRRenderModel(String modelName, String componentName, Model model) {
		this.modelName = modelName;
		this.modelNamePtr = VRProvider.pointerFromString(modelName);
		this.componentName = componentName;
		this.componentNamePtr = VRProvider.pointerFromString(componentName);
		this.model = model;
		this.transform = new Matrix4f();
		this.transformLocal = new Matrix4f();
	}
}
