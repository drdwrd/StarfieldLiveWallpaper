package drwdrd.ktdev.starfield_free

import android.content.Context
import drwdrd.ktdev.engine.Log
import java.io.File
import java.io.FileNotFoundException
import java.lang.NumberFormatException

private const val TAG = "drwdrd.ktdev.starfield_free.SettingsProvider"

object SettingsProvider {

    private const val DEFAULT_PARTICLE_SPEED = 0.2f
    private const val DEFAULT_PARTICLE_SPAWN_TIME_MULTIPLIER = 0.1
    private const val DEFAULT_PARALLAX_EFFECT_MULTIPLIER = 0.5f
    private const val DEFAULT_TEXTURE_QUALITY_LEVEL = 0
    private const val DEFAULT_SLIDE_EFFECT_MULTIPLIER = 0.5f
    private const val DEFAULT_PRECESSION_SPEED = 0.005f
    const val TEXTURE_QUALITY_UNKNOWN = 100

    enum class TextureCompressionMode(val type : Int) {
        NONE(0),
        ETC1(1),
        ETC2(2),
        ASTC(3),
        UNKNOWN(4);

        companion object {
            fun fromInt(type : Int) : TextureCompressionMode {
                return when(type) {
                    0 -> NONE
                    1 -> ETC1
                    2 -> ETC2
                    3 -> ASTC
                    else -> UNKNOWN
                }
            }
        }
    }

    var textureCompressionMode = TextureCompressionMode.UNKNOWN

    var adaptiveFPS = true

    var particleSpeed = DEFAULT_PARTICLE_SPEED                     // (0.1, 10.0) ????

    var particlesSpawnTimeMultiplier = DEFAULT_PARTICLE_SPAWN_TIME_MULTIPLIER       //in ms time delay between star particles spawn

    var baseTextureQualityLevel = TEXTURE_QUALITY_UNKNOWN

    var textureQualityLevel = DEFAULT_TEXTURE_QUALITY_LEVEL             // 0 - high quality, 1 - low quality

    fun resetSettings() {
        adaptiveFPS = true
        particleSpeed = DEFAULT_PARTICLE_SPEED
        particlesSpawnTimeMultiplier = DEFAULT_PARTICLE_SPAWN_TIME_MULTIPLIER
        textureQualityLevel = DEFAULT_TEXTURE_QUALITY_LEVEL
    }

    fun save(context : Context, filename : String) {
        File(context.filesDir, filename).bufferedWriter().use {
            it.write("textureCompressionMode=${textureCompressionMode.type}\n")
            it.write("adaptiveFPS=$adaptiveFPS\n")
            it.write("particleSpeed=$particleSpeed\n")
            it.write("particlesSpawnTimeMultiplier=$particlesSpawnTimeMultiplier\n")
            it.write("baseTextureQualityLevel=$baseTextureQualityLevel\n")
            it.write("textureQualityLevel=$textureQualityLevel\n")
        }
    }

    fun load(context: Context, filename : String) {
        try {
            File(context.filesDir, filename).bufferedReader().useLines {
                lines -> lines.forEach {
                    val s = it.split("=")
                    if(s.size >= 2) {
                        when (s[0]) {
                            "textureCompressionMode" -> TextureCompressionMode.fromInt(s[1].toInt())
                            "adaptiveFPS" -> adaptiveFPS = s[1].toBoolean()
                            "particleSpeed" -> particleSpeed = s[1].toFloat()
                            "particlesSpawnTimeMultiplier" -> particlesSpawnTimeMultiplier = s[1].toDouble()
                            "baseTextureQualityLevel" -> baseTextureQualityLevel = s[1].toInt()
                            "textureQualityLevel" -> textureQualityLevel = s[1].toInt()
                        }
                    }
                }
            }
        } catch(e : FileNotFoundException) {
            Log.warning(TAG, "No settings file found!\n")
        } catch(e : NumberFormatException) {
            Log.warning(TAG, "Cannot parse settings!\n")
        }
    }
}