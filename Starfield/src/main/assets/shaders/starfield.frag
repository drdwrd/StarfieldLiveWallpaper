#version 100
precision highp float;


uniform sampler2D u_Layer0;
uniform sampler2D u_Layer1;
uniform sampler2D u_Layer2;

uniform vec2 u_Aspect;
uniform float u_Time;
uniform float u_DropTime;
uniform vec2 u_DropCenter;

varying vec2 uv;


void main() {


    vec2 duv = vec2(sin(0.05 * u_Time), cos(0.05 * u_Time));


    vec4 layer0 = texture2D(u_Layer0, uv + duv);
    vec4 layer1 = texture2D(u_Layer1, uv + 2.0 * duv);
    vec4 layer2 = texture2D(u_Layer2, uv + 3.0 * duv);

    float dist = length(uv - u_DropCenter);

    float wave = (5.0 * smoothstep(0.0, 1.0, sin(10.0 * min(dist - u_DropTime, 0.0))) * exp(-2.0 * u_DropTime) + 1.0);

    float ll = 0.299 * layer0.r + 0.587 * layer0.g + 0.114 * layer0.b;

    vec4 color = pow(layer0, 1.0 - 1.75 * vec4(wave * ll)) + layer1 + layer2;

    gl_FragColor = color;
}
