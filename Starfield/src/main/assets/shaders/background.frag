#version 100
precision highp float;

uniform sampler2D u_Layer1;

varying vec2 uv;


void main() {

    vec2 tex = 0.5 + 0.5 * uv;

    vec4 layer1 = texture2D(u_Layer1, tex);

    gl_FragColor = layer1;

}

