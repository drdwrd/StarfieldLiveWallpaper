#version 100
precision highp float;


uniform sampler2D u_Layer0;
uniform sampler2D u_Layer1;
uniform sampler2D u_Layer2;
uniform sampler2D u_Noise;

uniform vec2 u_dg;
uniform vec2 u_Aspect;
uniform float u_Time;
uniform float u_DropTime;
uniform vec2 u_DropCenter;

varying vec2 uv;


void main() {


    vec2 duv =  u_dg + vec2(sin(0.1 * u_Time), cos(0.1 * u_Time));

    vec4 noise = texture2D(u_Noise, 2.0 * uv - 1.1 * duv);

    float a = 2.0 * noise.r - 1.0;

    vec4 layer0 = (1.0 + 0.75 * a) * texture2D(u_Layer0, uv - 1.0 * duv);
    vec4 layer1 = (1.0 + 0.75 * a) * texture2D(u_Layer1, uv + 1.0 * duv);
    vec4 layer2 = (1.0 + 0.75 * a) * texture2D(u_Layer2, uv + 2.0 * duv);

    float dist = length(uv - u_DropCenter);

    float wave = (5.0 * smoothstep(0.0, 1.0, sin(10.0 * min(dist - u_DropTime, 0.0))) * exp(-2.0 * u_DropTime) + 1.0);

    float l0 = min(1.0, 0.299 * layer0.r + 0.587 * layer0.g + 0.114 * layer0.b);
    float l1 = min(1.0, 0.299 * layer1.r + 0.587 * layer1.g + 0.114 * layer1.b);
//    float l2 = 0.299 * layer2.r + 0.587 * layer2.g + 0.114 * layer2.b;

    vec4 color = pow(layer0, 1.0 - 1.75 * vec4(wave * l0)) + (layer1 + layer2 * pow(1.0 - l1, 12.0)) * pow(1.0 - l0, 15.0);

    gl_FragColor = color;
}
