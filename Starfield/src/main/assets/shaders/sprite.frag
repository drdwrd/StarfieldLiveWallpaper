#version 100
precision highp float;


uniform sampler2D u_Layer0;
uniform vec2 u_uvPos;
uniform float u_uvScale;
uniform float u_Gamma;

varying vec2 uv;


void main() {

    vec2 tex = u_uvPos + 0.2 * u_uvScale * uv;

    vec4 layer0 = texture2D(u_Layer0, tex);

    float l0 = min(1.0, 0.299 * layer0.r + 0.587 * layer0.g + 0.114 * layer0.b);
    float alpha = max(1.0 - dot(uv, uv), 0.0);

    gl_FragColor = vec4(pow(layer0.rgb, vec3(u_Gamma * (1.0 + 0.1 / (1.01 - l0)))) * alpha, 1.0);
}
