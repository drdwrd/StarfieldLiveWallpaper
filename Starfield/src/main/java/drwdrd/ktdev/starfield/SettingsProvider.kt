package drwdrd.ktdev.starfield

import android.content.Context
import drwdrd.ktdev.engine.Log
import java.io.File
import java.io.FileNotFoundException

object SettingsProvider {

    var timeScale = 1.0                     // (0.1, 10.0) ????

    var starParticlesSpawnTime = 0.01       //in ms time delay between star particles spawn

    var cloudParticleSpawnTime = 0.1        //in ms time delay between cloud particles spawn

    var parallaxEffectMultiplier = 1.0f

    var enableParallaxEffect = false

    var textureQualityLevel = 0             // 0 - high quality, 1 - low quality

    //TODO: add to settings????
    var precessionSpeed = 0.0002f

    fun resetSettings() {
        timeScale = 1.0
        starParticlesSpawnTime = 0.01
        cloudParticleSpawnTime = 0.1
        parallaxEffectMultiplier = 1.0f
        enableParallaxEffect = false
        textureQualityLevel = 0
        precessionSpeed = 0.0002f
    }

    fun save(context : Context, filename : String) {
        File(context.filesDir, filename).bufferedWriter().use {
            it.write("timeScale=$timeScale\n")
            it.write("starParticlesSpawnTime=$starParticlesSpawnTime\n")
            it.write("cloudParticleSpawnTime=$cloudParticleSpawnTime\n")
            it.write("parallaxEffectMultiplier=$parallaxEffectMultiplier\n")
            it.write("enableParallaxEffect=$enableParallaxEffect\n")
            it.write("textureQualityLevel=$textureQualityLevel\n")
            it.write("precessionSpeed=$precessionSpeed")
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
                    "textureQualityLevel" -> textureQualityLevel = s[1].toInt()
                    "precessionSpeed" -> precessionSpeed = s[1].toFloat()
                }
            }
            }
        } catch(e : FileNotFoundException) {
            Log.debug("No settings file found!\n")
        }
    }

}