#version 100
precision highp float;

uniform sampler2D u_Starfield;
uniform float u_Time;

varying vec2 uv;
varying vec2 center;

const vec3 lumaCoeff = vec3(0.2126, 0.7152, 0.0722);

void main() {

    vec2 tex = 0.5 + 0.5 * uv;
    vec2 c = 0.5 + 0.5 * center;

    vec4 starfield = texture2D(u_Starfield, tex);

    float luma = min(1.0, dot(lumaCoeff, starfield.rgb));

    float d = 1.0 + 0.5 * sin(60.0 * dot(tex - c, tex - c) - 0.4 * u_Time) * smoothstep(0.25, 0.6, luma);

    gl_FragColor = pow(starfield, vec4(d));
}

