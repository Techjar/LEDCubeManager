
package com.techjar.cubedesigner;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL32.*;
import static org.lwjgl.opengl.GL40.*;
import static org.lwjgl.opengl.GL43.*;

import com.techjar.cubedesigner.util.ShaderProgram;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Techjar
 */
public class ShaderLoader {
    protected final File shaderPath;
    protected final Map<String, Integer> types;

    public ShaderLoader() {
        shaderPath = new File("resources/models/");
        Map<String, Integer> typeMap = new HashMap<>();
        typeMap.put("vsh", GL_VERTEX_SHADER);
        typeMap.put("tcsh", GL_TESS_CONTROL_SHADER);
        typeMap.put("tesh", GL_TESS_EVALUATION_SHADER);
        typeMap.put("gsh", GL_GEOMETRY_SHADER);
        typeMap.put("fsh", GL_FRAGMENT_SHADER);
        typeMap.put("csh", GL_COMPUTE_SHADER);
        types = Collections.unmodifiableMap(typeMap);
    }


}
