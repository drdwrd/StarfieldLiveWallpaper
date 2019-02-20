package drwdrd.ktdev.engine


import java.util.Random

object RandomGenerator {

    private val m_Random = Random(0)

    fun createSeed(): Long {
        val seed = System.currentTimeMillis()
        m_Random.setSeed(seed)
        return seed
    }

    fun seed(seed: Long) {
        m_Random.setSeed(seed)
    }

    fun randf(): Float {
        return m_Random.nextFloat()
    }

    fun randf(min: Float, max: Float): Float {
        return m_Random.nextFloat() * (max - min) + min
    }

    fun rand2f() = vector2f(randf(), randf())

    fun rand2f(min : Float, max : Float) = vector2f(randf(min, max), randf(min, max))

    fun rand3f() = vector3f(randf(), randf(), randf())

    fun rand3f(min : Float, max : Float) = vector3f(randf(min, max), randf(min, max), randf(min, max))

    fun rand4f() = vector4f(randf(), randf(), randf(), randf())

    fun rand4f(min : Float, max : Float) = vector4f(randf(min, max), randf(min, max), randf(min, max), randf(min, max))

    fun rand(): Int {
        return m_Random.nextInt()
    }

    fun rand(max: Int): Int {
        return m_Random.nextInt(max)
    }
}
