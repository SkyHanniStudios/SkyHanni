#version 150

in vec3 Position;

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

flat out int roundedUniformIndex;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position.xy, 0.0, 1.0);
    roundedUniformIndex = int(Position.z);
}
