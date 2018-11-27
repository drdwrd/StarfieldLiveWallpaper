package drwdrd.ktdev.engine

class vector2f(x : Float, y : Float) {

    constructor() : this(0.0f, 0.0f)
    constructor(v : vector2f) : this(v.ex, v.ey)
    constructor(v : FloatArray) : this(v[0], v[1])

    private var ex : Float = x
    private var ey : Float = y

    val x : Float
        get() = ex
    val y : Float
        get() = ey

    override operator fun equals(other : Any?) : Boolean {
        return when(other) {
            is vector2f -> (ex == other.ex) && (ey == other.ey)
            else -> false
        }
    }

    operator fun plus(v : vector2f) = vector2f(ex + v.ex, ey + v.ey)
    operator fun minus(v : vector2f) = vector2f(ex - v.ex, ey - v.ey)
    operator fun times(v : vector2f) = vector2f(ex * v.ex, ey * v.ey)
    operator fun times(m : matrix2f) = vector2f(m[0] * ex + m[2] * ey,m[1] * ex + m[3] * ey)
    operator fun times(a : Float) = vector2f(ex * a, ey * a)

    operator fun div(v : vector2f) : vector2f {
        require(v.ex != 0.0f && v.ey != 0.0f)
        return vector2f(ex / v.ex, ey / v.ey)
    }

    operator fun div(a : Float) : vector2f {
        require(a != 0.0f)
        return vector2f(ex / a, ey / a)
    }

    operator fun plusAssign(v : vector2f) {
        ex += v.ex
        ey += v.ey
    }

    operator fun minusAssign(v : vector2f) {
        ex -= v.ex
        ey -= v.ey
    }

    operator fun timesAssign(v : vector2f) {
        ex *= v.ex
        ey *= v.ey
    }

    operator fun timesAssign(m : matrix2f) {
        ex = m[0] * ex + m[2] * ey
        ey = m[1] * ex + m[3] * ey
    }

    operator fun timesAssign(a : Float) {
        ex *= a
        ey *= a
    }

    operator fun divAssign(v : vector2f) {
        require(v.ex != 0.0f && v.ey != 0.0f)
        ex /= v.ex
        ey /= v.ey
    }

    operator fun divAssign(a : Float) {
        require(a != 0.0f)
        ex /= a
        ey /= a
    }

    operator fun unaryMinus() = vector2f(-ex, -ey)

    fun abs() = (ex * ex + ey * ey)
    fun length() = Math.sqrt((ex * ex + ey * ey).toDouble()).toFloat()

    fun transform(m : matrix3f) {
        val nex = m[0] * ex + m[3] * ey + m[6]
        val ney = m[1] * ex + m[4] * ey + m[7]
        ex = nex
        ey = ney
    }

    fun transformed(m : matrix3f) = vector2f(m[0] * ex + m[3] * ey + m[6] , m[1] * ex + m[4] * ey + m[7])

    fun rotate(m : matrix3f) {
        val nex = m[0] * ex + m[3] * ey
        val ney = m[1] * ex + m[4] * ey
        ex = nex
        ey = ney
    }

    fun rotated(m : matrix3f) = vector2f(m[0] * ex + m[3] * ey, m[1] * ex + m[4] * ey)

    fun inverseRotate(m : matrix3f) {
        val nex = m[0] * ex + m[1] * ey
        val ney = m[3] * ex + m[4] * ey
        ex = nex
        ey = ney
    }

    fun inverseRotated(m : matrix3f) = vector2f(m[0] * ex + m[1] * ey, m[3] * ex + m[4] * ey)

    fun translate(m : matrix3f) {
        ex += m[6]
        ey += m[7]
    }

    fun translated(m : matrix3f) = vector2f(ex + m[6], ey + m[7])

    fun inverseTranslate(m : matrix3f) {
        ex -= m[6]
        ey -= m[7]
    }

    fun inverseTranslated(m : matrix3f) = vector2f(ex - m[6], ey - m[7])

    fun scale(m : matrix3f) {
        ex *= m[0]
        ey *= m[4]
    }

    fun scaled(m : matrix3f) = vector2f(ex * m[0], ey * m[4])

    fun toFloatArray() = floatArrayOf(ex, ey)

    operator fun get(index : Int) : Float {
        return when(index) {
            0 -> ex
            1 -> ey
            else -> throw IndexOutOfBoundsException()
        }
    }

    operator fun set(index : Int, value : Float) {
        when(index) {
            0 -> ex = value
            1 -> ey = value
            else -> throw IndexOutOfBoundsException()
        }
    }


    companion object {
        fun mix(a : vector2f, b : vector2f, s : Float) : vector2f {
            require(s in 0.0f .. 1.0f)
            return vector2f(s * a.ex + (1.0f - s) * b.ex,s * a.ey + (1.0f - s) * b.ey)
        }
    }

}

operator fun Float.times(v : vector2f) = v.times(this)
operator fun matrix2f.times(v : vector2f) = v.times(this)
