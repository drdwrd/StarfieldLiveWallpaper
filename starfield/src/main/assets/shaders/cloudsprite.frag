#version 100
precision highp float;


uniform sampler2D u_CloudSprites;
uniform vec4 u_uvRoI;
uniform float u_Fade;
uniform vec4 u_Color;

varying vec2 uv;


void main() {

    vec2 tex = u_uvRoI.xy + u_uvRoI.zw * uv;

    vec4 cloud = texture2D(u_CloudSprites, tex);

    gl_FragColor = u_Fade * u_Color * cloud;
}
