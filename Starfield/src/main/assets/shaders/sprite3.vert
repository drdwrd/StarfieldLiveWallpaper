#version 100
precision highp float;

uniform mat4 u_ModelViewProjectionMatrix;

attribute vec3 position;
attribute vec2 uvCoord;

varying vec2 uv;


void main() {
    uv = uvCoord;

	gl_Position = u_ModelViewProjectionMatrix * vec4(position, 1.0);
}

