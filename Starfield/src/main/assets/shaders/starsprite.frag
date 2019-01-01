#version 100
precision highp float;


uniform sampler2D u_StarSprites;
uniform vec4 u_uvRoI;
uniform float u_FadeIn;

varying vec2 uv;

void main() {

    vec2 tex = u_uvRoI.xy + u_uvRoI.zw * uv;

    vec4 star = texture2D(u_StarSprites, tex);

    gl_FragColor = u_FadeIn * star;
}
