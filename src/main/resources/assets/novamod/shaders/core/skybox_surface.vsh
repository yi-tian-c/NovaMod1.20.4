#version 150

in vec3 Position;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

void main() {
    vec4 clipPosition = ProjMat * ModelViewMat * vec4(Position, 1.0);
    gl_Position = clipPosition;
}
