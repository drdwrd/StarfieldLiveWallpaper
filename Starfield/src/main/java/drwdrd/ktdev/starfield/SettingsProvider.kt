package drwdrd.ktdev.starfield

import android.content.Context
import drwdrd.ktdev.engine.Log
import java.io.File
import java.io.FileNotFoundException
import java.lang.NumberFormatException

object SettingsProvider {

    const val DEFAULT_TIMESCALE = 1.0
    const val DEFAULT_STAR_PARTICLE_SPAWN_TIME = 0.02
    const val DEFAULT_CLOUD_PARTICLE_SPAWN_TIME = 0.2
    const val DEFAULT_PARALLAX_EFFECT_MULTIPLIER = 1.0f
    const val DEFAULT_ENABLE_PARALLAX_EFFECT = false
    const val DEFAULT_TEXTURE_QUALITY_LEVEL = 0
    const val DEFAULT_PRECESSION_SPEED = 0.0001f

    var timeScale = DEFAULT_TIMESCALE                     // (0.1, 10.0) ????

    var starParticlesSpawnTime = DEFAULT_STAR_PARTICLE_SPAWN_TIME       //in ms time delay between star particles spawn

    var cloudParticleSpawnTime = DEFAULT_CLOUD_PARTICLE_SPAWN_TIME        //in ms time delay between cloud particles spawn

    var parallaxEffectMultiplier = DEFAULT_PARALLAX_EFFECT_MULTIPLIER

    var enableParallaxEffect = DEFAULT_ENABLE_PARALLAX_EFFECT

    var textureQualityLevel = DEFAULT_TEXTURE_QUALITY_LEVEL             // 0 - high quality, 1 - low quality

    var precessionSpeed = DEFAULT_PRECESSION_SPEED

    fun resetSettings() {
        timeScale = DEFAULT_TIMESCALE
        starParticlesSpawnTime = DEFAULT_STAR_PARTICLE_SPAWN_TIME
        cloudParticleSpawnTime = DEFAULT_CLOUD_PARTICLE_SPAWN_TIME
        parallaxEffectMultiplier = DEFAULT_PARALLAX_EFFECT_MULTIPLIER
        enableParallaxEffect = DEFAULT_ENABLE_PARALLAX_EFFECT
        textureQualityLevel = DEFAULT_TEXTURE_QUALITY_LEVEL
        precessionSpeed = DEFAULT_PRECESSION_SPEED
    }

    fun save(context : Context, filename : String) {
        File(context.filesDir, filename).bufferedWriter().use {
            it.write("timeScale=$timeScale\n")
            it.write("starParticlesSpawnTime=$starParticlesSpawnTime\n")
            it.write("cloudParticleSpawnTime=$cloudParticleSpawnTime\n")
            it.write("parallaxEffectMultiplier=$parallaxEffectMultiplier\n")
            it.write("enableParallaxEffect=$enableParallaxEffect\n")
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
                            "timeScale" -> timeScale = s[1].toDouble()
                            "starParticlesSpawnTime" -> starParticlesSpawnTime = s[1].toDouble()
                            "cloudParticleSpawnTime" -> cloudParticleSpawnTime = s[1].toDouble()
                            "parallaxEffectMultiplier" -> parallaxEffectMultiplier = s[1].toFloat()
                            "enableParallaxEffect" -> enableParallaxEffect = s[1].toBoolean()
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