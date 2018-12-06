#version 100
precision highp float;

uniform mat4 u_ModelViewProjectionMatrix;

attribute vec3 position;

varying vec2 uv;


void main() {
    uv = position.xy;

	gl_Position = u_ModelViewProjectionMatrix * vec4(position, 1.0);
}

