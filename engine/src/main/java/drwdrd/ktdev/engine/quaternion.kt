package drwdrd.ktdev.engine

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class quaternion(_x : Float, _y : Float, _z : Float, _w : Float) {

    constructor() : this(0.0f, 0.0f, 0.0f, 0.0f)
    constructor(v : FloatArray) : this(v[0], v[1], v[2], v[3])
    constructor(v : FloatArray, offset : Int) : this(v[offset], v[offset + 1], v[offset + 2], v[offset + 3])
    constructor(s : Float , u : vector3f) : this(u[0], u[1], u[2], s)

    val x get() = e[0]
    val y get() = e[1]
    val z get() = e[2]
    val w get() = e[3]

    var e = floatArrayOf(_x, _y, _z, _w)

    operator fun get(index : Int) : Float {
        require(index in 0 .. 3)
        return e[index]
    }

    operator fun set(index : Int, value : Float) {
        require(index in 0 .. 3)
        e[index] = value
    }


    fun toFloatArray() = e

    operator fun plus(q: quaternion) = quaternion(e[0] + q.e[0], e[1] + q.e[1], e[2] + q.e[2], e[3] + q.e[3])
    operator fun minus(q: quaternion) = quaternion(e[0] - q.e[0], e[1] - q.e[1], e[2] - q.e[2], e[3] - q.e[3])

    operator fun times(q: quaternion) = quaternion(
        e[0] * q.e[3] + e[3] * q.e[0] + e[2] * q.e[1] - e[1] * q.e[2],
        e[1] * q.e[3] + e[3] * q.e[1] + e[0] * q.e[2] - e[2] * q.e[0],
        e[2] * q.e[3] + e[3] * q.e[2] + e[1] * q.e[0] - e[0] * q.e[1],
        e[3] * q.e[3] - e[0] * q.e[0] - e[1] * q.e[1] - e[2] * q.e[2]
    )

    operator fun times(s: Float) = quaternion(e[0] * s, e[1] * s, e[2] * s, e[3] * s)

    operator fun plusAssign(q: quaternion) {
        e[0] += q.e[0]
        e[1] += q.e[1]
        e[2] += q.e[2]
        e[3] += q.e[3]
    }

    operator fun minusAssign(q: quaternion) {
        e[0] -= q.e[0]
        e[1] -= q.e[1]
        e[2] -= q.e[2]
        e[3] -= q.e[3]
    }

    operator fun timesAssign(q: quaternion) {
        val ne0 = e[0] * q.e[3] + e[3] * q.e[0] + e[2] * q.e[1] - e[1] * q.e[2]
        val ne1 = e[1] * q.e[3] + e[3] * q.e[1] + e[0] * q.e[2] - e[2] * q.e[0]
        val ne2 = e[2] * q.e[3] + e[3] * q.e[2] + e[1] * q.e[0] - e[0] * q.e[1]
        val ne3 = e[3] * q.e[3] - e[0] * q.e[0] - e[1] * q.e[1] - e[2] * q.e[2]

        e[0] = ne0
        e[1] = ne1
        e[2] = ne2
        e[3] = ne3
    }

    operator fun timesAssign(a: Float) {
        e[0] *= a
        e[1] *= a
        e[2] *= a
        e[3] *= a
    }

    fun inverse() {
        e[0] = -e[0]
        e[1] = -e[1]
        e[2] = -e[2]
    }

    fun inversed() = quaternion(-e[0], -e[1], -e[2], e[3])

    fun normalize() {
        val d = 1.0f / sqrt(e[0] * e[0] + e[1] * e[1] + e[2] * e[2] + e[3] * e[3])
        e[0] *= d
        e[1] *= d
        e[2] *= d
        e[3] *= d
    }

    fun normalized(): quaternion {
        val d = 1.0f / sqrt(e[0] * e[0] + e[1] * e[1] + e[2] * e[2] + e[3] * e[3])
        return this * d
    }

    fun dot(q: quaternion) = (e[0] * q.e[0] + e[1] * q.e[1] + e[2] * q.e[2] + e[3] * q.e[3])

    fun setEulerRotation(xangle: Float, yangle: Float, zangle: Float) {

        val sr = sin(0.5f * xangle)
        val cr = cos(0.5f * xangle)

        val sp = sin(0.5f * yangle)
        val cp = cos(0.5f * yangle)

        val sy = sin(0.5f * zangle)
        val cy = cos(0.5f * zangle)

        val cpcy = cp * cy
        val spcy = sp * cy
        val cpsy = cp * sy
        val spsy = sp * sy

        e[0] = sr * cpcy - cr * spsy
        e[1] = cr * spcy + sr * cpsy
        e[2] = cr * cpsy - sr * spcy
        e[3] = cr * cpcy + sr * spsy
        normalize()
    }

    fun setAxisRotation(axis: vector3f, angle: Float) {
        val sinAngle = sin(0.5f * angle)
        e[3] = cos(0.5f * angle)
        e[0] = sinAngle * axis.x
        e[1] = sinAngle * axis.y
        e[2] = sinAngle * axis.z

    }

    //dir and normal are normalized
    fun setAxisRotation(dir : vector3f, normal: vector3f) {
/*
        dir.normalize()
        normal.normalize()*/

        val half = normal + dir
        half.normalize()

        var u = vector3f.cross(dir, half)

        //angle
        e[3] = vector3f.dot(dir, half)
        e[0] = u.x
        e[1] = u.y
        e[2] = u.z
    }

    fun getRotationMatrix() : matrix4f {
        val m = matrix4f()

        m[0] = 1.0f - 2.0f * e[1] * e[1] - 2.0f * e[2] * e[2]
        m[1] = 2.0f * e[0] * e[1] + 2.0f * e[2] * e[3]
        m[2] = 2.0f * e[0] * e[2] - 2.0f * e[1] * e[3]
        m[3] = 0.0f

        m[4] = 2.0f * e[0] * e[1] - 2.0f * e[2] * e[3]
        m[5] = 1.0f - 2.0f * e[0] * e[0] - 2.0f * e[2] * e[2]
        m[6] = 2.0f * e[2] * e[1] + 2.0f * e[0] * e[3]
        m[7] = 0.0f

        m[8] = 2.0f * e[0] * e[2] + 2.0f * e[1] * e[3]
        m[9] = 2.0f * e[2] * e[1] - 2.0f * e[0] * e[3]
        m[10] =1.0f - 2.0f * e[0] * e[0] - 2.0f * e[1] * e[1]
        m[11] = 0.0f

        m[12] = 0.0f
        m[13] = 0.0f
        m[14] = 0.0f
        m[15] = 1.0f

        return m
    }

    fun getMatrix(center : vector3f, scale : Float) : matrix4f {

        val m = matrix4f()

        m[0] = scale * (1.0f - 2.0f * e[1] * e[1] - 2.0f * e[2] * e[2])
        m[1] = scale * (2.0f * e[0] * e[1] + 2.0f * e[2] * e[3])
        m[2] = scale * (2.0f * e[0] * e[2] - 2.0f * e[1] * e[3])
        m[3] = 0.0f

        m[4] = scale * (2.0f * e[0] * e[1] - 2.0f * e[2] * e[3])
        m[5] = scale * (1.0f - 2.0f * e[0] * e[0] - 2.0f * e[2] * e[2])
        m[6] = scale * (2.0f * e[2] * e[1] + 2.0f * e[0] * e[3])
        m[7] = 0.0f

        m[8] = scale * (2.0f * e[0] * e[2] + 2.0f * e[1] * e[3])
        m[9] = scale * (2.0f * e[2] * e[1] - 2.0f * e[0] * e[3])
        m[10] = scale * (1.0f - 2.0f * e[0] * e[0] - 2.0f * e[1] * e[1])
        m[11] = 0.0f

        m[12] = center.x
        m[13] = center.y
        m[14] = center.z
        m[15] = 1.0f

        return m
    }
}

operator fun Float.times(q : quaternion) = q.times(this)
