package drwdrd.ktdev.engine

class matrix2f(e0 : Float, e1 : Float, e2 : Float, e3 : Float) {

    constructor() : this(0.0f, 0.0f, 0.0f, 0.0f)
    constructor(m : matrix2f) : this(m[0], m[1], m[2], m[3])
    constructor(m : FloatArray) : this(m[0], m[1], m[2], m[3])
    constructor(m : FloatArray, offset: Int) : this(m[offset], m[offset + 1], m[offset + 2], m[offset + 3])

    private var e : FloatArray = floatArrayOf(e0, e1, e2, e3)

    operator fun get(index : Int) : Float {
        require(index in 0 .. 3)
        return e[index]
    }

    operator fun set(index : Int, value : Float) {
        require(index in 0 .. 3)
        e[index] = value
    }

    fun equals(m : matrix2f) = ((e[0] == m.e[0]) && (e[1] == m.e[1]) && (e[2] == m.e[2]) && (e[3] == m.e[3]))

    override operator fun equals(other: Any?): Boolean {
        return when(other) {
            is matrix2f -> this.equals(other)
            else -> false
        }
    }

    fun loadIdentity() {
        e[0] = 1.0f
        e[1] = 0.0f
        e[2] = 0.0f
        e[3] = 1.0f
    }

    fun loadZero() {
        e[0] = 0.0f
        e[1] = 0.0f
        e[2] = 0.0f
        e[3] = 0.0f
    }

    operator fun plus(m : matrix2f) = matrix2f(e[0] + m.e[0], e[1] + m.e[1], e[2] + m.e[2], e[3] + m.e[3])
    operator fun minus(m : matrix2f) = matrix2f(e[0] - m.e[0], e[1] - m.e[1], e[2] - m.e[2], e[3] - m.e[3])
    operator fun times(m : matrix2f) = matrix2f(e[0] * m.e[0] + e[2] * m.e[1], e[1] * m.e[0] + e[3] * m.e[1], e[0] * m.e[2] + e[2] * m.e[3], e[1] * m.e[2] + e[3] * m.e[3])
    operator fun times(s : Float) = matrix2f(e[0] * s, e[1] * s, e[2] * s, e[3] * s)

    operator fun plusAssign(m : matrix2f) {
        e[0] += m.e[0]
        e[1] += m.e[1]
        e[2] += m.e[2]
        e[3] += m.e[3]
    }

    operator fun minusAssign(m : matrix2f) {
        e[0] -= m.e[0]
        e[1] -= m.e[1]
        e[2] -= m.e[2]
        e[3] -= m.e[3]
    }

    operator fun timesAssign(m : matrix2f) {
        val ne0 = e[0] * m.e[0] + e[2] * m.e[1]
        val ne1 = e[1] * m.e[0] + e[3] * m.e[1]
        val ne2 = e[0] * m.e[2] + e[2] * m.e[3]
        val ne3 = e[1] * m.e[2] + e[3] * m.e[3]

        e[0] = ne0
        e[1] = ne1
        e[2] = ne2
        e[3] = ne3
    }

    operator fun timesAssign(a : Float) {
        e[0] *= a
        e[1] *= a
        e[2] *= a
        e[3] *= a
    }

    operator fun divAssign(a : Float) {
        require(a != 0.0f)
        e[0] /= a
        e[1] /= a
        e[2] /= a
        e[3] /= a
    }


    operator fun unaryMinus() = matrix2f(-e[0], -e[1], -e[2], -e[3])

    fun transpose() {
        val tmp = e[1]
        e[1] = e[2]
        e[2] = tmp
    }

    fun det() = e[0] * e[3] - e[1] * e[2]

    fun inverse() {
        val idet = 1.0f / det()
        val tmp = e[0]

        e[0] = idet * e[3]
        e[2] = -idet * e[2]

        e[1] = -idet * e[1]
        e[3] = idet * tmp
    }

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


    companion object {
        fun identity() = matrix2f(1.0f, 0.0f, 0.0f, 1.0f)
        fun zero() = matrix2f(0.0f, 0.0f, 0.0f, 0.0f)
    }
}