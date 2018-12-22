#version 420 core

layout(binding = 0) uniform sampler2D image;

uniform bool horizontal;
uniform float kernel_size = 5;
uniform float kernel[5] = float[] (0.227027, 0.1945946, 0.1216216, 0.054054, 0.016216);

in VERTEX {
    vec3 position;
    vec3 normal;
    vec2 texcoord;
    vec4 color;
} vertex;

layout(location = 0) out vec4 FragColor;

void main()
{
    vec2 tex_offset = 1.0 / textureSize(image, 0); // gets size of single texel
    vec3 result = texture(image, vertex.texcoord).rgb * kernel[0]; // current fragment's contribution
    if(horizontal)
    {
        for(int i = 1; i < kernel_size; ++i)
        {
            result += texture(image, vertex.texcoord + vec2(tex_offset.x * i, 0.0)).rgb * kernel[i];
            result += texture(image, vertex.texcoord - vec2(tex_offset.x * i, 0.0)).rgb * kernel[i];
        }
    }
    else
    {
        for(int i = 1; i < kernel_size; ++i)
        {
            result += texture(image, vertex.texcoord + vec2(0.0, tex_offset.y * i)).rgb * kernel[i];
            result += texture(image, vertex.texcoord - vec2(0.0, tex_offset.y * i)).rgb * kernel[i];
        }
    }
    FragColor = vec4(result, 1.0);
}