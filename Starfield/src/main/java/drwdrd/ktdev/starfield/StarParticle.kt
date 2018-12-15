package drwdrd.ktdev.starfield

import drwdrd.ktdev.engine.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class StarParticle(_position : vector3f, _velocity : vector3f, _rotation : vector3f, _scale : vector3f, _uvRoI : Rectangle, _age : Float) {

    val position = _position
    val velocity = _velocity
    val rotation = _rotation
    val scale = _scale
    val uvRoI = _uvRoI

    public var repulsiveForce = 0.0f

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

    fun tick(deltaTime : Float) {

        var r = sqrt(position.length())

        var ax = position.x / (r + 0.1f)
        var ay = position.y / (r + 0.1f)

        position += velocity * deltaTime
        velocity[0] += repulsiveForce * ax * deltaTime
        velocity[1] += repulsiveForce * ay * deltaTime

        age += deltaTime
    }

    companion object {
        fun createRandom(z : Float) : StarParticle {
            var pos = vector3f(RandomGenerator.randf(-1.5f, 1.5f), RandomGenerator.randf(-3.0f, 3.0f), z)
            var vel = vector3f(0.0f, 0.0f, -1.0f)
            val s = RandomGenerator.randf(0.2f, 1.0f)
            var rot = RandomGenerator.randf(-0.1f, 0.1f)
/*            var i = RandomGenerator.rand(2) * 0.5f
            var j = RandomGenerator.rand(2) * 0.5f
            var roi = Rectangle(i , j, i + 0.5f, j + 0.5f)*/
//            var roi = Rectangle(0.0f, 0.0f, 1.0f, 1.0f)
            var uvpos = RandomGenerator.rand2f(0.0f, 1.0f)
            var uvSize = 0.5f * s
            var roi = Rectangle(uvpos.x - uvSize, uvpos.y - uvSize, uvpos.x + uvSize, uvpos.y + uvSize)
            val p = StarParticle(pos, vel, vector3f(0.0f, 0.0f, rot), vector3f(s, s, 0.0f), roi, 0.0f)
            p.repulsiveForce = 0.15f
            return p
        }

        fun createRandom2(z : Float) : StarParticle {
            var pos = vector3f(RandomGenerator.randf(-1.5f, 1.5f), RandomGenerator.randf(-3.0f, 3.0f), z)
            var vel = vector3f(0.0f, 0.0f, -1.0f)
            val s = RandomGenerator.randf(0.15f, 0.35f)
            var rot = RandomGenerator.randf(-0.1f, 0.1f)
/*            var i = RandomGenerator.rand(2) * 0.5f
            var j = RandomGenerator.rand(2) * 0.5f
            var roi = Rectangle(i , j, i + 0.5f, j + 0.5f)*/
            var roi = Rectangle(0.0f, 0.0f, 1.0f, 1.0f)
            val p = StarParticle(pos, vel, vector3f(0.0f, 0.0f, rot), vector3f(s, s, 0.0f), roi, 0.0f)
            p.repulsiveForce = 0.25f
            return p
        }
    }

}