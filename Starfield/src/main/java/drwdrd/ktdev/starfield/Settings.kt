package drwdrd.ktdev.starfield

import android.content.Context
import drwdrd.ktdev.engine.Log
import java.io.File
import java.io.FileNotFoundException

object Settings {

    var timeScale = 1.0

    var starParticlesSpawnTime = 0.01

    var cloudParticleSpawnTime = 0.1

    var parallaxEffectMultiplier = 1.0f

    var enableParallaxEffect = false

    fun save(context : Context, filename : String) {
        File(context.filesDir, filename).bufferedWriter().use {
            it.write("timeScale=$timeScale\n")
            it.write("starParticlesSpawnTime=$starParticlesSpawnTime\n")
            it.write("cloudParticleSpawnTime=$cloudParticleSpawnTime\n")
            it.write("parallaxEffectMultiplier=$parallaxEffectMultiplier\n")
            it.write("enableParallaxEffect = $enableParallaxEffect\n")
        }
    }

    fun load(context: Context, filename : String) {
        try {
            File(context.filesDir, filename).bufferedReader().useLines {
                    lines -> lines.forEach {
                val s = it.split("=")
                when(s[0]) {
                    "timeScale" -> timeScale = s[1].toDouble()
                    "starParticlesSpawnTime" -> starParticlesSpawnTime = s[1].toDouble()
                    "cloudParticleSpawnTime" -> cloudParticleSpawnTime = s[1].toDouble()
                    "parallaxEffectMultiplier" -> parallaxEffectMultiplier = s[1].toFloat()
                    "enableParallaxEffect" -> enableParallaxEffect = s[1].toBoolean()
                }
            }
            }
        } catch(e : FileNotFoundException) {
            Log.debug("No settings file found!\n")
        }
    }

}