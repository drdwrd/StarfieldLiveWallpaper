package drwdrd.ktdev.engine

class matrix3f(e0 : Float, e1 : Float, e2 : Float, e3 : Float, e4 : Float, e5 : Float, e6 : Float, e7 : Float, e8 : Float) {

    constructor() : this(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f ,0.0f, 0.0f, 0.0f)
    constructor(m : matrix3f) : this(m[0], m[1], m[2], m[3], m[4], m[5], m[6], m[7], m[8])
    constructor(m : FloatArray) : this(m[0], m[1], m[2], m[3], m[4], m[5], m[6], m[7], m[8])

    private var e = floatArrayOf(e0, e1, e2, e3, e4, e5, e6, e7, e8)

    operator fun get(index : Int) : Float {
        require(index in 0 .. 8)
        return e[index]
    }

    fun equals(m : matrix3f) : Boolean {
        return ((e[0] == m.e[0]) && (e[3] == m.e[3]) && (e[6] == m.e[6]) &&
                (e[1] == m.e[1]) && (e[4] == m.e[4]) && (e[7] == m.e[7]) &&
                (e[2] == m.e[2]) && (e[5] == m.e[5]) && (e[8] == m.e[8]))

    }

    override fun equals(other: Any?): Boolean {
        return when(other) {
            is matrix3f -> this.equals(other)
            else -> false
        }
    }

    fun loadIdentity() {
        e[0] = 1.0f
        e[1] = 0.0f
        e[2] = 0.0f
        e[3] = 0.0f
        e[4] = 1.0f
        e[5] = 0.0f
        e[6] = 0.0f
        e[7] = 0.0f
        e[8] = 1.0f
    }

    fun loadZero() {
        e[0] = 0.0f
        e[1] = 0.0f
        e[2] = 0.0f
        e[3] = 0.0f
        e[4] = 0.0f
        e[5] = 0.0f
        e[6] = 0.0f
        e[7] = 0.0f
        e[8] = 0.0f
    }

    operator fun plus(m : matrix3f) = matrix3f(e[0] + m.e[0], e[1] + m.e[1], e[2] + m.e[2], e[3] + m.e[3], e[4] + m.e[4], e[5] + m.e[5], e[6] + m.e[6], e[7] + m.e[7], e[8] + m.e[8])
    operator fun minus(m : matrix3f) = matrix3f(e[0] - m.e[0], e[1] - m.e[1], e[2] - m.e[2], e[3] - m.e[3], e[4] - m.e[4], e[5] - m.e[5], e[6] - m.e[6], e[7] - m.e[7], e[8] - m.e[8])

    operator fun times(m : matrix3f) : matrix3f {
        val ne0 = e[0] * m.e[0] + e[3] * m.e[1] + e[6] * m.e[2]
        val ne1 = e[1] * m.e[0] + e[4] * m.e[1] + e[7] * m.e[2]
        val ne2 = e[2] * m.e[0] + e[5] * m.e[1] + e[8] * m.e[2]
        val ne3 = e[0] * m.e[3] + e[3] * m.e[4] + e[6] * m.e[5]
        val ne4 = e[1] * m.e[3] + e[4] * m.e[4] + e[7] * m.e[5]
        val ne5 = e[2] * m.e[3] + e[5] * m.e[4] + e[8] * m.e[5]
        val ne6 = e[0] * m.e[6] + e[3] * m.e[7] + e[6] * m.e[8]
        val ne7 = e[1] * m.e[6] + e[4] * m.e[7] + e[7] * m.e[8]
        val ne8 = e[2] * m.e[6] + e[5] * m.e[7] + e[8] * m.e[8]

        return matrix3f(ne0, ne1, ne2, ne3, ne4, ne5, ne6, ne7, ne8)
    }

    operator fun times(s : Float) = matrix3f(e[0] * s, e[1] * s, e[2] * s, e[3] * s, e[4] * s, e[5] * s, e[6] * s, e[7] * s, e[8] * s)

    operator fun plusAssign(m : matrix3f) {
        e[0] += m.e[0]
        e[1] += m.e[1]
        e[2] += m.e[2]
        e[3] += m.e[3]
        e[4] += m.e[4]
        e[5] += m.e[5]
        e[6] += m.e[6]
        e[7] += m.e[7]
        e[8] += m.e[8]
    }

    operator fun minusAssign(m : matrix3f) {
        e[0] -= m.e[0]
        e[1] -= m.e[1]
        e[2] -= m.e[2]
        e[3] -= m.e[3]
        e[4] -= m.e[4]
        e[5] -= m.e[5]
        e[6] -= m.e[6]
        e[7] -= m.e[7]
        e[8] -= m.e[8]
    }

    operator fun timesAssign(m : matrix3f) {
        val ne0 = e[0] * m.e[0] + e[3] * m.e[1] + e[6] * m.e[2]
        val ne1 = e[1] * m.e[0] + e[4] * m.e[1] + e[7] * m.e[2]
        val ne2 = e[2] * m.e[0] + e[5] * m.e[1] + e[8] * m.e[2]
        val ne3 = e[0] * m.e[3] + e[3] * m.e[4] + e[6] * m.e[5]
        val ne4 = e[1] * m.e[3] + e[4] * m.e[4] + e[7] * m.e[5]
        val ne5 = e[2] * m.e[3] + e[5] * m.e[4] + e[8] * m.e[5]
        val ne6 = e[0] * m.e[6] + e[3] * m.e[7] + e[6] * m.e[8]
        val ne7 = e[1] * m.e[6] + e[4] * m.e[7] + e[7] * m.e[8]
        val ne8 = e[2] * m.e[6] + e[5] * m.e[7] + e[8] * m.e[8]

        e[0] = ne0
        e[1] = ne1
        e[2] = ne2
        e[3] = ne3
        e[4] = ne4
        e[5] = ne5
        e[6] = ne6
        e[7] = ne7
        e[8] = ne8
    }

    operator fun timesAssign(a : Float) {
        e[0] *= a
        e[1] *= a
        e[2] *= a
        e[3] *= a
        e[4] *= a
        e[5] *= a
        e[6] *= a
        e[7] *= a
        e[8] *= a
    }

    operator fun divAssign(a : Float) {
        require(a != 0.0f)
        e[0] /= a
        e[1] /= a
        e[2] /= a
        e[3] /= a
        e[4] /= a
        e[5] /= a
        e[6] /= a
        e[7] /= a
        e[8] /= a
    }

    operator fun unaryMinus() = matrix3f(-e[0], -e[1], -e[2], -e[3], -e[4], -e[5], -e[6], -e[7], -e[8])

    fun transpose() {
        val te1 = e[1]
        e[1] = e[3]
        e[3] = te1
        val te2 = e[2]
        e[2] = e[6]
        e[6] = te2
        val te5 = e[5]
        e[5] = e[7]
        e[7] = te5
    }

    fun det() = (e[0] * (e[4] * e[8] - e[7] * e[5]) - e[1] * (e[3] * e[8] - e[5] * e[6]) + e[2] * (e[3] * e[7] - e[4] * e[6]))

    fun inverse() {
        val idet = 1.0f / det()
        val inv0 = idet * (e[4] * e[8] - e[7] * e[5])
        val inv3 = idet * (e[6] * e[5] - e[3] * e[8])
        val inv6 = idet * (e[3] * e[7] - e[6] * e[4])
        val inv1 = idet * (e[7] * e[2] - e[1] * e[8])
        val inv4 = idet * (e[0] * e[8] - e[6] * e[2])
        val inv7 = idet * (e[6] * e[1] - e[0] * e[7])
        val inv2 = idet * (e[1] * e[5] - e[4] * e[2])
        val inv5 = idet * (e[3] * e[2] - e[0] * e[5])
        val inv8 = idet * (e[0] * e[4] - e[3] * e[1])

        e[0] = inv0
        e[3] = inv3
        e[6] = inv6
        e[1] = inv1
        e[4] = inv4
        e[7] = inv7
        e[2] = inv2
        e[5] = inv5
        e[8] = inv8
    }

    fun setScale(sx : Float, sy : Float) {
        loadIdentity()
        e[0] = sx
        e[4] = sy
    }

    fun setScale(s : vector2f) {
        loadIdentity()
        e[0] = s.x
        e[4] = s.y
    }

    fun setScale(s : FloatArray) {
        loadIdentity()
        e[0] = s[0]
        e[4] = s[1]
    }

    fun setScalePart(sx : Float, sy : Float) {
        e[0] = sx
        e[4] = sy
    }

    fun setScalePart(s : vector2f) {
        e[0] = s.x
        e[4] = s.y
    }

    fun setScalePart(s : FloatArray) {
        e[0] = s[0]
        e[4] = s[1]
    }

    fun setTranslation(tx : Float, ty : Float) {
        loadIdentity()
        e[6] = tx
        e[7] = ty
    }

    fun setTranslation(t : vector2f) {
        loadIdentity()
        e[6] = t.x
        e[7] = t.y
    }

    fun setTranslation(t : FloatArray) {
        loadIdentity()
        e[6] = t[0]
        e[7] = t[1]
    }

    fun setTranslationPart(tx : Float, ty : Float) {
        e[6] = tx
        e[7] = ty
    }

    fun setTranslationPart(t : vector2f) {
        e[6] = t.x
        e[7] = t.y
    }

    fun setTranslationPart(t : FloatArray) {
        e[6] = t[0]
        e[7] = t[1]
    }

    fun setRotation(angle : Float) {
        loadIdentity()
        e[0] = Math.cos(angle.toDouble()).toFloat()
        e[1] = Math.sin(angle.toDouble()).toFloat()
        e[3] = -Math.sin(angle.toDouble()).toFloat()
        e[4] = Math.cos(angle.toDouble()).toFloat()
    }

    fun setRotationPart(angle : Float) {
        e[0] = Math.cos(angle.toDouble()).toFloat()
        e[1] = Math.sin(angle.toDouble()).toFloat()
        e[3] = -Math.sin(angle.toDouble()).toFloat()
        e[4] = Math.cos(angle.toDouble()).toFloat()
    }

    fun toFloatArray() = e.copyOf()

    companion object {
        fun identity() = matrix3f(1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f)
        fun zero() = matrix3f(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f ,0.0f, 0.0f)
    }
}