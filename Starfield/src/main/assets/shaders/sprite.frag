#version 100
precision highp float;


uniform sampler2D u_Layer0;
uniform sampler2D u_Noise;
uniform vec4 u_uvRoI;
uniform float u_Fade;

varying vec2 uv;


void main() {

    vec2 tex = u_uvRoI.xy + u_uvRoI.zw * uv;

    vec4 layer0 = texture2D(u_Layer0, tex);
    vec4 noise = texture2D(u_Noise, tex);

    float l0 = min(1.0, 0.299 * layer0.r + 0.587 * layer0.g + 0.114 * layer0.b);
    float alpha = 2.0 * u_Fade * max(1.0 - dot(uv, uv), 0.0) * smoothstep(0.015, 0.35, pow(noise.r, 2.0));

    vec3 c1 = pow(layer0.rgb, vec3(1.0 * (1.0 + 0.1 / (1.01 - l0))));
    vec3 c2 = pow(layer0.rgb, vec3(5.0 * (1.0 + 0.1 / (1.01 - l0))));


    gl_FragColor = vec4( (c1 + c2) * alpha, 1.0);
}
