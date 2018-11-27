package drwdrd.ktdev.engine

import android.graphics.Color


class vector3f(x : Float, y : Float, z : Float) {

    constructor() : this(0.0f, 0.0f, 0.0f)
    constructor(v : vector3f) : this(v.ex, v.ey, v.ez)
    constructor(v : FloatArray) : this(v[0], v[1], v[2])
    constructor(color : Int) : this() {
        ex = Color.red(color) / 255.0f
        ey = Color.green(color) / 255.0f
        ez = Color.blue(color) / 255.0f
    }

    private var ex : Float = x
    private var ey : Float = y
    private var ez : Float = z

    val x : Float
        get() = ex
    val y : Float
        get() = ey
    val z : Float
        get() = ez

    fun toColor()  = Color.rgb(Math.round(255.0f * ex), Math.round(255.0f * ey), Math.round(255.0f * ez))

    override operator fun equals(other : Any?) : Boolean {
        return when(other) {
            is vector3f -> (ex == other.ex) && (ey == other.ey) && (ez == other.ez)
            else -> false
        }
    }

    operator fun plus(v : vector3f) = vector3f(ex + v.ex, ey + v.ey, ez + v.ez)
    operator fun minus(v : vector3f) = vector3f(ex - v.ex, ey - v.ey, ez - v.ez)
    operator fun times(v : vector3f) = vector3f(ex * v.ex, ey * v.ey, ez * v.ez)
    operator fun times(a : Float) = vector3f(ex * a, ey * a, ez * a)

    operator fun div(v : vector3f) : vector3f {
        require(v.ex != 0.0f && v.ey != 0.0f && v.ez != 0.0f)
        return vector3f(ex / v.ex, ey / v.ey, ez / v.ez)
    }

    operator fun div(a : Float) : vector3f {
        require(a != 0.0f)
        return vector3f(ex / a, ey / a, ez / a)
    }

    operator fun plusAssign(v : vector3f) {
        ex += v.ex
        ey += v.ey
        ez += v.ez
    }

    operator fun minusAssign(v : vector3f) {
        ex -= v.ex
        ey -= v.ey
        ez -= v.ez
    }

    operator fun timesAssign(v : vector3f) {
        ex *= v.ex
        ey *= v.ey
        ez *= v.ez
    }

    operator fun timesAssign(a : Float) {
        ex *= a
        ey *= a
        ez *= a
    }

    operator fun divAssign(v : vector3f) {
        require(v.ex != 0.0f && v.ey != 0.0f && v.ez != 0.0f)
        ex /= v.ex
        ey /= v.ey
        ez /= v.ez
    }

    operator fun divAssign(a : Float) {
        require(a != 0.0f)
        ex /= a
        ey /= a
        ez /= a
    }

    operator fun unaryMinus() = vector3f(-ex, -ey, -ez)

    fun abs() = (ex * ex + ey * ey + ez * ez)
    fun length() = Math.sqrt((ex * ex + ey * ey + ez * ez).toDouble()).toFloat()

    fun normalized() : vector3f  {
        var l = length()
        if(l != 0.0f) {
            return div(l)
        }
        throw ArithmeticException()
    }

    fun toFloatArray() = floatArrayOf(ex, ey, ez)

    operator fun get(index : Int) : Float {
        return when(index) {
            0 -> ex
            1 -> ey
            2 -> ez
            else -> throw IndexOutOfBoundsException()
        }
    }

    operator fun set(index : Int, value : Float) {
        when(index) {
            0 -> ex = value
            1 -> ey = value
            2 -> ez = value
            else -> throw IndexOutOfBoundsException()
        }
    }

    companion object {
        fun mix(a : vector3f, b : vector3f, s : Float) : vector3f {
            require(s in 0.0f .. 1.0f)
            return vector3f(s * a.ex + (1.0f - s) * b.ex,s * a.ey + (1.0f - s) * b.ey, s * a.ez + (1.0f - s) * b.ez)
        }
    }

}

operator fun Float.times(v : vector3f) = v.times(this)

