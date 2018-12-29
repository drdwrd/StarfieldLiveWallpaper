#version 100
precision highp float;

uniform sampler2D u_Layer1;
uniform float u_Time;

varying vec2 uv;


void main() {

    vec2 tex = 0.5 + 0.5 * uv;


    vec4 layer1 = texture2D(u_Layer1, tex);

    float l1 = min(1.0, 0.299 * layer1.r + 0.587 * layer1.g + 0.114 * layer1.b);

    float d = 1.0 + 0.5 * sin(5.0 * dot(uv, uv) - 5.0 * u_Time) * smoothstep(0.5, 0.8, l1);


    gl_FragColor = pow(layer1, vec4(d));

}

