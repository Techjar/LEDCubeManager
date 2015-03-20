#version 410 core

// there are our regular vertex attributes
layout(location = 0) in vec3 position;
layout(location = 1) in vec3 normal;
layout(location = 2) in vec2 texcoord;

// this is per instance
layout(location = 3) in vec4 color;

// model_matrix will be used as a per-instance transformation
// matrix. Note that a mat4 consumes 4 consecutive locations, so
// this will actually sit in locations, 4, 5, 6, and 7.
layout(location = 4) in mat4 model_matrix;

// The view matrix and the projection matrix are constant
// across a draw
uniform mat4 view_matrix;
uniform mat4 projection_matrix;

// The output of the vertex shader (matched to the fragment shader)
out VERTEX {
    vec3    position;
    vec3    normal;
    vec2    texcoord;
    vec4    color;
} vertex;

// Ok, go!
void main(void) {
    // Construct a model-view matrix from the uniform view matrix
    // and the per-instance model matrix.
    mat4 model_view_matrix = view_matrix * model_matrix;
    mat3 m_3x3_inv_transp = transpose(inverse(mat3(model_matrix)));

    // Transform position by the model-view matrix, then by the
    // projection matrix.
    gl_Position = projection_matrix * (model_view_matrix * vec4(position, 1));
    vertex.position = vec3(model_matrix * vec4(position, 1));

    // Transform the normal by the upper-left-3x3-submatrix of the
    // model-view matrix
    vertex.normal = normalize(m_3x3_inv_transp * normal);

    // Pass the per-instance texcoord through to the fragment shader.
    vertex.texcoord = texcoord;
    vertex.color = color;
}
