#version 100
precision highp float;

uniform sampler2D u_Starfield;
uniform float u_Time;

varying vec2 uv;
varying vec2 center;


void main() {

    vec2 tex = 0.5 + 0.5 * uv;
    vec2 c = 0.5 + 0.5 * center;

    vec4 starfield = texture2D(u_Starfield, tex);

    float luma = max(max(starfield.r, starfield.g), starfield.b);

    float d = 1.0 + 0.45 * sin(60.0 * tex.y - 0.75 * u_Time) * smoothstep(0.25, 0.8, luma);

    gl_FragColor = pow(starfield, vec4(d));
}

