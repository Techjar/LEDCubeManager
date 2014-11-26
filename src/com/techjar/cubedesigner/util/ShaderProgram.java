
package com.techjar.cubedesigner.util;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL32.*;
import static org.lwjgl.opengl.GL40.*;
import static org.lwjgl.opengl.GL41.*;
import static org.lwjgl.opengl.GL43.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.SneakyThrows;
import org.lwjgl.opengl.GL20;

/**
 *
 * @author Techjar
 */
public class ShaderProgram {
    private static final File shaderPath = new File("resources/models/");
    private static final Map<String, Integer> types;
    @Getter private int programId;
    private List<Integer> shaderIds;
    private boolean linked;

    static {
        Map<String, Integer> typeMap = new HashMap<>();
        typeMap.put("vsh", GL_VERTEX_SHADER);
        typeMap.put("tcsh", GL_TESS_CONTROL_SHADER);
        typeMap.put("tesh", GL_TESS_EVALUATION_SHADER);
        typeMap.put("gsh", GL_GEOMETRY_SHADER);
        typeMap.put("fsh", GL_FRAGMENT_SHADER);
        typeMap.put("csh", GL_COMPUTE_SHADER);
        types = Collections.unmodifiableMap(typeMap);
    }

    public ShaderProgram() {
        this.shaderIds = new ArrayList<>();
        programId = glCreateProgram();
        glProgramParameteri(programId, GL_PROGRAM_SEPARABLE, GL_TRUE);
    }

    @SneakyThrows(IOException.class)
    public ShaderProgram loadShader(String name) {
        checkLinked(true);
        boolean found = false;
        for (Map.Entry<String, Integer> entry : types.entrySet()) {
            File file = new File(shaderPath, name + '.' + entry.getKey());
            if (file.exists()) {
                found = true;
                String source = Util.readFile(file);
                int id = glCreateShader(entry.getValue());
                glShaderSource(id, source);
                glCompileShader(id);
                if (glGetShaderi(id, GL_COMPILE_STATUS) != GL_TRUE) {
                    int length = glGetShaderi(id, GL_INFO_LOG_LENGTH);
                    String log = glGetShaderInfoLog(id, length);
                    throw new RuntimeException("Shader compile error: " + log);
                }
                shaderIds.add(id);
            }
        }
        if (!found) throw new FileNotFoundException("Shader \"" + name + "\" does not exist");
        return this;
    }

    public ShaderProgram link() {
        checkLinked(true);
        for (int i : shaderIds) {
            glAttachShader(programId, i);
        }
        glLinkProgram(programId);
        if (glGetProgrami(programId, GL_LINK_STATUS) != GL_TRUE) {
            int length = glGetProgrami(programId, GL_INFO_LOG_LENGTH);
            String log = glGetShaderInfoLog(programId, length);
            throw new RuntimeException("Program linking error: " + log);
        }
        for (int i : shaderIds) {
            glDetachShader(programId, i);
            glDeleteShader(i);
        }
        shaderIds = null;
        linked = true;
        return this;
    }

    public void use() {
        checkLinked(false);
        glUseProgram(programId);
    }

    public static void useNone() {
        glUseProgram(0);
    }

    private void checkLinked(boolean errorCondition) {
        if (linked == errorCondition) throw new IllegalStateException(errorCondition ? "Shader program already linked" : "Shader program net yet linked");
    }
}
