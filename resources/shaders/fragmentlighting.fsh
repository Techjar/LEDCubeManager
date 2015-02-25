#version 430 core

struct Light {
    vec4 ambient;
    vec4 diffuse;
    vec4 specular;
    vec4 position;
    vec3 spotDirection;
    float spotExponent;
    float spotCutoff;
    float constantAttenuation;
    float linearAttenuation;
    float quadraticAttenuation;
};
struct Material {
    vec4 ambient;
    vec4 diffuse;
    vec4 specular;
    float shininess;
};

layout(binding=0) uniform sampler2D model_texture;
layout(location=0) uniform int num_lights;
layout(location=1) uniform Light lights[10];

// The view matrix and the projection matrix are constant
// across a draw
uniform mat4 view_matrix;
uniform mat4 projection_matrix;

in VERTEX {
    vec3    position;
    vec3    normal;
    vec2    texcoord;
    vec4    color;
} vertex;

out vec4 out_Color;

vec4 scene_ambient = vec4(0.2, 0.2, 0.2, 1.0);
Material front_material = Material(
    vec4(0.2, 0.2, 0.2, 1.0),
    vec4(0.8, 0.8, 0.8, 1.0),
    vec4(1.0, 1.0, 1.0, 1.0),
    20.0
);

void main(void) {
    vec4 color = texture2D(model_texture, vertex.texcoord) * vertex.color;

    mat4 view_inverse = inverse(view_matrix);
    vec3 normalDirection = vertex.normal;
    vec3 viewDirection = normalize(vec3(view_inverse * vec4(0, 0, 0, 1) - vec4(vertex.position, 0)));
    vec3 lightDirection;
    float attenuation;
    vec4 totalDiffuse;
    vec4 totalSpecular;

    for (int i = 0; i < num_lights; i++) {wut
        Light light = lights[i];
        if (light.position.w == 0) {thefuckisthis
            attenuation = 1;
            lightDirection = normalize(vec3(light.position));
        } else {
            vec3 positionToLight = light.position.xyz - vertex.position;
            float distance = length(positionToLight);
            lightDirection = normalize(positionToLight);
            float linAtt = 0;
            float quadAtt = 0;
            if (light.linearAttenuation != 0) {
                quadAtt = light.linearAttenuation * distance * distance;
            }
            if (light.quadraticAttenuation != 0) {
                quadAtt = light.quadraticAttenuation * distance * distance;
            }
            float totalAtt = light.constantAttenuation + linAtt + quadAtt;
            if (totalAtt != 1) {
                attenuation = 1.0 / totalAtt;
            } else {
                attenuation = 1;
            }
            if (light.spotCutoff <= 90) {
                float clampedCosine = max(0, dot(-lightDirection, light.spotDirection));
                if (clampedCosine < cos(radians(light.spotCutoff))) {
                    attenuation = 0;
                } else {
                    attenuation = attenuation * pow(clampedCosine, light.spotExponent);
                }
            }
        }
        float normalLightDot = dot(normalDirection, lightDirection);
        vec4 diffuse = attenuation * light.diffuse * front_material.diffuse * max(0, normalLightDot);
        vec4 specular;
        if (normalLightDot < 0) {
            specular = vec4(0, 0, 0, 0);
        } else {
            specular = attenuation * light.specular * front_material.specular * pow(max(0, dot(reflect(-lightDirection, normalDirection), viewDirection)), front_material.shininess);
        }
        totalDiffuse += diffuse;
        totalSpecular += specular;
    }

    vec4 ambient = scene_ambient * front_material.ambient;
    out_Color = color * (ambient + totalDiffuse) + totalSpecular;
}
