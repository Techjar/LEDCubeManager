#version 430 core

struct Light {
    vec3 diffuse;
    vec3 specular;
    vec4 position;
    vec3 spotDirection;
    vec2 spotParams; // x = exponent, y = cutoff
    vec3 attenuation; // x = constant, y = linear, z = quadratic
};
struct Material {
    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
    float shininess;
};

layout(binding = 0) uniform sampler2D model_texture;
//layout(binding = 1) uniform sampler2D model_normalmap;
layout(location=0) uniform Material front_material;
layout(location=4) uniform vec3 scene_ambient;
layout(location=5) uniform int num_lights;
layout(location=6) uniform Light lights[10];

// The view matrix and the projection matrix are constant across a draw
uniform mat4 view_matrix;
uniform mat4 projection_matrix;

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

    mat4 view_inverse = inverse(view_matrix);
    vec3 normalDirection = vertex.normal;
    vec3 viewDirection = normalize(vec3(view_inverse * vec4(0, 0, 0, 1) - vec4(vertex.position, 0)));
    vec3 lightDirection = vec3(0, 0, 0);
    float attenuation = 0;
    vec3 totalDiffuse = vec3(0, 0, 0);
    vec3 totalSpecular = vec3(0, 0, 0);

    for (int i = 0; i < num_lights; i++) {
        Light light = lights[i];
        if (light.position.w == 0) {
            attenuation = 1;
            lightDirection = normalize(vec3(light.position));
        } else {
            vec3 positionToLight = light.position.xyz - vertex.position;
            float distance = length(positionToLight);
            lightDirection = normalize(positionToLight);
            float totalAtt = light.attenuation.x;
            if (light.attenuation.y != 0) {
                totalAtt += light.attenuation.y * distance;
            }
            if (light.attenuation.z != 0) {
                totalAtt += light.attenuation.z * distance * distance;
            }
            if (totalAtt != 1) {
                attenuation = 1.0 / totalAtt;
            } else {
                attenuation = 1;
            }
            if (light.spotParams.y <= 90) {
                float clampedCosine = max(0, dot(-lightDirection, light.spotDirection));
                if (clampedCosine < cos(radians(light.spotParams.y))) {
                    attenuation = 0;
                } else {
                    attenuation = attenuation * pow(clampedCosine, light.spotParams.x);
                }
            }
        }
        float normalLightDot = dot(normalDirection, lightDirection);
        vec3 diffuse = attenuation * light.diffuse * front_material.diffuse * max(0, normalLightDot);
        vec3 specular;
        if (normalLightDot < 0) {
            specular = vec3(0, 0, 0);
        } else {
            specular = attenuation * light.specular * front_material.specular * pow(max(0, dot(-normalize(reflect(lightDirection, normalDirection)), viewDirection)), front_material.shininess);
        }
        totalDiffuse += diffuse;
        totalSpecular += specular;
    }

    float specularLuminance = 0.2126 * totalSpecular.r + 0.7152 * totalSpecular.g + 0.0722 * totalSpecular.b;
    vec3 ambient = scene_ambient * front_material.ambient;
    vec3 linearColor = color.rgb * (ambient + totalDiffuse) + totalSpecular;
    out_Color = vec4(linearColor, color.a + specularLuminance);
    
    // Gamma correction (turns out to be useless garbage, as modern displays are already gamma corrected)
    //vec3 gamma = vec3(1.0 / 2.2);
    //out_Color = vec4(pow(linearColor, gamma), color.a + specularLuminance);
}
