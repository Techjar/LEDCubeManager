#version 420 core

layout(binding = 0) uniform sampler2D scene;
layout(binding = 1) uniform sampler2D bloom;

in VERTEX {
    vec3 position;
    vec3 normal;
    vec2 texcoord;
    vec4 color;
} vertex;

layout(location = 0) out vec4 FragColor;

void main()
{
    vec3 color = texture(scene, vertex.texcoord).rgb;
    vec3 bloomColor = texture(bloom, vertex.texcoord).rgb;
    FragColor = vec4(color + bloomColor, 1);
}