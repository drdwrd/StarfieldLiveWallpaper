package drwdrd.ktdev.starfield

import drwdrd.ktdev.engine.*
import kotlin.math.sqrt

class Particle(_position : vector3f, _velocity : vector3f, _rotation : vector3f, _scale : vector3f, _uvRoI : Rectangle, _age : Float) {

    val position = _position
    val velocity = _velocity
    val rotation = _rotation
    val scale = _scale
    val uvRoI = _uvRoI

    public var repulsiveForce = 0.0f
    public var color = vector4f(0.0f, 0.0f, 0.0f, 0.0f)

    var age = _age
        private set

    val modelMatrix : matrix4f
        get() {
            var translationMatrix = matrix4f()
            translationMatrix.setTranslation(position)

            var scaleMatrix = matrix4f()
            scaleMatrix.setScale(scale)

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

    fun calculateBillboardModelMatrix(dir : vector3f, normal : vector3f) : matrix4f {


        var rotationMatrix2 = matrix4f()
        rotationMatrix2.setAxisRotation(dir, normal)

        var translationMatrix = matrix4f()
        translationMatrix.setTranslation(position)

        var scaleMatrix = matrix4f()
        scaleMatrix.setScale(scale)

        var rotationMatrix = matrix4f()
        rotationMatrix.setEulerRotation(age * rotation.x, age * rotation.y, age * rotation.z)

        return translationMatrix * rotationMatrix2 * rotationMatrix * scaleMatrix

    }
//TODO : fix hardcoded stuff...
    fun tick(deltaTime : Float) {

        val cg = vector3f(0.0f, 0.0f, -1.0f)

        val r = (cg - position).length()

        val ax = -(cg.x - position.x) / (r * r + 0.1f)
        val ay = -(cg.y - position.y) / (r * r + 0.1f)
        val az = -(cg.z - position.z) / (r * r + 0.1f)


        position += velocity * deltaTime
        velocity[0] += repulsiveForce * ax * deltaTime
        velocity[1] += repulsiveForce * ay * deltaTime
        velocity[2] += repulsiveForce * az * deltaTime

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
            val p = Particle(pos, vel, vector3f(0.0f, 0.0f, rot), vector3f(s, s, s), roi, 0.0f)
            p.repulsiveForce = 0.0f
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
            val p = Particle(pos, vel, vector3f(0.0f, 0.0f, rot), vector3f(s, s, s), roi, 0.0f)
            p.repulsiveForce = 0.0f
            p.color = 0.15f * vector4f(RandomColor.randomColor())
            return p
        }
    }
}