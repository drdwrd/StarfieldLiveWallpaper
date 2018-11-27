package drwdrd.ktdev.engine

import android.graphics.Color


class vector4f(x : Float, y : Float, z : Float, w : Float) {

    constructor() : this(0.0f, 0.0f, 0.0f, 0.0f)
    constructor(v : vector4f) : this(v.ex, v.ey, v.ez, v.ew)
    constructor(v : FloatArray) : this(v[0], v[1], v[2], v[3])
    constructor(color : Int) : this() {
        ex = Color.red(color) / 255.0f
        ey = Color.green(color) / 255.0f
        ez = Color.blue(color) / 255.0f
        ew = Color.alpha(color) / 255.0f
    }

    private var ex : Float = x
    private var ey : Float = y
    private var ez : Float = z
    private var ew : Float = w

    val x : Float
        get() = ex
    val y : Float
        get() = ey
    val z : Float
        get() = ez
    val w : Float
        get() = ew

    fun toColor()  = Color.argb(Math.round(255.0f * ew), Math.round(255.0f * ex), Math.round(255.0f * ey), Math.round(255.0f * ez))

    override operator fun equals(other : Any?) : Boolean {
        return when(other) {
            is vector4f -> (ex == other.ex) && (ey == other.ey) && (ez == other.ez) && (ew == other.ew)
            else -> false
        }
    }

    operator fun plus(v : vector4f) = vector4f(ex + v.ex, ey + v.ey, ez + v.ez, ew + v.ew)
    operator fun minus(v : vector4f) = vector4f(ex - v.ex, ey - v.ey, ez - v.ez, ew - v.ew)
    operator fun times(v : vector4f) = vector4f(ex * v.ex, ey * v.ey, ez * v.ez, ew * v.ew)
    operator fun times(a : Float) = vector4f(ex * a, ey * a, ez * a, ew * a)

    operator fun div(v : vector4f) : vector4f {
        require(v.ex != 0.0f && v.ey != 0.0f && v.ez != 0.0f && v.ew != 0.0f)
        return vector4f(ex / v.ex, ey / v.ey, ez / v.ez, ew / v.ew)
    }

    operator fun div(a : Float) : vector4f {
        require(a != 0.0f)
        return vector4f(ex / a, ey / a, ez / a, ew / a)
    }

    operator fun plusAssign(v : vector4f) {
        ex += v.ex
        ey += v.ey
        ez += v.ez
        ew += v.ew
    }

    operator fun minusAssign(v : vector4f) {
        ex -= v.ex
        ey -= v.ey
        ez -= v.ez
        ew -= v.ew
    }

    operator fun timesAssign(v : vector4f) {
        ex *= v.ex
        ey *= v.ey
        ez *= v.ez
        ew *= v.ew
    }

    operator fun timesAssign(a : Float) {
        ex *= a
        ey *= a
        ez *= a
        ew *= a
    }

    operator fun divAssign(v : vector4f) {
        require(v.ex != 0.0f && v.ey != 0.0f && v.ez != 0.0f && v.ew != 0.0f)
        ex /= v.ex
        ey /= v.ey
        ez /= v.ez
        ew /= v.ew
    }

    operator fun divAssign(a : Float) {
        require(a != 0.0f)
        ex /= a
        ey /= a
        ez /= a
        ew /= a
    }

    operator fun unaryMinus() = vector4f(-ex, -ey, -ez, -ew)

    fun abs() = (ex * ex + ey * ey + ez * ez + ew * ew)
    fun length() = Math.sqrt((ex * ex + ey * ey + ez * ez + ew * ew).toDouble()).toFloat()

    fun toFloatArray() = floatArrayOf(ex, ey, ez, ew)

    operator fun get(index : Int) : Float {
        return when(index) {
            0 -> ex
            1 -> ey
            2 -> ez
            3 -> ew
            else -> throw IndexOutOfBoundsException()
        }
    }

    operator fun set(index : Int, value : Float) {
        when(index) {
            0 -> ex = value
            1 -> ey = value
            2 -> ez = value
            3 -> ew = value
            else -> throw IndexOutOfBoundsException()
        }
    }

    companion object {
        fun mix(a : vector4f, b : vector4f, s : Float) : vector4f {
            require(s in 0.0f .. 1.0f)
            return vector4f(s * a.ex + (1.0f - s) * b.ex,s * a.ey + (1.0f - s) * b.ey, s * a.ez + (1.0f - s) * b.ez, s * a.ew + (1.0f - s) * b.ew)
        }
    }

}

operator fun Float.times(v : vector4f) = v.times(this)

