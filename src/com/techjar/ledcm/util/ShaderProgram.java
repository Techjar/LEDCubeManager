
package com.techjar.ledcm.util;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL32.*;
import static org.lwjgl.opengl.GL40.*;
import static org.lwjgl.opengl.GL41.*;
import static org.lwjgl.opengl.GL43.*;

import com.techjar.ledcm.util.logging.LogHelper;
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

/**
 *
 * @author Techjar
 */
public class ShaderProgram {
    private static final File shaderPath = new File("resources/shaders/");
    private static final Map<String, Integer> types;
    private static final Map<String, Integer> typeBitLookup;
    private static Map<String, List<Integer>> shaderCache = new HashMap<>();
    private static List<ShaderProgram> programCache = new ArrayList<>();
    private static ShaderProgram activeProgram;
    @Getter private int id;
    private List<Integer> shaderIds;
    private Map<String, Integer> attribs;
    private Map<String, Integer> uniforms;
    @Getter private boolean linked;

    static {
        Map<String, Integer> typeMap = new HashMap<>();
        typeMap.put("vsh", GL_VERTEX_SHADER);
        typeMap.put("tcsh", GL_TESS_CONTROL_SHADER);
        typeMap.put("tesh", GL_TESS_EVALUATION_SHADER);
        typeMap.put("gsh", GL_GEOMETRY_SHADER);
        typeMap.put("fsh", GL_FRAGMENT_SHADER);
        typeMap.put("csh", GL_COMPUTE_SHADER);
        types = Collections.unmodifiableMap(typeMap);
        Map<String, Integer> typeBitMap = new HashMap<>();
        typeBitMap.put("vsh", GL_VERTEX_SHADER_BIT);
        typeBitMap.put("tcsh", GL_TESS_CONTROL_SHADER_BIT);
        typeBitMap.put("tesh", GL_TESS_EVALUATION_SHADER_BIT);
        typeBitMap.put("gsh", GL_GEOMETRY_SHADER_BIT);
        typeBitMap.put("fsh", GL_FRAGMENT_SHADER_BIT);
        typeBitMap.put("csh", GL_COMPUTE_SHADER_BIT);
        typeBitLookup = Collections.unmodifiableMap(typeBitMap);
    }

    public ShaderProgram() {
        this.shaderIds = new ArrayList<>();
        this.attribs = new HashMap<>();
        this.uniforms = new HashMap<>();
        id = glCreateProgram();
        programCache.add(this);
        //glProgramParameteri(id, GL_PROGRAM_SEPARABLE, GL_TRUE);
    }

    public int getAttribLocation(String name) {
        if (attribs.containsKey(name)) return attribs.get(name);
        int value = glGetAttribLocation(id, name);
        attribs.put(name, value);
        return value;
    }

    public int getUniformLocation(String name) {
        if (uniforms.containsKey(name)) return uniforms.get(name);
        int value = glGetUniformLocation(id, name);
        uniforms.put(name, value);
        return value;
    }

    @SneakyThrows(IOException.class)
    public ShaderProgram loadShader(String name, int typeBits) {
        checkLinked(true);
        if (shaderCache.containsKey(name)) {
            for (int shaderId : shaderCache.get(name)) {
                shaderIds.add(shaderId);
            }
            return this;
        }
        boolean found = false;
        for (Map.Entry<String, Integer> entry : types.entrySet()) {
            String fileName = name + '.' + entry.getKey();
            File file = new File(shaderPath, fileName);
            if (file.exists()) {
                found = true;
                if ((typeBitLookup.get(entry.getKey()) & typeBits) == 0) continue;
                String source = Util.readFile(file);
                int shaderId = glCreateShader(entry.getValue());
                glShaderSource(shaderId, source);
                glCompileShader(shaderId);
                if (glGetShaderi(shaderId, GL_COMPILE_STATUS) != GL_TRUE) {
                    int length = glGetShaderi(shaderId, GL_INFO_LOG_LENGTH);
                    String log = glGetShaderInfoLog(shaderId, length);
                    throw new RuntimeException("Shader compile error in " + fileName + ": " + log);
                }
                shaderIds.add(shaderId);
                if (!shaderCache.containsKey(name)) shaderCache.put(name, new ArrayList<Integer>());
                shaderCache.get(name).add(id);
                LogHelper.fine("Loaded shader: %s", fileName);
            }
        }
        if (!found) throw new FileNotFoundException("Shader \"" + name + "\" does not exist");
        return this;
    }

    public ShaderProgram loadShader(String name) {
        return loadShader(name, GL_ALL_SHADER_BITS);
    }

    public ShaderProgram link() {
        checkLinked(true);
        for (int i : shaderIds) {
            glAttachShader(id, i);
        }
        glLinkProgram(id);
        glValidateProgram(id);
        if (glGetProgrami(id, GL_LINK_STATUS) != GL_TRUE) {
            int length = glGetProgrami(id, GL_INFO_LOG_LENGTH);
            String log = glGetProgramInfoLog(id, length);
            throw new RuntimeException("Program linking error: " + log);
        }
        for (int i : shaderIds) {
            glDetachShader(id, i);
        }
        shaderIds = null;
        linked = true;
        return this;
    }

    public void use() {
        checkLinked(false);
        glUseProgram(id);
        activeProgram = this;
    }

    public void release() {
        if (activeProgram == this) useNone();
        glDeleteProgram(id);
    }

    public static void useNone() {
        glUseProgram(0);
        activeProgram = null;
    }

    public static ShaderProgram getCurrent() {
        return activeProgram;
    }

    public static void cleanup() {
        for (ShaderProgram program : programCache) {
            program.release();
        }
        for (List<Integer> list : shaderCache.values()) {
            for (int shaderId : list) {
                glDeleteShader(shaderId);
            }
        }
        shaderCache.clear();
        programCache.clear();
    }

    private void checkLinked(boolean errorCondition) {
        if (linked == errorCondition) throw new IllegalStateException(errorCondition ? "Shader program already linked" : "Shader program net yet linked");
    }
}
