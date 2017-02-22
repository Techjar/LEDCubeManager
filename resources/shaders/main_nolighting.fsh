#version 430 core

struct Material {
    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
    float shininess;
};

// Texture samplers
layout(binding = 0) uniform sampler2D model_texture;
//layout(binding = 1) uniform sampler2D model_normalmap;

// The view matrix and the projection matrix are constant across a draw
uniform mat4 view_matrix;
uniform mat4 projection_matrix;

// Material
uniform Material front_material;

in VERTEX {
    vec3    position;
    vec3    normal;
    vec2    texcoord;
    vec4    color;
} vertex;

out vec4 out_Color;

void main(void) {
    vec4 color = texture2D(model_texture, vertex.texcoord) * vertex.color;
    if (color.a < 0.002) discard;
    out_Color = color;
}
