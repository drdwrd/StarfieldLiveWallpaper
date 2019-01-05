#version 100
precision highp float;

uniform vec2 u_Aspect;
uniform mat3 u_TextureMatrix;
uniform vec2 u_Offset;

attribute vec3 position;
attribute vec2 uvCoord;

varying vec2 uv;
varying vec2 center;

const vec2 impactCenter = vec2(0.0, 0.0);


void main() {

    vec3 tex = u_TextureMatrix * vec3(0.5 * u_Aspect * position.xy + u_Offset, 1.0);
    uv =  tex.xy;

    vec3 c = u_TextureMatrix * vec3(0.5 * u_Aspect * impactCenter, 1.0);
    center = c.xy;

	gl_Position = vec4(position.xy, 0.0, 1.0);
}

