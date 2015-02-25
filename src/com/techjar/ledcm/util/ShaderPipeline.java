
package com.techjar.ledcm.util;

import static org.lwjgl.opengl.GL41.*;

import lombok.Getter;

/**
 *
 * @author Techjar
 */
public class ShaderPipeline {
    @Getter private int id;

    public ShaderPipeline() {
        this.id = glGenProgramPipelines();
    }

    public ShaderPipeline useProgramStages(ShaderProgram program, int stages) {
        glUseProgramStages(id, stages, program.getId());
        return this;
    }

    public ShaderPipeline removeProgramStages(int stages) {
        glUseProgramStages(id, stages, 0);
        return this;
    }

    public void bind() {
        glBindProgramPipeline(id);
    }

    public static void bindNone() {
        glBindProgramPipeline(0);
    }
}
