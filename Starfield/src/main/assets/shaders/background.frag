#version 100
precision highp float;

uniform sampler2D u_Layer1;
//uniform sampler2D u_Noise;
uniform float u_Time;

varying vec2 uv;


void main() {

    vec2 tex = 0.5 + 0.5 * uv;


    vec4 layer1 = texture2D(u_Layer1, tex);
//    vec4 noise = texture2D(u_Noise, tex + vec2(0.1 * u_Time, 0.0));

    float l1 = min(1.0, 0.299 * layer1.r + 0.587 * layer1.g + 0.114 * layer1.b);

//    float d = 2.0 + 2.0 * (noise.r - 0.5) * smoothstep(0.4, 0.8, l1);
    float d = 1.0 + 0.75 * sin(60.0 * dot(uv, uv) - 2.0 * u_Time) * smoothstep(0.25, 0.75, l1);


    gl_FragColor = pow(layer1, vec4(d));

}

