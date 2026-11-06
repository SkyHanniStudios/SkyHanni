#version 150

in vec2 texCoord;
flat in int roundedUniformIndex;
layout(std140) uniform SkyHanniRoundedUniforms {
    vec4 roundedParams[640];
};

uniform sampler2D Sampler0;

out vec4 outColor;

float roundedRectSDF(vec2 center, vec2 halfSize, float radius) {
    return length(max(abs(center) - halfSize + radius, 0.0)) - radius;
}

void main() {
    vec4 roundedParams0 = roundedParams[roundedUniformIndex * 5];
    vec4 roundedParams1 = roundedParams[roundedUniformIndex * 5 + 1];
    float radius = roundedParams0.x;
    float smoothness = roundedParams0.y;
    vec2 halfSize = roundedParams0.zw;
    vec2 centerPos = roundedParams1.xy;
    float alpha = roundedParams1.z;

    float distance = roundedRectSDF(gl_FragCoord.xy - centerPos, halfSize, radius);
    float smoothed = 1.0 - smoothstep(0.0, smoothness, distance);
    outColor = texture(Sampler0, texCoord) * vec4(1.0, 1.0, 1.0, smoothed * alpha);
}
