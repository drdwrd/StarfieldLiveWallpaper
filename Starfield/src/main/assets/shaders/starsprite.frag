#version 100
precision highp float;


uniform sampler2D u_StarSprites;
uniform sampler2D u_Noise;
uniform vec4 u_uvRoI;
uniform float u_FadeIn;
uniform mat3 u_RotationMatrix;

varying vec2 uv;

const vec3 lumaCoeff = vec3(0.2126, 0.7152, 0.0722);

void main() {

    vec2 tex = u_uvRoI.xy + u_uvRoI.zw * uv;

    vec4 star = texture2D(u_StarSprites, tex);

    vec3 p = u_RotationMatrix * vec3(uv, 1.0);

    vec4 noise = texture2D(u_Noise, p.xy);

    float luma = min(1.0, dot(lumaCoeff, star.rgb));

    float d = 1.0 - 0.5 * smoothstep(0.25, 0.6, luma + (noise.r - 0.5));

    vec3 color = pow(star.rgb, vec3(d));

    gl_FragColor = u_FadeIn * vec4(color, star.a);
}
