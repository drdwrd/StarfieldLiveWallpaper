#version 100
precision highp float;


uniform sampler2D u_Layer0;

varying vec2 uv;


void main() {

    gl_FragColor = texture2D(u_Layer0, uv);
}
