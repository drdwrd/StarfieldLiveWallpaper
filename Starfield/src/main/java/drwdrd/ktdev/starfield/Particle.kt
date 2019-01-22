package drwdrd.ktdev.starfield

import drwdrd.ktdev.engine.*
import kotlin.math.cos
import kotlin.math.sqrt

class Particle(_position : vector3f, _velocity : vector3f, _rotation : vector3f, _scale : Float, _uvRoI : Rectangle, _age : Float) {

    var position = _position
    var velocity = _velocity
    var rotation = _rotation
    var scale = _scale
    var uvRoI = _uvRoI

    public var repulsiveForce = 0.0f
    public var color = vector4f(0.0f, 0.0f, 0.0f, 0.0f)

    var age = _age
        private set

    val modelMatrix : matrix4f
        get() {
            var translationMatrix = matrix4f()
            translationMatrix.setTranslation(position)

            var scaleMatrix = matrix4f()
            scaleMatrix.setScale(vector3f(scale, scale, scale))

            var rotationMatrix = matrix4f()
            rotationMatrix.setEulerRotation(age * rotation.x, age * rotation.y, age * rotation.z)

            return translationMatrix * rotationMatrix * scaleMatrix
        }

    val normalMatrix : matrix4f
        get() {
            var rotationMatrix = matrix4f()
            rotationMatrix.setEulerRotation(age * rotation.x, age * rotation.y, age * rotation.z)
            return rotationMatrix
        }

    //TODO: optimized usage down to 15%
    fun calculateBillboardModelMatrix(dir : vector3f, normal : vector3f) : matrix4f {
        var rotationMatrix2 = matrix4f()
        rotationMatrix2.setAxisRotation(dir, normal)

        var translationMatrix = matrix4f()
        translationMatrix.setTranslation(position)

        var scaleMatrix = matrix4f()
        scaleMatrix.setScale(vector3f(scale, scale, scale))

        var rotationMatrix = matrix4f()
        rotationMatrix.setEulerRotation(age * rotation.x, age * rotation.y, age * rotation.z)

        return translationMatrix * rotationMatrix2 * rotationMatrix * scaleMatrix

    }

/*
    //TODO: faster but needs fixing, down to 10% with this one
    fun calculateBillboardModelMatrix(dir : vector3f, normal : vector3f) : matrix4f {
        val rotationQuaternion2 = quaternion()
        rotationQuaternion2.setAxisRotation(dir, normal)

        val rotationQuaternion = quaternion()
        rotationQuaternion.setEulerRotation(age * rotation.x, age * rotation.y, age * rotation.z)

        val q = rotationQuaternion2 * rotationQuaternion

        return q.getMatrix(position, scale)
    }
*/
    fun boundingSphere() = BoundingSphere(position, scale * sqrt(2.0f))

    //TODO : most time spent here right now ~33% atm
    fun tick(eye : Eye, deltaTime: Float) {
        val cosAlpha = vector3f.dot(position.normalized(), eye.forward)

        val q = eye.position + eye.forward * position.length() * cosAlpha - position

        val r = vector3f.dot(q, q)

        val a = -q / (r + 0.1f)

        position.plusAssign(velocity * deltaTime)
        velocity = eye.forward +  repulsiveForce * a * deltaTime

        age += deltaTime
    }

    companion object {

        fun createStar(spawningPoint : vector3f, targetPoint : vector3f) : Particle {
            val rp = vector3f(RandomGenerator.randf(-1.5f, 1.5f), RandomGenerator.randf(-3.0f, 3.0f), 0.0f)
            val pos = spawningPoint + rp
            val t = targetPoint + rp
            //val vel = vector3f(0.0f, 0.0f, -1.0f)
            val vel = (t - pos).normalized()
            val s = RandomGenerator.randf(0.01f, 0.15f)
            val rot = RandomGenerator.randf(-1.5f, 1.5f)
            val i = RandomGenerator.rand(2) * 0.5f
            val j = RandomGenerator.rand(2) * 0.5f
            val roi = Rectangle(i , j, i + 0.5f, j + 0.5f)
            val p = Particle(pos, vel, vector3f(0.0f, 0.0f, rot), s, roi, 0.0f)
            p.repulsiveForce = 0.02f
            return p
        }

        fun createCloud(spawningPoint: vector3f, targetPoint: vector3f) : Particle {
            val rp = vector3f(RandomGenerator.randf(-1.5f, 1.5f), RandomGenerator.randf(-3.0f, 3.0f), 0.0f)
            val pos = spawningPoint + rp
            val t = targetPoint + rp
            //val vel = vector3f(0.0f, 0.0f, -1.0f)
            val vel = (t - pos).normalized()
            val s = RandomGenerator.randf(0.5f, 1.5f)
            val rot = RandomGenerator.randf(-0.5f, 0.5f)
//            val i = RandomGenerator.rand(2) * 0.5f
//            val j = RandomGenerator.rand(2) * 0.5f
//            val roi = Rectangle(i , j, i + 0.5f, j + 0.5f)
            val roi = Rectangle(0.0f, 0.0f, 1.0f, 1.0f)
            val p = Particle(pos, vel, vector3f(0.0f, 0.0f, rot), s, roi, 0.0f)
            p.repulsiveForce = 0.02f
            p.color = 0.15f * vector4f(RandomColor.randomColor())
            return p
        }
    }
}