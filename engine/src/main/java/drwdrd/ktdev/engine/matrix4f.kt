package drwdrd.ktdev.engine

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

class matrix4f(e0 : Float, e1 : Float, e2 : Float, e3 : Float, e4 : Float, e5 : Float, e6 : Float, e7 : Float,
        e8 : Float, e9 : Float, e10 : Float, e11 : Float, e12 : Float, e13 : Float, e14 : Float, e15 : Float) {

    constructor() : this(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f ,0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f)
    constructor(m : matrix3f) : this(m[0], m[1], m[2], m[3], m[4], m[5], m[6], m[7], m[8], m[9], m[10], m[11], m[12], m[13], m[14], m[15])
    constructor(m : FloatArray) : this(m[0], m[1], m[2], m[3], m[4], m[5], m[6], m[7], m[8], m[9], m[10], m[11], m[12], m[13], m[14], m[15])


    private var e = floatArrayOf(e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13, e14, e15)

    operator fun get(index : Int) : Float {
        require(index in 0 .. 15)
        return e[index]
    }

    fun equals(m : matrix4f) : Boolean {
        return ((e[0] == m.e[0]) && (e[4] == m.e[4]) && (e[8] == m.e[8]) && (e[12] == m.e[12]) &&
                (e[1] == m.e[1]) && (e[5] == m.e[5]) && (e[9] == m.e[9]) && (e[13] == m.e[13]) &&
                (e[2] == m.e[2]) && (e[6] == m.e[6]) && (e[10] == m.e[10]) && (e[14] == m.e[14]) &&
                (e[3] == m.e[3]) && (e[7] == m.e[7]) && (e[11] == m.e[11]) && (e[15] == m.e[15]))

    }

    override fun equals(other: Any?): Boolean {
        return when(other) {
            is matrix4f -> this.equals(other)
            else -> false
        }
    }

    fun loadIdentity() {
        e[0] = 1.0f; e[4] = 0.0f; e[8] = 0.0f; e[12] = 0.0f
        e[1] = 0.0f; e[5] = 1.0f; e[9] = 0.0f; e[13] = 0.0f
        e[2] = 0.0f; e[6] = 0.0f; e[10] = 1.0f; e[14] = 0.0f
        e[3] = 0.0f; e[7] = 0.0f; e[11] = 0.0f; e[15] = 1.0f
    }

    fun loadZero() {
        e[0] = 0.0f; e[4] = 0.0f; e[8] = 0.0f; e[12] = 0.0f
        e[1] = 0.0f; e[5] = 0.0f; e[9] = 0.0f; e[13] = 0.0f
        e[2] = 0.0f; e[6] = 0.0f; e[10] = 0.0f; e[14] = 0.0f
        e[3] = 0.0f; e[7] = 0.0f; e[11] = 0.0f; e[15] = 0.0f
    }

    operator fun plus(m : matrix4f) = matrix4f(
        e[0] + m.e[0], e[1] + m.e[1], e[2] + m.e[2], e[3] + m.e[3],
        e[4] + m.e[4], e[5] + m.e[5], e[6] + m.e[6], e[7] + m.e[7],
        e[8] + m.e[8], e[9] + m.e[9], e[10] + m.e[10], e[11] + m.e[11],
        e[12] + m.e[12], e[13] + m.e[13], e[14] + m.e[14], e[15] + m.e[15])

    operator fun minus(m : matrix4f) = matrix4f(
    e[0] - m.e[0], e[1] - m.e[1], e[2] - m.e[2], e[3] - m.e[3],
    e[4] - m.e[4], e[5] - m.e[5], e[6] - m.e[6], e[7] - m.e[7],
    e[8] - m.e[8], e[9] - m.e[9], e[10] - m.e[10], e[11] - m.e[11],
    e[12] - m.e[12], e[13] - m.e[13], e[14] - m.e[14], e[15] - m.e[15])

    operator fun times(m : matrix4f) : matrix4f {
        var ne0 = e[0] * m.e[0] + e[4] * m.e[1] + e[8] * m.e[2] + e[12] * m.e[3]
        var ne1 = e[1] * m.e[0] + e[5] * m.e[1] + e[9] * m.e[2] + e[13] * m.e[3]
        var ne2 = e[2] * m.e[0] + e[6] * m.e[1] + e[10] * m.e[2] + e[14] * m.e[3]
        var ne3 = e[3] * m.e[0] + e[7] * m.e[1] + e[11] * m.e[2] + e[15] * m.e[3]

        var ne4 = e[0] * m.e[4] + e[4] * m.e[5] + e[8] * m.e[6] + e[12] * m.e[7]
        var ne5 = e[1] * m.e[4] + e[5] * m.e[5] + e[9] * m.e[6] + e[13] * m.e[7]
        var ne6 = e[2] * m.e[4] + e[6] * m.e[5] + e[10] * m.e[6] + e[14] * m.e[7]
        var ne7 = e[3] * m.e[4] + e[7] * m.e[5] + e[11] * m.e[6] + e[15] * m.e[7]

        var ne8 = e[0] * m.e[8] + e[4] * m.e[9] + e[8] * m.e[10] + e[12] * m.e[11]
        var ne9 = e[1] * m.e[8] + e[5] * m.e[9] + e[9] * m.e[10] + e[13] * m.e[11]
        var ne10 = e[2] * m.e[8] + e[6] * m.e[9] + e[10] * m.e[10] + e[14] * m.e[11]
        var ne11 = e[3] * m.e[8] + e[7] * m.e[9] + e[11] * m.e[10] + e[15] * m.e[11]

        var ne12 = e[0] * m.e[12] + e[4] * m.e[13] + e[8] * m.e[14] + e[12] * m.e[15]
        var ne13 = e[1] * m.e[12] + e[5] * m.e[13] + e[9] * m.e[14] + e[13] * m.e[15]
        var ne14 = e[2] * m.e[12] + e[6] * m.e[13] + e[10] * m.e[14] + e[14] * m.e[15]
        var ne15 = e[3] * m.e[12] + e[7] * m.e[13] + e[11] * m.e[14] + e[15] * m.e[15]

        return  matrix4f(ne0, ne1, ne2, ne3, ne4, ne5, ne6, ne7, ne8, ne9, ne10, ne11, ne12, ne13, ne14, ne15)
    }

    operator fun times(s : Float) = matrix4f(e[0] * s, e[1] * s, e[2] * s, e[3] * s, e[4] * s, e[5] * s, e[6] * s, e[7] * s, e[8] * s, e[9] * s, e[10] * s, e[11] * s, e[12] * s, e[13] * s, e[14] * s, e[15] * s)


    operator fun plusAssign(m : matrix4f) {
        e[0] += m.e[0]
        e[1] += m.e[1]
        e[2] += m.e[2]
        e[3] += m.e[3]
        e[4] += m.e[4]
        e[5] += m.e[5]
        e[6] += m.e[6]
        e[7] += m.e[7]
        e[8] += m.e[8]
        e[9] += m.e[9]
        e[10] += m.e[10]
        e[11] += m.e[11]
        e[12] += m.e[12]
        e[13] += m.e[13]
        e[14] += m.e[14]
        e[15] += m.e[15]
    }

    operator fun minusAssign(m : matrix4f) {
        e[0] -= m.e[0]
        e[1] -= m.e[1]
        e[2] -= m.e[2]
        e[3] -= m.e[3]
        e[4] -= m.e[4]
        e[5] -= m.e[5]
        e[6] -= m.e[6]
        e[7] -= m.e[7]
        e[8] -= m.e[8]
        e[9] -= m.e[9]
        e[10] -= m.e[10]
        e[11] -= m.e[11]
        e[12] -= m.e[12]
        e[13] -= m.e[13]
        e[14] -= m.e[14]
        e[15] -= m.e[15]
    }

    operator fun timesAssign(m : matrix4f) {
        var ne0 = e[0] * m.e[0] + e[4] * m.e[1] + e[8] * m.e[2] + e[12] * m.e[3]
        var ne1 = e[1] * m.e[0] + e[5] * m.e[1] + e[9] * m.e[2] + e[13] * m.e[3]
        var ne2 = e[2] * m.e[0] + e[6] * m.e[1] + e[10] * m.e[2] + e[14] * m.e[3]
        var ne3 = e[3] * m.e[0] + e[7] * m.e[1] + e[11] * m.e[2] + e[15] * m.e[3]

        var ne4 = e[0] * m.e[4] + e[4] * m.e[5] + e[8] * m.e[6] + e[12] * m.e[7]
        var ne5 = e[1] * m.e[4] + e[5] * m.e[5] + e[9] * m.e[6] + e[13] * m.e[7]
        var ne6 = e[2] * m.e[4] + e[6] * m.e[5] + e[10] * m.e[6] + e[14] * m.e[7]
        var ne7 = e[3] * m.e[4] + e[7] * m.e[5] + e[11] * m.e[6] + e[15] * m.e[7]

        var ne8 = e[0] * m.e[8] + e[4] * m.e[9] + e[8] * m.e[10] + e[12] * m.e[11]
        var ne9 = e[1] * m.e[8] + e[5] * m.e[9] + e[9] * m.e[10] + e[13] * m.e[11]
        var ne10 = e[2] * m.e[8] + e[6] * m.e[9] + e[10] * m.e[10] + e[14] * m.e[11]
        var ne11 = e[3] * m.e[8] + e[7] * m.e[9] + e[11] * m.e[10] + e[15] * m.e[11]

        var ne12 = e[0] * m.e[12] + e[4] * m.e[13] + e[8] * m.e[14] + e[12] * m.e[15]
        var ne13 = e[1] * m.e[12] + e[5] * m.e[13] + e[9] * m.e[14] + e[13] * m.e[15]
        var ne14 = e[2] * m.e[12] + e[6] * m.e[13] + e[10] * m.e[14] + e[14] * m.e[15]
        var ne15 = e[3] * m.e[12] + e[7] * m.e[13] + e[11] * m.e[14] + e[15] * m.e[15]

        e[0] = ne0
        e[1] = ne1
        e[2] = ne2
        e[3] = ne3
        e[4] = ne4
        e[5] = ne5
        e[6] = ne6
        e[7] = ne7
        e[8] = ne8
        e[9] = ne9
        e[10] = ne10
        e[11] = ne11
        e[12] = ne12
        e[13] = ne13
        e[14] = ne14
        e[15] = ne15
    }

    operator fun timesAssign(s : Float) {
        e[0] *= s
        e[1] *= s
        e[2] *= s
        e[3] *= s
        e[4] *= s
        e[5] *= s
        e[6] *= s
        e[7] *= s
        e[8] *= s
        e[9] *= s
        e[10] *= s
        e[11] *= s
        e[12] *= s
        e[13] *= s
        e[14] *= s
        e[15] *= s
    }

    operator fun divAssign(s : Float) {
        require(s != 0.0f)
        e[0] /= s
        e[1] /= s
        e[2] /= s
        e[3] /= s
        e[4] /= s
        e[5] /= s
        e[6] /= s
        e[7] /= s
        e[8] /= s
        e[9] /= s
        e[10] /= s
        e[11] /= s
        e[12] /= s
        e[13] /= s
        e[14] /= s
        e[15] /= s
    }

    operator fun unaryMinus() = matrix4f(-e[0], -e[1], -e[2], -e[3], -e[4], -e[5], -e[6], -e[7], -e[8], -e[9], -e[10], -e[11], -e[12], -e[13], -e[14], -e[15])

    fun transpose() {
        val te1 = e[1]
        e[1] = e[4]
        val te2 = e[2]
        e[2] = e[8]
        val te3 = e[3]
        e[3] = e[12]

        e[4] = te1
        val te6 = e[6]
        e[6] = e[8]
        val te7 = e[7]
        e[7] = e[13]

        e[8] = te2
        e[9] = te6
        val te11 = e[11]
        e[11] = e[14]

        e[12] = te3
        e[13] = te7
        e[14] = te11
    }

    fun det() : Float {

        val vec0 = e[5] * e[10] * e[15] - e[5] * e[11] * e[14] - e[9] * e[6] * e[15] + e[9] * e[7] * e[14] + e[13] * e[6] * e[11] - e[13] * e[7] * e[10]

        val vec1 = -e[4] * e[10] * e[15] + e[4] * e[11] * e[14] + e[8] * e[6] * e[15] - e[8] * e[7] * e[14] - e[12] * e[6] * e[11] + e[12] * e[7] * e[10]

        val vec2 = e[4] * e[9] * e[15] - e[4] * e[11] * e[13] - e[8] * e[5] * e[15] + e[8] * e[7] * e[13] + e[12] * e[5] * e[11] - e[12] * e[7] * e[9]

        val vec3 = -e[4] * e[9] * e[14] + e[4] * e[10] * e[13] + e[8] * e[5] * e[14] - e[8] * e[6] * e[13] - e[12] * e[5] * e[10] + e[12] * e[6] * e[9]

        return e[0] * vec0 + e[1] * vec1 + e[2] * vec2 + e[3] * vec3
    }

    fun inverse() {
        val inv0 = e[5] * e[10] * e[15] - e[5] * e[11] * e[14] - e[9] * e[6] * e[15] + e[9] * e[7] * e[14] + e[13] * e[6] * e[11] - e[13] * e[7] * e[10]

        val inv4 = -e[4] * e[10] * e[15] + e[4] * e[11] * e[14] + e[8] * e[6] * e[15] - e[8] * e[7] * e[14] - e[12] * e[6] * e[11] + e[12] * e[7] * e[10]

        val inv8 = e[4] * e[9] * e[15] - e[4] * e[11] * e[13] - e[8] * e[5] * e[15] + e[8] * e[7] * e[13] + e[12] * e[5] * e[11] - e[12] * e[7] * e[9]

        val inv12 = -e[4] * e[9] * e[14] + e[4] * e[10] * e[13] + e[8] * e[5] * e[14] - e[8] * e[6] * e[13] - e[12] * e[5] * e[10] + e[12] * e[6] * e[9]

        val inv1 = -e[1] * e[10] * e[15] + e[1] * e[11] * e[14] + e[9] * e[2] * e[15] - e[9] * e[3] * e[14] - e[13] * e[2] * e[11] + e[13] * e[3] * e[10]

        val inv5 = e[0] * e[10] * e[15] - e[0] * e[11] * e[14] - e[8] * e[2] * e[15] + e[8] * e[3] * e[14] + e[12] * e[2] * e[11] - e[12] * e[3] * e[10]

        val inv9 = -e[0] * e[9] * e[15] + e[0] * e[11] * e[13] + e[8] * e[1] * e[15] - e[8] * e[3] * e[13] - e[12] * e[1] * e[11] + e[12] * e[3] * e[9]

        val inv13 = e[0] * e[9] * e[14] - e[0] * e[10] * e[13] - e[8] * e[1] * e[14] + e[8] * e[2] * e[13] + e[12] * e[1] * e[10] - e[12] * e[2] * e[9]

        val inv2 = e[1] * e[6] * e[15] - e[1] * e[7] * e[14] - e[5] * e[2] * e[15] + e[5] * e[3] * e[14] + e[13] * e[2] * e[7] - e[13] * e[3] * e[6]

        val inv6 = -e[0] * e[6] * e[15] + e[0] * e[7] * e[14] + e[4] * e[2] * e[15] - e[4] * e[3] * e[14] - e[12] * e[2] * e[7] + e[12] * e[3] * e[6]

        val inv10 = e[0] * e[5] * e[15] - e[0] * e[7] * e[13] - e[4] * e[1] * e[15] + e[4] * e[3] * e[13] + e[12] * e[1] * e[7] - e[12] * e[3] * e[5]

        val inv14 = -e[0] * e[5] * e[14] + e[0] * e[6] * e[13] + e[4] * e[1] * e[14] - e[4] * e[2] * e[13] - e[12] * e[1] * e[6] + e[12] * e[2] * e[5]

        val inv3 = -e[1] * e[6] * e[11] + e[1] * e[7] * e[10] + e[5] * e[2] * e[11] - e[5] * e[3] * e[10] - e[9] * e[2] * e[7] + e[9] * e[3] * e[6]

        val inv7 = e[0] * e[6] * e[11] - e[0] * e[7] * e[10] - e[4] * e[2] * e[11] + e[4] * e[3] * e[10] + e[8] * e[2] * e[7] - e[8] * e[3] * e[6]

        val inv11 = -e[0] * e[5] * e[11] + e[0] * e[7] * e[9] + e[4] * e[1] * e[11] - e[4] * e[3] * e[9] - e[8] * e[1] * e[7] + e[8] * e[3] * e[5]

        val inv15 = e[0] * e[5] * e[10] - e[0] * e[6] * e[9] - e[4] * e[1] * e[10] + e[4] * e[2] * e[9] + e[8] * e[1] * e[6] - e[8] * e[2] * e[5]

        val det = 1.0f / (e[0] * inv0 + e[1] * inv4 + e[2] * inv8 + e[3] * inv12)

        e[0] = inv0 * det
        e[1] = inv1 * det
        e[2] = inv2 * det
        e[3] = inv3 * det
        e[4] = inv4 * det
        e[5] = inv5 * det
        e[6] = inv6 * det
        e[7] = inv7 * det
        e[8] = inv8 * det
        e[9] = inv9 * det
        e[10] = inv10 * det
        e[11] = inv11 * det
        e[12] = inv12 * det
        e[13] = inv13 * det
        e[14] = inv14 * det
        e[15] = inv15 * det
    }

    fun setScale(sx : Float, sy : Float, sz : Float) {
        loadIdentity()
        e[0] = sx
        e[5] = sy
        e[10] = sz
    }

    fun setScale(s : vector3f) {
        loadIdentity()
        e[0] = s.x
        e[5] = s.y
        e[10] = s.z
    }

    fun setScale(s : FloatArray) {
        loadIdentity()
        e[0] = s[0]
        e[5] = s[1]
        e[10] = s[2]
    }

    fun setScalePart(sx : Float, sy : Float, sz : Float) {
        e[0] = sx
        e[5] = sy
        e[10] = sz
    }

    fun setScalePart(s : vector3f) {
        e[0] = s.x
        e[5] = s.y
        e[10] = s.z
    }

    fun setScalePart(s : FloatArray) {
        e[0] = s[0]
        e[5] = s[1]
        e[10] = s[2]
    }

    fun setTranslation(tx : Float, ty : Float, tz : Float) {
        loadIdentity()
        e[12] = tx
        e[13] = ty
        e[14] = tz
    }

    fun setTranslation(t : vector3f) {
        loadIdentity()
        e[12] = t.x
        e[13] = t.y
        e[14] = t.z
    }

    fun setTranslation(t : FloatArray) {
        loadIdentity()
        e[12] = t[0]
        e[13] = t[1]
        e[14] = t[2]
    }

    fun setTranslationPart(tx : Float, ty : Float, tz : Float) {
        e[12] = tx
        e[13] = ty
        e[14] = tz
    }

    fun setTranslationPart(t : vector3f) {
        e[12] = t.x
        e[13] = t.y
        e[14] = t.z
    }

    fun setTranslationPart(t : FloatArray) {
        e[12] = t[0]
        e[13] = t[1]
        e[14] = t[2]
    }

    fun setAxisRotation(axis : vector3f, angle : Float) {
        loadIdentity()
        setAxisRotationPart(axis, angle)
    }

    fun setAxisRotationPart(axis : vector3f, angle : Float) {
        var u = vector3f(axis)
        u.normalize()

        val sinAngle = sin(angle)
        val cosAngle = cos(angle)
        val oneMinusCosAngle = 1.0f - cosAngle

        e[0] = u.x * u.x + cosAngle * (1 - u.x * u.x)
        e[4] = oneMinusCosAngle * u.x * u.y - sinAngle * u.z
        e[8] = oneMinusCosAngle * u.x * u.z + sinAngle * u.y

        e[1] = oneMinusCosAngle * u.x * u.y + sinAngle * u.z
        e[5] = u.y * u.y + cosAngle * (1 - u.y * u.y)
        e[9] = oneMinusCosAngle * u.y * u.z - sinAngle * u.x

        e[2] = oneMinusCosAngle * u.x * u.z - sinAngle * u.y
        e[6] = oneMinusCosAngle * u.y * u.z + sinAngle * u.x
        e[10] = u.z * u.z + cosAngle * (1 - u.z * u.z)
    }

    fun setRotationX(angle : Float) {
        loadIdentity()

        e[5] = cos(angle)
        e[6] = sin(angle)

        e[9] = -e[6]
        e[10] = e[5]
    }

    fun setRotationY(angle : Float) {
        loadIdentity()

        e[0] = cos(angle)
        e[2] = -sin(angle)

        e[8] = -e[2]
        e[10] = e[0]
    }

    fun setRotationZ(angle : Float) {
        loadIdentity()

        e[0] = cos(angle)
        e[1] = sin(angle)

        e[4] = -e[1]
        e[5] = e[0]
    }

    fun setEulerRotation(xangle : Float, yangle : Float, zangle : Float) {
        loadIdentity()
        setEulerRotationPart(xangle, yangle, zangle)
    }

    fun setEulerRotationPart(xangle : Float, yangle : Float, zangle : Float) {

        val cr = cos(xangle)
        val sr = sin(xangle)
        val cp = cos(yangle)
        val sp = sin(yangle)
        val cy = cos(zangle)
        val sy = sin(zangle)
        val srsp = sr * sp;
        val crsp = cr * sp;

        e[0] = cp * cy
        e[1] = cp * sy
        e[2] = -sp
        e[4] = srsp * cy - cr * sy
        e[5] = srsp * sy + cr * cy
        e[6] = sr * cp
        e[8] = crsp * cy + sr * sy
        e[9] = crsp * sy - sr * cy
        e[10] = cr * cp
    }

    fun setFrustumProjection(left : Float, right : Float, bottom : Float, top : Float, znear : Float, zfar : Float) {
        loadZero()
        e[0] = 2.0f * znear / (right - left)
        e[5] = 2.0f * znear / (top - bottom)
        e[8] = (right + left) / (right - left)
        e[9] = (top + bottom) / (top - bottom)
        e[10] = (znear + zfar) / (znear - zfar)
        e[11] = -1.0f
        e[14] = 2.0f * znear * zfar / (znear - zfar)
    }

    fun setPerspectiveProjectionV(fovy : Float, aspect : Float, znear : Float, zfar : Float) {
        val f = 1.0f / tan(fovy / 2.0f)
        loadZero()
        e[0] = f / aspect
        e[5] = f
        e[10] = (znear + zfar) / (znear - zfar)
        e[11] = -1.0f
        e[14] = 2.0f * znear * zfar / (znear - zfar)
    }

    fun setPerspectiveProjectionH(fovx : Float, aspect : Float, znear : Float, zfar : Float) {
        val f = 1.0f / tan(fovx)
        loadZero()
        e[0] = f
        e[5] = f * aspect
        e[10] = (znear + zfar) / (znear - zfar)
        e[11] = -1.0f
        e[14] = 2.0f * znear * zfar / (znear - zfar)
    }

    fun setOrthoProjection(left : Float, right : Float, bottom : Float, top : Float, znear : Float, zfar : Float) {
        loadZero()
        e[0] = 2.0f / (right - left)
        e[5] = 2.0f / (top - bottom)
        e[10] = 2.0f / (znear - zfar)
        e[12] = (left + right) / (left - right)
        e[13] = (bottom + top) / (bottom - top)
        e[14] = (znear + zfar) / (znear - zfar)
        e[15] = 1.0f
    }

    fun setLookAt(eye : vector3f, target : vector3f, upVector : vector3f) {

        val dir = target - eye
        dir.normalize()

        val right = dir.cross(upVector)
        right.normalize()

        val up = right.cross(dir)
        up.normalize()

        val rot = matrix4f(
                right.x, up.x, -dir.x, 0.0f,
                right.y, up.y, -dir.y, 0.0f,
                right.z, up.z, -dir.z, 0.0f,
                0.0f, 0.0f, 0.0f, 1.0f)

        val trans = matrix4f()
        trans.setTranslation(-eye)

        val v = rot * trans

        e[0] = v.e[0]
        e[1] = v.e[1]
        e[2] = v.e[2]
        e[3] = v.e[3]
        e[4] = v.e[4]
        e[5] = v.e[5]
        e[6] = v.e[6]
        e[7] = v.e[7]
        e[8] = v.e[8]
        e[9] = v.e[9]
        e[10] = v.e[10]
        e[11] = v.e[11]
        e[12] = v.e[12]
        e[13] = v.e[13]
        e[14] = v.e[14]
        e[15] = v.e[15]
    }

    fun toFloatArray() = e.copyOf()

}