package drwdrd.ktdev.starfield

import android.content.Context
import drwdrd.ktdev.engine.Log
import java.io.File
import java.io.FileNotFoundException
import java.lang.NumberFormatException

object SettingsProvider {

    private const val DEFAULT_PARTICLE_SPEED = 0.2f
    private const val DEFAULT_PARTICLE_SPAWN_TIME_MULTIPLIER = 0.1
    private const val DEFAULT_PARALLAX_EFFECT_MULTIPLIER = 0.2f
    private const val DEFAULT_ENABLE_PARALLAX_EFFECT = false
    private const val DEFAULT_TEXTURE_QUALITY_LEVEL = 0
    private const val DEFAULT_PRECESSION_SPEED = 0.0001f
    const val TEXTURE_QUALITY_UNKNOWN = 100

    enum class ParallaxEffectEngineType(val type : Int) {
        None(0),
        Gravity(1),
        Gyro(2),
        Unknown(3);

        companion object {
            fun fromInt(type: Int): ParallaxEffectEngineType {
                return when(type) {
                    0 -> None
                    1 -> Gravity
                    2 -> Gyro
                    else -> Unknown
                }
            }
        }
    }

    var parallaxEffectEngineType = ParallaxEffectEngineType.Unknown

    var particleSpeed = DEFAULT_PARTICLE_SPEED                     // (0.1, 10.0) ????

    var particlesSpawnTimeMultiplier = DEFAULT_PARTICLE_SPAWN_TIME_MULTIPLIER       //in ms time delay between star particles spawn

    var parallaxEffectMultiplier = DEFAULT_PARALLAX_EFFECT_MULTIPLIER

    var enableParallaxEffect = DEFAULT_ENABLE_PARALLAX_EFFECT

    var baseTextureQualityLevel = TEXTURE_QUALITY_UNKNOWN

    var textureQualityLevel = DEFAULT_TEXTURE_QUALITY_LEVEL             // 0 - high quality, 1 - low quality

    var precessionSpeed = DEFAULT_PRECESSION_SPEED

    fun resetSettings() {
        parallaxEffectEngineType = ParallaxEffectEngineType.Unknown
        particleSpeed = DEFAULT_PARTICLE_SPEED
        particlesSpawnTimeMultiplier = DEFAULT_PARTICLE_SPAWN_TIME_MULTIPLIER
        parallaxEffectMultiplier = DEFAULT_PARALLAX_EFFECT_MULTIPLIER
        enableParallaxEffect = DEFAULT_ENABLE_PARALLAX_EFFECT
        textureQualityLevel = DEFAULT_TEXTURE_QUALITY_LEVEL
        precessionSpeed = DEFAULT_PRECESSION_SPEED
    }

    fun save(context : Context, filename : String) {
        File(context.filesDir, filename).bufferedWriter().use {
            it.write("parallaxEffectEngineType=${parallaxEffectEngineType.type}\n")
            it.write("particleSpeed=$particleSpeed\n")
            it.write("particlesSpawnTimeMultiplier=$particlesSpawnTimeMultiplier\n")
            it.write("parallaxEffectMultiplier=$parallaxEffectMultiplier\n")
            it.write("enableParallaxEffect=$enableParallaxEffect\n")
            it.write("baseTextureQualityLevel=$baseTextureQualityLevel\n")
            it.write("textureQualityLevel=$textureQualityLevel\n")
            it.write("precessionSpeed=$precessionSpeed\n")
        }
    }

    fun load(context: Context, filename : String) {
        try {
            File(context.filesDir, filename).bufferedReader().useLines {
                lines -> lines.forEach {
                    val s = it.split("=")
                    if(s.size >= 2) {
                        when (s[0]) {
                            "parallaxEffectEngineType" -> parallaxEffectEngineType = ParallaxEffectEngineType.fromInt(s[1].toInt())
                            "particleSpeed" -> particleSpeed = s[1].toFloat()
                            "particlesSpawnTimeMultiplier" -> particlesSpawnTimeMultiplier = s[1].toDouble()
                            "parallaxEffectMultiplier" -> parallaxEffectMultiplier = s[1].toFloat()
                            "enableParallaxEffect" -> enableParallaxEffect = s[1].toBoolean()
                            "baseTextureQualityLevel" -> baseTextureQualityLevel = s[1].toInt()
                            "textureQualityLevel" -> textureQualityLevel = s[1].toInt()
                            "precessionSpeed" -> precessionSpeed = s[1].toFloat()
                        }
                    }
                }
            }
        } catch(e : FileNotFoundException) {
            Log.debug("No settings file found!\n")
        } catch(e : NumberFormatException) {
            Log.debug("Cannot parse settings!\n")
        }
    }
}