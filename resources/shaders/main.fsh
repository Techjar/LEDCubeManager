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
    float emissivity;
};

// Texture samplers
layout(binding = 0) uniform sampler2D model_texture;
//layout(binding = 1) uniform sampler2D model_normalmap;
layout(binding = 2) uniform sampler2D model_specularmap;

// The view matrix and the projection matrix are constant across a draw
uniform mat4 view_matrix;
uniform mat4 projection_matrix;

// Material
uniform Material front_material;

// Lighting
uniform vec3 scene_ambient;
uniform int num_lights;
uniform Light lights[10];

in VERTEX {
    vec3 position;
    vec3 normal;
    vec2 texcoord;
    vec4 color;
	vec3 viewDirection;
} vertex;

layout(location = 0) out vec4 out_Color;
layout(location = 1) out vec4 bloom_Color;

void main(void) {
    vec4 color = texture2D(model_texture, vertex.texcoord) * vertex.color;
    if (color.a < 0.002) discard;
    vec3 fragSpecular = texture2D(model_specularmap, vertex.texcoord).rgb * front_material.specular;

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
        float normalLightDot = dot(vertex.normal, lightDirection);
        vec3 diffuse = attenuation * light.diffuse * front_material.diffuse * max(0, normalLightDot);
        vec3 specular;
        if (normalLightDot < 0) {
            specular = vec3(0, 0, 0);
        } else {
            specular = attenuation * light.specular * fragSpecular * pow(max(0, dot(-normalize(reflect(lightDirection, vertex.normal)), vertex.viewDirection)), front_material.shininess);
        }
        totalDiffuse += diffuse;
        totalSpecular += specular;
    }

    float specularLuminance = 0.2126 * totalSpecular.r + 0.7152 * totalSpecular.g + 0.0722 * totalSpecular.b;
    vec3 ambient = scene_ambient * front_material.ambient;
    vec3 linearColor = color.rgb * (ambient + totalDiffuse) + totalSpecular + (color.rgb * clamp(front_material.emissivity, 0, 1));
    out_Color = vec4(linearColor, color.a + specularLuminance);

    float brightness = dot(out_Color.rgb * out_Color.a * front_material.emissivity, vec3(0.2126, 0.7152, 0.0722));
    if (brightness > 1.0)
        bloom_Color = vec4(out_Color.rgb * out_Color.a, 1.0);
    else
        bloom_Color = vec4(0.0, 0.0, 0.0, 1.0);
    
    // Gamma correction (turns out to be useless garbage, as modern displays are already gamma corrected)
    //vec3 gamma = vec3(1.0 / 2.2);
    //out_Color = vec4(pow(linearColor, gamma), color.a + specularLuminance);
}
