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
        velocity[0] += 0.05f * ax * deltaTime
        velocity[1] += 0.05f * ay * deltaTime

        age += deltaTime
    }

    companion object {
        fun createRandom(z : Float) : StarParticle {
            var pos = vector3f(RandomGenerator.randf(-2.5f, 2.5f), RandomGenerator.randf(-5.0f, 5.0f), z)
            var vel = vector3f(0.0f, 0.0f, -1.0f)
            var rot = RandomGenerator.randf(-0.1f, 0.1f)
            val s = RandomGenerator.randf(0.2f, 1.5f)
            var uvpos = RandomGenerator.rand2f(0.0f, 1.0f)
            var roi = Rectangle(uvpos.x - 0.2f * s, uvpos.y - 0.2f * s, uvpos.x + 0.2f * s, uvpos.y + 0.2f * s)
            return StarParticle(pos, vel, vector3f(0.0f, 0.0f, rot), vector3f(s, s, 0.0f), roi, RandomGenerator.randf(0.0f, 10.0f))
        }

        fun createRandom() : StarParticle {
            var pos = vector3f(RandomGenerator.randf(-2.5f, 2.5f), RandomGenerator.randf(-5.0f, 5.0f), 10.0f)
            var vel = vector3f(0.0f, 0.0f,-1.0f)
            var rot = RandomGenerator.randf(-0.1f, 0.1f)
            val s = RandomGenerator.randf(0.2f, 1.5f)
            var uvpos = RandomGenerator.rand2f(0.0f, 1.0f)
            var roi = Rectangle(uvpos.x - 0.2f * s, uvpos.y - 0.2f * s, uvpos.x + 0.2f * s, uvpos.y + 0.2f * s)
            return StarParticle(pos, vel, vector3f(0.0f, 0.0f, rot), vector3f(s, s, 0.0f), roi, 0.0f)
        }
    }

}