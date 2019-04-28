#version 100
precision highp float;


uniform sampler2D u_CloudSprites;
//uniform sampler2D u_Noise;
uniform vec4 u_uvRoI;
uniform float u_Fade;
//uniform mat3 u_RotationMatrix;
uniform vec4 u_Color;
uniform float u_AlphaThreshold;

varying vec2 uv;


void main() {

    vec2 tex = u_uvRoI.xy + u_uvRoI.zw * uv;

    vec4 cloud = texture2D(u_CloudSprites, tex);

//    vec3 p = u_RotationMatrix * vec3(uv, 1.0);

//    vec4 noise = texture2D(u_Noise, p.xy);

    vec3 c = u_Color.rgb * cloud.rgb;
//    float a = u_Color.a * smoothstep(0.0, u_AlphaThreshold + noise.r - 0.5, cloud.a);
    float a = u_Color.a * smoothstep(0.0, u_AlphaThreshold, cloud.a);

    gl_FragColor = u_Fade * vec4(c * a, a);
}
