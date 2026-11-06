#version 150

in vec3 Position;
in vec2 UV0;

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

out vec2 texCoord;
flat out int shapeIndex;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position.xy, 0.0, 1.0);
    texCoord = UV0;
    shapeIndex = int(Position.z);
}
