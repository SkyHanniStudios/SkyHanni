#version 150

in vec3 Position;
in vec4 Color;

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

out vec4 vertexColor;
flat out int roundedUniformIndex;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position.xy, 0.0, 1.0);
    vertexColor = Color;
    roundedUniformIndex = int(Position.z);
}
