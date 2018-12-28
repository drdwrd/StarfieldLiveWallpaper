#version 100
precision highp float;

uniform vec2 u_Aspect;

attribute vec3 position;
attribute vec2 uvCoord;

varying vec2 uv;


void main() {

    uv = u_Aspect * position.xy;
	gl_Position = vec4(position.xy, 0.0, 1.0);
}

