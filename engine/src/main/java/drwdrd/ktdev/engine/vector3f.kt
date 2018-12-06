package drwdrd.ktdev.engine

import android.graphics.Color
import kotlin.math.sqrt


class vector3f(x : Float, y : Float, z : Float) {

    constructor() : this(0.0f, 0.0f, 0.0f)
    constructor(v : vector3f) : this(v.e[0], v.e[1], v.e[2])
    constructor(v : FloatArray) : this(v[0], v[1], v[2])
    constructor(v : FloatArray, offset : Int) : this(v[offset], v[offset + 1], v[offset + 2])

    constructor(color : Int) : this() {
        e[0] = Color.red(color) / 255.0f
        e[1] = Color.green(color) / 255.0f
        e[2] = Color.blue(color) / 255.0f
    }

    private var e = floatArrayOf(x, y, z)

    val x : Float
        get() = e[0]
    val y : Float
        get() = e[1]
    val z : Float
        get() = e[2]

    fun toColor()  = Color.rgb(Math.round(255.0f * e[0]), Math.round(255.0f * e[1]), Math.round(255.0f * e[2]))

    override operator fun equals(other : Any?) : Boolean {
        return when(other) {
            is vector3f -> ((e[0] == other.e[0]) && (e[1] == other.e[1]) && (e[2] == other.e[2]))
            else -> false
        }
    }

    operator fun plus(v : vector3f) = vector3f(e[0] + v.e[0], e[1] + v.e[1], e[2] + v.e[2])
    operator fun minus(v : vector3f) = vector3f(e[0] - v.e[0], e[1] - v.e[1], e[2] - v.e[2])
    operator fun times(v : vector3f) = vector3f(e[0] * v.e[0], e[1] * v.e[1], e[2] * v.e[2])
    operator fun times(a : Float) = vector3f(e[0] * a, e[1] * a, e[2] * a)

    operator fun div(v : vector3f) : vector3f {
        require(v.e[0] != 0.0f && v.e[1] != 0.0f && v.e[2] != 0.0f)
        return vector3f(e[0] / v.e[0], e[1] / v.e[1], e[2] / v.e[2])
    }

    operator fun div(a : Float) : vector3f {
        require(a != 0.0f)
        return vector3f(e[0] / a, e[1] / a, e[2] / a)
    }

    operator fun plusAssign(v : vector3f) {
        e[0] += v.e[0]
        e[1] += v.e[1]
        e[2] += v.e[2]
    }

    operator fun minusAssign(v : vector3f) {
        e[0] -= v.e[0]
        e[1] -= v.e[1]
        e[2] -= v.e[2]
    }

    operator fun timesAssign(v : vector3f) {
        e[0] *= v.e[0]
        e[1] *= v.e[1]
        e[2] *= v.e[2]
    }

    operator fun timesAssign(a : Float) {
        e[0] *= a
        e[1] *= a
        e[2] *= a
    }

    operator fun divAssign(v : vector3f) {
        require(v.e[0] != 0.0f && v.e[1] != 0.0f && v.e[2] != 0.0f)
        e[0] /= v.e[0]
        e[1] /= v.e[1]
        e[2] /= v.e[2]
    }

    operator fun divAssign(a : Float) {
        require(a != 0.0f)
        e[0] /= a
        e[1] /= a
        e[2] /= a
    }

    operator fun unaryMinus() = vector3f(-e[0], -e[1], -e[2])

    fun abs() = (e[0] * e[0] + e[1] * e[1] + e[2] * e[2])
    fun length() = sqrt(e[0] * e[0] + e[1] * e[1] + e[2] * e[2])

    fun normalize()  {
        var l = length()
        if(l != 0.0f) {
            divAssign(l)
        }
        throw ArithmeticException()
    }

    fun normalized() : vector3f  {
        var l = length()
        if(l != 0.0f) {
            return div(l)
        }
        return vector3f(0.0f, 0.0f, 0.0f)
    }

    fun toFloatArray() = e

    fun put(buffer : FloatArray, offset : Int) {
        buffer[offset] = e[0]
        buffer[offset + 1] = e[1]
        buffer[offset + 2] = e[2]
    }

    fun get(buffer: FloatArray, offset : Int) {
        e[0] = buffer[offset]
        e[1] = buffer[offset + 1]
        e[2] = buffer[offset + 2]
    }

    operator fun get(index : Int) : Float {
        require(index in 0 .. 2)
        return e[index]
    }

    operator fun set(index : Int, value : Float) {
        require(index in 0 .. 2)
        e[index] = value
    }

    fun dot(v : vector3f) = e[0] * v.e[0] + e[1] * v.e[1] + e[2] * v.e[2]

    fun cross(v : vector3f) = vector3f(e[1] * v.e[2] - e[2] * v.e[1], e[2] * v.e[0] - e[0] * v.e[2], e[0] * v.e[1] - e[1] * v.e[0])


    companion object {
        fun mix(a : vector3f, b : vector3f, s : Float) : vector3f {
            require(s in 0.0f .. 1.0f)
            return vector3f(s * a.e[0] + (1.0f - s) * b.e[0],s * a.e[1] + (1.0f - s) * b.e[1], s * a.e[2] + (1.0f - s) * b.e[2])
        }

        fun dot(v1 : vector3f, v2 : vector3f) = v1.dot(v2)

        fun cross(v1 : vector3f, v2 : vector3f) = v1.cross(v2)
    }

}

operator fun Float.times(v : vector3f) = v.times(this)

