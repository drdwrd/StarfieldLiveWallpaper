package drwdrd.ktdev.engine

class vector2f(x : Float, y : Float) {

    constructor() : this(0.0f, 0.0f)
    constructor(v : vector2f) : this(v.e[0], v.e[1])
    constructor(v : FloatArray) : this(v[0], v[1])
    constructor(v : FloatArray, offset : Int) : this(v[offset], v[offset + 1])

    private var e = floatArrayOf(x, y)

    val x : Float
        get() = e[0]
    val y : Float
        get() = e[1]

    override operator fun equals(other : Any?) : Boolean {
        return when(other) {
            is vector2f -> (e[0] == other.e[0]) && (e[1] == other.e[1])
            else -> false
        }
    }

    operator fun plus(v : vector2f) = vector2f(e[0] + v.e[0], e[1] + v.e[1])
    operator fun minus(v : vector2f) = vector2f(e[0] - v.e[0], e[1] - v.e[1])
    operator fun times(v : vector2f) = vector2f(e[0] * v.e[0], e[1] * v.e[1])
    operator fun times(m : matrix2f) = vector2f(m[0] * e[0] + m[2] * e[1],m[1] * e[0] + m[3] * e[1])
    operator fun times(a : Float) = vector2f(e[0] * a, e[1] * a)

    operator fun div(v : vector2f) : vector2f {
        require(v.e[0] != 0.0f && v.e[1] != 0.0f)
        return vector2f(e[0] / v.e[0], e[1] / v.e[1])
    }

    operator fun div(a : Float) : vector2f {
        require(a != 0.0f)
        return vector2f(e[0] / a, e[1] / a)
    }

    operator fun plusAssign(v : vector2f) {
        e[0] += v.e[0]
        e[1] += v.e[1]
    }

    operator fun minusAssign(v : vector2f) {
        e[0] -= v.e[0]
        e[1] -= v.e[1]
    }

    operator fun timesAssign(v : vector2f) {
        e[0] *= v.e[0]
        e[1] *= v.e[1]
    }

    operator fun timesAssign(m : matrix2f) {
        e[0] = m[0] * e[0] + m[2] * e[1]
        e[1] = m[1] * e[0] + m[3] * e[1]
    }

    operator fun timesAssign(a : Float) {
        e[0] *= a
        e[1] *= a
    }

    operator fun divAssign(v : vector2f) {
        require(v.e[0] != 0.0f && v.e[1] != 0.0f)
        e[0] /= v.e[0]
        e[1] /= v.e[1]
    }

    operator fun divAssign(a : Float) {
        require(a != 0.0f)
        e[0] /= a
        e[1] /= a
    }

    operator fun unaryMinus() = vector2f(-e[0], -e[1])

    fun abs() = (e[0] * e[0] + e[1] * e[1])
    fun length() = Math.sqrt((e[0] * e[0] + e[1] * e[1]).toDouble()).toFloat()

    fun transform(m : matrix3f) {
        val nex = m[0] * e[0] + m[3] * e[1] + m[6]
        val ney = m[1] * e[0] + m[4] * e[1] + m[7]
        e[0] = nex
        e[1] = ney
    }

    fun transformed(m : matrix3f) = vector2f(m[0] * e[0] + m[3] * e[1] + m[6] , m[1] * e[0] + m[4] * e[1] + m[7])

    fun rotate(m : matrix3f) {
        val nex = m[0] * e[0] + m[3] * e[1]
        val ney = m[1] * e[0] + m[4] * e[1]
        e[0] = nex
        e[1] = ney
    }

    fun rotated(m : matrix3f) = vector2f(m[0] * e[0] + m[3] * e[1], m[1] * e[0] + m[4] * e[1])

    fun inverseRotate(m : matrix3f) {
        val nex = m[0] * e[0] + m[1] * e[1]
        val ney = m[3] * e[0] + m[4] * e[1]
        e[0] = nex
        e[1] = ney
    }

    fun inverseRotated(m : matrix3f) = vector2f(m[0] * e[0] + m[1] * e[1], m[3] * e[0] + m[4] * e[1])

    fun translate(m : matrix3f) {
        e[0] += m[6]
        e[1] += m[7]
    }

    fun translated(m : matrix3f) = vector2f(e[0] + m[6], e[1] + m[7])

    fun inverseTranslate(m : matrix3f) {
        e[0] -= m[6]
        e[1] -= m[7]
    }

    fun inverseTranslated(m : matrix3f) = vector2f(e[0] - m[6], e[1] - m[7])

    fun scale(m : matrix3f) {
        e[0] *= m[0]
        e[1] *= m[4]
    }

    fun scaled(m : matrix3f) = vector2f(e[0] * m[0], e[1] * m[4])

    fun toFloatArray() = e

    fun put(buffer : FloatArray, offset : Int) {
        buffer[offset] = e[0]
        buffer[offset + 1] = e[1]
    }

    fun get(buffer: FloatArray, offset : Int) {
        e[0] = buffer[offset]
        e[1] = buffer[offset + 1]
    }

    operator fun get(index : Int) : Float {
        require(index in 0 .. 1)
        return e[index]
    }

    operator fun set(index : Int, value : Float) {
        require(index in 0 .. 1)
        e[index] = value
    }


    companion object {
        fun mix(a : vector2f, b : vector2f, s : Float) : vector2f {
            require(s in 0.0f .. 1.0f)
            return vector2f(s * a.e[0] + (1.0f - s) * b.e[0],s * a.e[1] + (1.0f - s) * b.e[1])
        }
    }

}

operator fun Float.times(v : vector2f) = v.times(this)
operator fun matrix2f.times(v : vector2f) = v.times(this)
