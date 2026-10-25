#version 150

uniform sampler2D Sampler0;

out vec4 fragColor;

void main() {
    vec2 screenUv = gl_FragCoord.xy / vec2(textureSize(Sampler0, 0));
    fragColor = texture(Sampler0, screenUv);
}
