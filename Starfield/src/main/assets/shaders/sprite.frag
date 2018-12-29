#version 100
precision highp float;


uniform sampler2D u_Layer0;
uniform sampler2D u_Noise;
uniform vec4 u_uvRoI;
uniform float u_FadeIn;
uniform float u_FadeOut;

varying vec2 uv;


void main() {

    vec2 tex = u_uvRoI.xy + u_uvRoI.zw * uv;

    vec4 layer0 = texture2D(u_Layer0, tex);
    vec4 noise = texture2D(u_Noise, uv);

    float d = 2.0 * length(uv - vec2(0.5)) + 0.2 * noise.r;

    float l0 = min(1.0, 0.299 * layer0.r + 0.587 * layer0.g + 0.114 * layer0.b);
    float alpha = u_FadeOut * u_FadeIn * smoothstep(0.015, 0.35, 2.0 * max(1.0 - d * d, 0.0) * pow(noise.r, 2.0));

    vec3 c1 = layer0.rgb;//pow(layer0.rgb, vec3(1.0 / (u_FadeOut + 0.01)));
    float c2 = 0.0;//smoothstep(0.5, 0.7, l0);


    gl_FragColor = alpha * vec4(c1 + vec3(c2), 1.0);
    //gl_FragColor = u_Fade * alpha * layer0;
}
