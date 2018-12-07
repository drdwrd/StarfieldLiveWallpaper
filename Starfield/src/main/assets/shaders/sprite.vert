#version 100
precision highp float;

uniform mat4 u_ModelViewProjectionMatrix;

attribute vec3 position;

varying vec2 uv;


void main() {
    uv = 0.5 * position.xy + 0.5;

	gl_Position = u_ModelViewProjectionMatrix * vec4(position, 1.0);
}

