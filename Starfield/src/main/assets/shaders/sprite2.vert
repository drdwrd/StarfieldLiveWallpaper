#version 100
precision highp float;

uniform mat4 u_ModelViewProjectionMatrix;
uniform mat4 u_NormalMatrix;

attribute vec3 position;
attribute vec3 normal;
attribute vec2 uvCoord;

varying vec2 uv;
varying vec3 n;


void main() {
    uv = uvCoord;

    n = normalize(mat3(u_NormalMatrix) * normal);

	gl_Position = u_ModelViewProjectionMatrix * vec4(position, 1.0);
}

