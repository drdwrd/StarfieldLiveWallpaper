package drwdrd.ktdev.starfield

import android.content.Context
import drwdrd.ktdev.engine.Flag
import drwdrd.ktdev.engine.FlagType
import drwdrd.ktdev.engine.Log
import java.io.File
import java.io.FileNotFoundException
import java.lang.NumberFormatException

private const val TAG = "drwdrd.ktdev.starfield.SettingsProvider"

object SettingsProvider {

    private const val DEFAULT_PARTICLE_SPEED = 0.2f
    private const val DEFAULT_PARTICLE_SPAWN_TIME_MULTIPLIER = 0.1
    private const val DEFAULT_PARALLAX_EFFECT_MULTIPLIER = 0.5f
    private const val DEFAULT_TEXTURE_QUALITY_LEVEL = 0
    private const val DEFAULT_SLIDE_EFFECT_MULTIPLIER = 0.5f
    private const val DEFAULT_CAMERA_ROTATION_SPEED = 0.01f
    private const val DEFAULT_CAMERA_MOVEMENT_ACCELERATION = 0.01f
    const val TEXTURE_QUALITY_UNKNOWN = 100

    enum class ParallaxEffectEngineType(val type : Int) {
        None(0),
        Accelerometer(1),
        Gravity(2),
        Gyro(3),
        Unknown(4);

        companion object {
            fun fromInt(type: Int): ParallaxEffectEngineType {
                return when(type) {
                    None.type -> None
                    Accelerometer.type -> Accelerometer
                    Gravity.type -> Gravity
                    Gyro.type -> Gyro
                    else -> Unknown
                }
            }
        }
    }

    class TextureCompressionMode private constructor(override val type: Int) : FlagType() {

        companion object {
            val NONE = TextureCompressionMode(0b00000001)
            val ETC1 = TextureCompressionMode(0b00000010)
            val ETC2 = TextureCompressionMode(0b00000100)
            val ASTC = TextureCompressionMode(0b00001000)
            val UNKNOWN = TextureCompressionMode(0b00000000)

            fun fromInt(type: Int): TextureCompressionMode {
                return when (type) {
                    NONE.type -> NONE
                    ETC1.type -> ETC1
                    ETC2.type -> ETC2
                    ASTC.type -> ASTC
                    else -> UNKNOWN
                }
            }
        }
    }

    var textureCompressionMode = Flag(TextureCompressionMode.UNKNOWN)

    var parallaxEffectEngineType = ParallaxEffectEngineType.Unknown

    var adaptiveFPS = true

    var particleSpeed = DEFAULT_PARTICLE_SPEED                     // (0.1, 10.0) ????

    var particlesSpawnTimeMultiplier = DEFAULT_PARTICLE_SPAWN_TIME_MULTIPLIER       //in ms time delay between star particles spawn

    var parallaxEffectMultiplier = DEFAULT_PARALLAX_EFFECT_MULTIPLIER

    var enableParallaxEffect = false

    var baseTextureQualityLevel = TEXTURE_QUALITY_UNKNOWN

    var textureQualityLevel = DEFAULT_TEXTURE_QUALITY_LEVEL             // 0 - high quality, 1 - low quality

    var scrollingEffectMultiplier = DEFAULT_SLIDE_EFFECT_MULTIPLIER

    var enableScrollingEffect = true

    var cameraRotationSpeed = DEFAULT_CAMERA_ROTATION_SPEED

    var cameraMovementAcceleration = DEFAULT_CAMERA_MOVEMENT_ACCELERATION

    fun resetSettings() {
        textureCompressionMode = Flag(TextureCompressionMode.UNKNOWN)
        parallaxEffectEngineType = ParallaxEffectEngineType.Unknown
        adaptiveFPS = true
        particleSpeed = DEFAULT_PARTICLE_SPEED
        particlesSpawnTimeMultiplier = DEFAULT_PARTICLE_SPAWN_TIME_MULTIPLIER
        parallaxEffectMultiplier = DEFAULT_PARALLAX_EFFECT_MULTIPLIER
        enableParallaxEffect = false
        baseTextureQualityLevel = TEXTURE_QUALITY_UNKNOWN
        textureQualityLevel = DEFAULT_TEXTURE_QUALITY_LEVEL
        enableScrollingEffect = true
        scrollingEffectMultiplier = DEFAULT_SLIDE_EFFECT_MULTIPLIER
    }

    //changed some params names because of collision with older ini files format (file are backed up so old file was restored and messed up initialization)
    fun save(context : Context, filename : String) {
        File(context.filesDir, filename).bufferedWriter().use {
            it.write("textureCompression=${textureCompressionMode.flags}\n")
            it.write("parallaxEffectEngine=${parallaxEffectEngineType.type}\n")
            it.write("adaptiveFPS=$adaptiveFPS\n")
            it.write("particleSpeed=$particleSpeed\n")
            it.write("particlesSpawnTimeMultiplier=$particlesSpawnTimeMultiplier\n")
            it.write("parallaxEffectMultiplier=$parallaxEffectMultiplier\n")
            it.write("enableParallaxEffect=$enableParallaxEffect\n")
            it.write("baseTextureQualityLevel=$baseTextureQualityLevel\n")
            it.write("textureQualityLevel=$textureQualityLevel\n")
            it.write("enableScrollingEffect=$enableScrollingEffect\n")
            it.write("scrollingEffectMultiplier=$scrollingEffectMultiplier\n")
        }
    }

    //TODO: make sure that old configs doesn't collide with new settings
    fun load(context: Context, filename : String) {
        try {
            File(context.filesDir, filename).bufferedReader().useLines {
                lines -> lines.forEach {
                    val s = it.split("=")
                    if(s.size >= 2) {
                        when (s[0]) {
                            "textureCompression" -> textureCompressionMode = Flag(s[1].toInt())
                            "parallaxEffectEngine" -> parallaxEffectEngineType = ParallaxEffectEngineType.fromInt(s[1].toInt())
                            "adaptiveFPS" -> adaptiveFPS = s[1].toBoolean()
                            "particleSpeed" -> particleSpeed = s[1].toFloat()
                            "particlesSpawnTimeMultiplier" -> particlesSpawnTimeMultiplier = s[1].toDouble()
                            "parallaxEffectMultiplier" -> parallaxEffectMultiplier = s[1].toFloat()
                            "enableParallaxEffect" -> enableParallaxEffect = s[1].toBoolean()
                            "baseTextureQualityLevel" -> baseTextureQualityLevel = s[1].toInt()
                            "textureQualityLevel" -> textureQualityLevel = s[1].toInt()
                            "enableScrollingEffect" -> enableScrollingEffect = s[1].toBoolean()
                            "scrollingEffectMultiplier" -> scrollingEffectMultiplier = s[1].toFloat()
                        }
                    }
                }
            }
        } catch(e : FileNotFoundException) {
            resetSettings()
            Log.warning(TAG, "No settings file found!\n")
        } catch(e : NumberFormatException) {
            resetSettings()
            Log.warning(TAG, "Cannot parse settings!\n")
        }
    }
}
