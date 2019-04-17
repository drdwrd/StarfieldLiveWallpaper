package drwdrd.ktdev.starfield

import drwdrd.ktdev.engine.*
import kotlin.math.sqrt

class Particle(_position : vector3f, _velocity : vector3f, _rotation : vector3f, _scale : Float, _uvRoI : Rectangle, _age : Float) {

    var position = _position
    var velocity = _velocity
    var rotation = _rotation
    var scale = _scale
    var uvRoI = _uvRoI
    var color = vector4f(0.0f, 0.0f, 0.0f, 0.0f)

    var age = _age
        private set

    val modelMatrix : matrix4f
        get() {
            val translationMatrix = matrix4f()
            translationMatrix.setTranslation(position)

            val scaleMatrix = matrix4f()
            scaleMatrix.setScale(vector3f(scale, scale, scale))

            val rotationMatrix = matrix4f()
            rotationMatrix.setEulerRotation(age * rotation.x, age * rotation.y, age * rotation.z)

            return translationMatrix * rotationMatrix * scaleMatrix
        }

    val normalMatrix : matrix4f
        get() {
            val rotationMatrix = matrix4f()
            rotationMatrix.setEulerRotation(age * rotation.x, age * rotation.y, age * rotation.z)
            return rotationMatrix
        }

    fun calculateBillboardModelMatrix(baseScale : Float, dir : vector3f, normal : vector3f) : matrix4f {
        val rotationMatrix2 = matrix4f()
        rotationMatrix2.setAxisRotation(dir, normal)

        val translationMatrix = matrix4f()
        translationMatrix.setTranslation(position)

        val scaleMatrix = matrix4f()
        scaleMatrix.setScale(baseScale * vector3f(scale, scale, scale))

        val rotationMatrix = matrix4f()
        rotationMatrix.setEulerRotation(age * rotation.x, age * rotation.y, age * rotation.z)

        return translationMatrix * rotationMatrix2 * rotationMatrix * scaleMatrix

    }

/*
    //TODO: faster but needs fixing
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

    fun tick(eyeForward : vector3f, particelSpeed : Float, deltaTime: Float) {
        velocity = -particelSpeed * eyeForward
        position.plusAssign(velocity * deltaTime)
        age += deltaTime
    }

    companion object {

        fun createStar(spawningDir : vector3f, targetPoint : vector3f, distance : Float) : Particle {
            //perturb spawn direction by small vector then normalize and set distance
            val rp = vector3f.cross(spawningDir, RandomGenerator.rand3f(-1.0f, 1.0f)).normalized()
            rp *= RandomGenerator.randf(0.1f, 5.0f)
            val pos = distance * spawningDir + rp
            val t = targetPoint + rp
            val vel = (t - pos).normalized()
            val s = RandomGenerator.randf(0.01f, 0.15f)
            val rot = RandomGenerator.randf(-0.5f, 0.5f)
            val i = RandomGenerator.rand(2) * 0.5f
            val j = RandomGenerator.rand(2) * 0.5f
            val roi = Rectangle(i , j, i + 0.5f, j + 0.5f)
            return Particle(pos, vel, vector3f(0.0f, 0.0f, rot), s, roi, 0.0f)
        }

        fun createCloud(spawningDir : vector3f, targetPoint : vector3f, distance : Float) : Particle {
            val rp = vector3f.cross(spawningDir, RandomGenerator.rand3f(-1.0f, 1.0f)).normalized()
            rp *= RandomGenerator.randf(0.1f, 5.0f)
            val pos = distance * spawningDir + rp
            val t = targetPoint + rp
            val vel = (t - pos).normalized()
            val s = RandomGenerator.randf(0.5f, 1.5f)
            val rot = RandomGenerator.randf(-0.1f, 0.1f)
            val roi = Rectangle(0.0f, 0.0f, 1.0f, 1.0f)
            val p = Particle(pos, vel, vector3f(0.0f, 0.0f, rot), s, roi, 0.0f)
            p.color = 0.15f * vector4f(RandomColor.randomColor())
            return p
        }
    }
}