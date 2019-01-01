#version 100
precision highp float;

uniform mat4 u_MVP;

attribute vec3 position;

varying vec2 uv;


void main() {
    uv = position.xy;

	gl_Position = u_MVP * vec4(position, 1.0);
}

