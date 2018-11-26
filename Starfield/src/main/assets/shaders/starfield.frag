#version 100
precision highp float;


uniform sampler2D u_Layer0;
uniform sampler2D u_Layer1;
uniform sampler2D u_Layer2;

uniform float u_Time;

varying vec2 uv;


void main() {


    vec2 duv0 = vec2(sin(0.05 * u_Time), cos(0.05 * u_Time));
    vec2 duv1 = vec2(sin(0.1 * u_Time), cos(0.1 * u_Time));
    vec2 duv2 = vec2(sin(0.15 * u_Time), cos(0.15 * u_Time));


    vec4 layer0 = texture2D(u_Layer0, uv + duv0);
    vec4 layer1 = texture2D(u_Layer1, uv + duv1);
    vec4 layer2 = texture2D(u_Layer2, uv + duv2);

    float l1 = exp(5.0 * (0.299 * layer1.r + 0.587 * layer1.g + 0.114 * layer1.b));
    float l2 = exp(5.0 * (0.299 * layer2.r + 0.587 * layer2.g + 0.114 * layer2.b));

    vec4 color = layer2;

    color = 1.0 - (1.0 - l2 * layer1) * (1.0 - layer2);

    color = 1.0 - (1.0 - l1 * layer0) * (1.0 - color);

    gl_FragColor = color;
}
