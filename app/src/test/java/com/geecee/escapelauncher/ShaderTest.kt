package com.geecee.escapelauncher

import android.graphics.RuntimeShader
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ShaderTest {

    @Test
    fun testShader() {
        val CLOUD_SHADER = """
            uniform float2 resolution;
            uniform float time;
            uniform float isNight;
            layout(color) uniform half4 color1;
            layout(color) uniform half4 color2;
            layout(color) uniform half4 color3;
        
            float hash(vec2 p) {
                return fract(sin(dot(p, vec2(12.9898, 78.233))) * 43758.5453);
            }
        
            float noise(vec2 p) {
                vec2 i = floor(p);
                vec2 f = fract(p);
                f = f * f * (3.0 - 2.0 * f);
                float a = hash(i);
                float b = hash(i + vec2(1.0, 0.0));
                float c = hash(i + vec2(0.0, 1.0));
                float d = hash(i + vec2(1.0, 1.0));
                return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
            }
        
            float fbm(vec2 p) {
                float v = 0.0;
                float a = 0.5;
                for (int i = 0; i < 4; i++) {
                    v += a * noise(p);
                    p *= 2.0;
                    a *= 0.5;
                }
                return v;
            }
        
            half4 main(in float2 fragCoord) {
                vec2 uv = (fragCoord - resolution.xy * 0.5) / (min(resolution.x, resolution.y) * 0.5);
                
                float d = length(uv);
                float angle = atan(uv.y, uv.x);
                
                float n = fbm(uv * 1.5 + time * 0.05);
                float wobble = sin(angle * 2.0 + time * 0.1) * 0.05 + cos(angle * 3.0 - time * 0.07) * 0.03 + n * 0.1;
                d += wobble;
                
                float core = exp(-d * 2.5);
                float halo = exp(-d * 1.0) * 0.6;
                
                vec4 c = mix(color3, color2, halo);
                c = mix(c, color1, core);
                
                // Add whitish stars for night mode
                float starNoise = hash(floor(uv * min(resolution.x, resolution.y) * 0.1));
                float stars = step(0.98, starNoise) * isNight * (sin(time * 3.0 + starNoise * 100.0) * 0.5 + 0.5);
                c.rgb += vec3(stars * 0.6);
                
                // Circular atom cloud fade out
                float edgeAlpha = 1.0 - smoothstep(0.4, 1.1, d);
                c.a *= edgeAlpha;
                
                // Premultiply alpha
                c.rgb *= c.a;
                
                return half4(c);
            }
        """
        RuntimeShader(CLOUD_SHADER)
    }
}