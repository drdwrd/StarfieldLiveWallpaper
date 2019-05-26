#version 100
precision highp float;

uniform mat4 u_ModelViewProjectionMatrix;
uniform mat3 u_RotationMatrix;

attribute vec3 position;
attribute vec2 uvCoord;

varying vec2 uv;
varying vec2 uv2;

void main() {
    uv = uvCoord;
    vec3 p = u_RotationMatrix * vec3(uvCoord, 1.0);
    uv2 = p.xy;
    gl_Position = u_ModelViewProjectionMatrix * vec4(position, 1.0);
}

