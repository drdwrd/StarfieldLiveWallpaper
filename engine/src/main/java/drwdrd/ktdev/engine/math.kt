package drwdrd.ktdev.engine

import android.support.v4.math.MathUtils


const val M_PI: Float = 3.141592653589793f
const val M_E: Float = 2.718281828459045f


fun deg2rad(deg: Float) : Float {
    return deg / 180.0f * M_PI
}

fun clamp(value: Float, min: Float, max: Float): Float {
    if (value < min) {
        return min
    } else if (value > max) {
        return max
    }
    return value
}


fun smoothstep(edge0: Float, edge1: Float, x: Float): Float {
    var x = x
    // Scale, bias and saturate x to 0..1 range
    x = clamp((x - edge0) / (edge1 - edge0), 0.0f, 1.0f)
    // Evaluate polynomial
    return x * x * (3 - 2 * x)
}
