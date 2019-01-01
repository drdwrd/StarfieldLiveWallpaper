#version 100
precision highp float;

uniform vec2 u_Aspect;

attribute vec2 position;

varying vec2 uv;


void main() {
    uv = u_Aspect * position;
	gl_Position = vec4(position, 0.0, 1.0);
}

