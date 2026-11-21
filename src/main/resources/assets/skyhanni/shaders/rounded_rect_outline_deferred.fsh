#version 150

#moj_import <skyhanni:shape_data.glsl>

in vec4 vertexColor;
flat in int shapeIndex;

out vec4 fragColor;

float roundedRectSDF(vec2 center, vec2 halfSize, float radius) {
    return length(max(abs(center) - halfSize + radius, 0.0)) - radius;
}

void main() {
    vec4 roundedParams0 = ShapeData[shapeIndex];
    vec4 roundedParams1 = ShapeData[shapeIndex + 1];
    float radius = roundedParams0.x;
    float borderThickness = roundedParams0.y;
    vec2 halfSize = roundedParams0.zw;
    vec2 centerPos = roundedParams1.xy;
    float borderBlur = roundedParams1.z;

    float distance = roundedRectSDF(gl_FragCoord.xy - centerPos, halfSize, max(radius, borderThickness));
    float smoothed = 1.0 - smoothstep(borderBlur, 1.0, abs(distance / borderThickness));
    fragColor = vertexColor * vec4(1.0, 1.0, 1.0, smoothed);
}
