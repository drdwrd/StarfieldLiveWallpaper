package drwdrd.ktdev.starfield

import drwdrd.ktdev.engine.*
import kotlin.math.ln
import kotlin.math.sqrt

// http://www.tannerhelland.com/4435/convert-temperature-rgb-algorithm-code/
fun blackBodyToRGB(temp : Double) : vector4f {
/*
    Start with a temperature, in Kelvin, somewhere between 1000 and 40000.  (Other values may work,
    but I can't make any promises about the quality of the algorithm's estimates above 40000 K.)
    Note also that the temperature and color variables need to be declared as floating-point.*/

    require(temp in 1000.0 .. 40000.0)

    var t = temp / 100.0

    var red : Double
    var green : Double
    var blue : Double

    red = when(t <= 66.0) {
        true -> 255.0
        false -> clamp(329.698727446 * (Math.pow(t - 60.0, -0.1332047592)), 0.0, 255.0)
    }

    green = when(t <= 66.0) {
        true -> clamp(99.4708025861 * ln(t) - 161.1195681661, 0.0, 255.0)
        false -> clamp(288.1221695283 * Math.pow(t - 60.0, -0.0755148492), 0.0, 255.0)
    }

    blue = when(t >= 66) {
        true -> 255.0
        false -> when(t <= 19) {
            true -> 0.0
            false -> clamp(138.5177312231 * ln(t - 10.0) - 305.0447927307, 0.0, 255.0)
        }
    }

    red /= 255.0
    green /= 255.0
    blue /= 255.0

    return vector4f(red.toFloat(), green.toFloat(), blue.toFloat(), 0.0f)
}

class StarParticle(_position : vector3f, _velocity : vector3f, _rotation : vector3f, _scale : vector3f, _uvRoI : Rectangle, _age : Float) {

    val position = _position
    val velocity = _velocity
    val rotation = _rotation
    val scale = _scale
    val uvRoI = _uvRoI

    public var repulsiveForce = 0.0f
    public var color = vector4f(1.0f, 1.0f, 1.0f, 1.0f)

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
            val s = RandomGenerator.randf(0.02f, 0.2f)
            var rot = RandomGenerator.randf(-0.1f, 0.1f)
/*            var i = RandomGenerator.rand(2) * 0.5f
            var j = RandomGenerator.rand(2) * 0.5f
            var roi = Rectangle(i , j, i + 0.5f, j + 0.5f)*/
//            var roi = Rectangle(0.0f, 0.0f, 1.0f, 1.0f)
            var uvpos = RandomGenerator.rand2f(0.0f, 1.0f)
            var uvSize = 0.5f * s
            var roi = Rectangle(uvpos.x - uvSize, uvpos.y - uvSize, uvpos.x + uvSize, uvpos.y + uvSize)
            val p = StarParticle(pos, vel, vector3f(0.0f, 0.0f, rot), vector3f(s, s, 1.0f), roi, 0.0f)
            p.repulsiveForce = 0.05f
            return p
        }

        fun createRandom2(z : Float) : StarParticle {
            var pos = vector3f(RandomGenerator.randf(-1.5f, 1.5f), RandomGenerator.randf(-3.0f, 3.0f), z)
            var vel = vector3f(0.0f, 0.0f, -1.0f)
            val s = 2.0f * RandomGenerator.randf(0.25f, 0.5f)
            var rot = RandomGenerator.rand3f(-1.5f, 1.5f)
/*            var i = RandomGenerator.rand(2) * 0.5f
            var j = RandomGenerator.rand(2) * 0.5f
            var roi = Rectangle(i , j, i + 0.5f, j + 0.5f)*/
            var roi = Rectangle(0.0f, 0.0f, 1.0f, 1.0f)
            val p = StarParticle(pos, vel, rot, vector3f(s, s, s), roi, 0.0f)
            p.repulsiveForce = 0.25f
            return p
        }

        fun createRandom3(z : Float) : StarParticle {
            var pos = vector3f(RandomGenerator.randf(-1.5f, 1.5f), RandomGenerator.randf(-3.0f, 3.0f), z)
            var vel = vector3f(0.0f, 0.0f, -1.0f)
            val s = RandomGenerator.randf(0.01f, 0.15f)
            var rot = RandomGenerator.randf(-1.5f, 1.5f)
            var i = RandomGenerator.rand(2) * 0.5f
            var j = RandomGenerator.rand(2) * 0.5f
            var roi = Rectangle(i , j, i + 0.5f, j + 0.5f)
//            var roi = Rectangle(0.0f, 0.0f, 1.0f, 1.0f)
            val p = StarParticle(pos, vel, vector3f(0.0f, 0.0f, 0.0f * rot), vector3f(s, s, s), roi, 0.0f)
            p.repulsiveForce = 0.0f
            var temp = RandomGenerator.randf(1000.0f, 40000.0f)
            p.color = blackBodyToRGB(temp.toDouble())
            return p
        }
    }

}