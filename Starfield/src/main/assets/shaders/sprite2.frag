#version 100
precision highp float;


uniform sampler2D u_Layer0;
uniform sampler2D u_Noise;
uniform vec4 u_uvRoI;
uniform float u_FadeIn;
uniform float u_FadeOut;
uniform vec3 u_Dir;

varying vec2 uv;
varying vec3 n;

void main() {

    vec2 tex = u_uvRoI.xy + u_uvRoI.zw * uv;

    vec4 layer0 = texture2D(u_Layer0, tex);
    vec4 noise = texture2D(u_Noise, tex);

    float d = 2.0 * length(uv - vec2(0.5));

    float l0 = min(1.0, 0.299 * layer0.r + 0.587 * layer0.g + 0.114 * layer0.b);
    float dt = abs(dot(n, u_Dir));
    float alpha = u_FadeIn * smoothstep(0.0, 0.5, l0) * dt;

    gl_FragColor = alpha * layer0;
}
