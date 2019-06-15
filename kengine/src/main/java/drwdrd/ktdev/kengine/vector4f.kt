package drwdrd.ktdev.kengine

import android.graphics.Color
import kotlin.math.sqrt


class vector4f(x : Float, y : Float, z : Float, w : Float) {

    constructor() : this(0.0f, 0.0f, 0.0f, 0.0f)
    constructor(v : vector3f, a : Float) : this(v[0], v[1], v[2], a)
    constructor(v : vector4f) : this(v.e[0], v.e[1], v.e[2], v.e[3])
    constructor(v : FloatArray) : this(v[0], v[1], v[2], v[3])
    constructor(v : FloatArray, offset : Int) : this(v[offset], v[offset + 1], v[offset + 2], v[offset + 3])

    //ARGB color
    constructor(color : Int) : this() {
        e[0] = Color.red(color) / 255.0f
        e[1] = Color.green(color) / 255.0f
        e[2] = Color.blue(color) / 255.0f
        e[3] = Color.alpha(color) / 255.0f
    }

    private var e = floatArrayOf(x, y, z, w)

    val x : Float
        get() = e[0]
    val y : Float
        get() = e[1]
    val z : Float
        get() = e[2]
    val w : Float
        get() = e[3]

    fun zero() {
        e[0] = 0.0f
        e[1] = 0.0f
        e[2] = 0.0f
        e[3] = 0.0f
    }

    fun assign(v : vector4f) {
        e[0] = v[0]
        e[1] = v[1]
        e[2] = v[2]
        e[3] = v[3]
    }

    fun fromColorPremultiply(color : Int) {
        e[3] = Color.alpha(color) / 255.0f
        e[0] = e[3] * Color.red(color) / 255.0f
        e[1] = e[3] * Color.green(color) / 255.0f
        e[2] = e[3] * Color.blue(color) / 255.0f
    }

    fun fromColor(color : Int) {
        e[0] = Color.red(color) / 255.0f
        e[1] = Color.green(color) / 255.0f
        e[2] = Color.blue(color) / 255.0f
        e[3] = Color.alpha(color) / 255.0f
    }

    fun fromColor(color : Int, alpha : Float) {
        e[0] = alpha * Color.red(color) / 255.0f
        e[1] = alpha * Color.green(color) / 255.0f
        e[2] = alpha * Color.blue(color) / 255.0f
        e[3] = alpha
    }

    fun toColor()  = Color.argb(Math.round(255.0f * e[3]), Math.round(255.0f * e[0]), Math.round(255.0f * e[1]), Math.round(255.0f * e[2]))

    fun isEqual(v : vector4f) : Boolean {
        return (e[0] == v.e[0]) && (e[1] == v.e[1]) && (e[2] == v.e[2]) && (e[3] == v.e[3])
    }

    operator fun plus(v : vector4f) = vector4f(e[0] + v.e[0], e[1] + v.e[1], e[2] + v.e[2], e[3] + v.e[3])
    operator fun minus(v : vector4f) = vector4f(e[0] - v.e[0], e[1] - v.e[1], e[2] - v.e[2], e[3] - v.e[3])
    operator fun times(v : vector4f) = vector4f(e[0] * v.e[0], e[1] * v.e[1], e[2] * v.e[2], e[3] * v.e[3])
    operator fun times(a : Float) = vector4f(e[0] * a, e[1] * a, e[2] * a, e[3] * a)

    operator fun div(v : vector4f) : vector4f {
        require(v.e[0] != 0.0f && v.e[1] != 0.0f && v.e[2] != 0.0f && v.e[3] != 0.0f)
        return vector4f(e[0] / v.e[0], e[1] / v.e[1], e[2] / v.e[2], e[3] / v.e[3])
    }

    operator fun div(a : Float) : vector4f {
        require(a != 0.0f)
        return vector4f(e[0] / a, e[1] / a, e[2] / a, e[3] / a)
    }

    operator fun plusAssign(v : vector4f) {
        e[0] += v.e[0]
        e[1] += v.e[1]
        e[2] += v.e[2]
        e[3] += v.e[3]
    }

    operator fun minusAssign(v : vector4f) {
        e[0] -= v.e[0]
        e[1] -= v.e[1]
        e[2] -= v.e[2]
        e[3] -= v.e[3]
    }

    operator fun timesAssign(v : vector4f) {
        e[0] *= v.e[0]
        e[1] *= v.e[1]
        e[2] *= v.e[2]
        e[3] *= v.e[3]
    }

    operator fun timesAssign(a : Float) {
        e[0] *= a
        e[1] *= a
        e[2] *= a
        e[3] *= a
    }

    operator fun divAssign(v : vector4f) {
        require(v.e[0] != 0.0f && v.e[1] != 0.0f && v.e[2] != 0.0f && v.e[3] != 0.0f)
        e[0] /= v.e[0]
        e[1] /= v.e[1]
        e[2] /= v.e[2]
        e[3] /= v.e[3]
    }

    operator fun divAssign(a : Float) {
        require(a != 0.0f)
        e[0] /= a
        e[1] /= a
        e[2] /= a
        e[3] /= a
    }

    operator fun unaryMinus() = vector4f(-e[0], -e[1], -e[2], -e[3])

    fun abs() = (e[0] * e[0] + e[1] * e[1] + e[2] * e[2] + e[3] * e[3])
    fun length() = sqrt((e[0] * e[0] + e[1] * e[1] + e[2] * e[2] + e[3] * e[3]))

    fun toFloatArray() = e

    fun put(buffer : FloatArray, offset : Int) {
        buffer[offset] = e[0]
        buffer[offset + 1] = e[1]
        buffer[offset + 2] = e[2]
        buffer[offset + 3] = e[3]
    }

    fun get(buffer: FloatArray, offset : Int) {
        e[0] = buffer[offset]
        e[1] = buffer[offset + 1]
        e[2] = buffer[offset + 2]
        e[3] = buffer[offset + 3]
    }

    operator fun get(index : Int) : Float {
        require(index in 0 .. 3)
        return e[index]
    }

    operator fun set(index : Int, value : Float) {
        require(index in 0 .. 3)
        e[index] = value
    }

    companion object {
        fun mix(a : vector4f, b : vector4f, s : Float) : vector4f {
            require(s in 0.0f .. 1.0f)
            return vector4f(s * a.e[0] + (1.0f - s) * b.e[0],s * a.e[1] + (1.0f - s) * b.e[1], s * a.e[2] + (1.0f - s) * b.e[2], s * a.e[3] + (1.0f - s) * b.e[3])
        }

        fun zero() = vector4f(0.0f, 0.0f, 0.0f, 0.0f)
    }

}

operator fun Float.times(v : vector4f) = v.times(this)

