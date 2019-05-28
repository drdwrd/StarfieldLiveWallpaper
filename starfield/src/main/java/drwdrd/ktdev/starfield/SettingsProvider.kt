package drwdrd.ktdev.starfield

import android.content.Context
import drwdrd.ktdev.engine.Flag
import drwdrd.ktdev.engine.FlagType
import drwdrd.ktdev.engine.logw
import java.io.File
import java.io.FileNotFoundException
import java.lang.NumberFormatException



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

fun Flag<TextureCompressionMode>.supportsAlpha() : Boolean {
    return hasFlag(TextureCompressionMode.ASTC) or hasFlag(TextureCompressionMode.ETC2)
}


object SettingsProvider {
    private const val CONFIG_VERSION = 1
    private const val DEFAULT_PARTICLE_SPEED = 1.0f
    private const val DEFAULT_STARS_SPAWN_TIME_MULTIPLIER = 0.025
    private const val DEFAULT_CLOUDS_SPAWN_TIME_MULTIPLIER = 0.25
    private const val DEFAULT_PARALLAX_EFFECT_MULTIPLIER = 0.5f
    private const val DEFAULT_PARALLAX_EFFECT_ACCELERATION = 0.01f
    private const val DEFAULT_TEXTURE_QUALITY_LEVEL = 0
    private const val DEFAULT_SLIDE_EFFECT_MULTIPLIER = 0.5f
    private const val DEFAULT_CAMERA_ROTATION_SPEED = 0.0f
    const val TEXTURE_QUALITY_UNKNOWN = 100

    var version = CONFIG_VERSION

    var textureCompressionMode = Flag(TextureCompressionMode.UNKNOWN)

    var parallaxEffectEngineType = ParallaxEffectEngineType.Unknown

    var adaptiveFPS = true

    var particleSpeed = DEFAULT_PARTICLE_SPEED                     // (0.1, 10.0) ????

    var starsSpawnTimeMultiplier = DEFAULT_STARS_SPAWN_TIME_MULTIPLIER       //in ms time delay between star particles spawn

    var cloudsSpawnTimeMultiplier = DEFAULT_CLOUDS_SPAWN_TIME_MULTIPLIER       //in ms time delay between star particles spawn

    var parallaxEffectMultiplier = DEFAULT_PARALLAX_EFFECT_MULTIPLIER

    var parallaxEffectAcceleration = DEFAULT_PARALLAX_EFFECT_ACCELERATION

    var enableParallaxEffect = false

    var baseTextureQualityLevel = TEXTURE_QUALITY_UNKNOWN

    var textureQualityLevel = DEFAULT_TEXTURE_QUALITY_LEVEL             // 0 - high quality, 1 - low quality

    var scrollingEffectMultiplier = DEFAULT_SLIDE_EFFECT_MULTIPLIER

    var enableScrollingEffect = true

    var cameraRotationSpeed = DEFAULT_CAMERA_ROTATION_SPEED

    var currentTheme = 0

    var askDownloadDefaultTheme = true

    fun resetSettings() {
        version = CONFIG_VERSION
        textureCompressionMode = Flag(TextureCompressionMode.UNKNOWN)
        parallaxEffectEngineType = ParallaxEffectEngineType.Unknown
        adaptiveFPS = true
        particleSpeed = DEFAULT_PARTICLE_SPEED
        starsSpawnTimeMultiplier = DEFAULT_STARS_SPAWN_TIME_MULTIPLIER
        cloudsSpawnTimeMultiplier = DEFAULT_CLOUDS_SPAWN_TIME_MULTIPLIER
        cameraRotationSpeed = DEFAULT_CAMERA_ROTATION_SPEED
        parallaxEffectMultiplier = DEFAULT_PARALLAX_EFFECT_MULTIPLIER
        parallaxEffectAcceleration = DEFAULT_PARALLAX_EFFECT_ACCELERATION
        enableParallaxEffect = false
        baseTextureQualityLevel = TEXTURE_QUALITY_UNKNOWN
        textureQualityLevel = DEFAULT_TEXTURE_QUALITY_LEVEL
        enableScrollingEffect = true
        scrollingEffectMultiplier = DEFAULT_SLIDE_EFFECT_MULTIPLIER
        askDownloadDefaultTheme = true
    }

    //changed some params names because of collision with older ini files format (file are backed up so old file was restored and messed up initialization)
    fun save(context : Context, filename : String) {
        File(context.filesDir, filename).bufferedWriter().use {
            it.write("version=$version\n")
            it.write("textureCompression=${textureCompressionMode.flags}\n")
            it.write("parallaxEffectEngine=${parallaxEffectEngineType.type}\n")
            it.write("adaptiveFPS=$adaptiveFPS\n")
            it.write("particleSpeed=$particleSpeed\n")
            it.write("starsSpawnTimeMultiplier=$starsSpawnTimeMultiplier\n")
            it.write("cloudsSpawnTimeMultiplier=$cloudsSpawnTimeMultiplier\n")
            it.write("cameraRotationSpeed=$cameraRotationSpeed\n")
            it.write("parallaxEffectMultiplier=$parallaxEffectMultiplier\n")
            it.write("parallaxEffectAcceleration=$parallaxEffectAcceleration\n")
            it.write("enableParallaxEffect=$enableParallaxEffect\n")
            it.write("baseTextureQualityLevel=$baseTextureQualityLevel\n")
            it.write("textureQualityLevel=$textureQualityLevel\n")
            it.write("enableScrollingEffect=$enableScrollingEffect\n")
            it.write("scrollingEffectMultiplier=$scrollingEffectMultiplier\n")
            it.write("currentTheme=$currentTheme\n")
            it.write("askDownloadDefaultTheme=$askDownloadDefaultTheme\n")
        }
    }

    //TODO: make sure that old configs doesn't collide with new settings, make reset on CONFIG_VERSION change
    fun load(context: Context, filename : String) {
        version = 0
        try {
            File(context.filesDir, filename).bufferedReader().useLines {
                lines -> lines.forEach {
                    val s = it.split("=")
                    if(s.size >= 2) {
                        when (s[0]) {
                            "version" -> version = s[1].toInt()
                            "textureCompression" -> textureCompressionMode = Flag(s[1].toInt())
                            "parallaxEffectEngine" -> parallaxEffectEngineType = ParallaxEffectEngineType.fromInt(s[1].toInt())
                            "adaptiveFPS" -> adaptiveFPS = s[1].toBoolean()
                            "particleSpeed" -> particleSpeed = s[1].toFloat()
                            "cameraRotationSpeed" -> cameraRotationSpeed = s[1].toFloat()
                            "starsSpawnTimeMultiplier" -> starsSpawnTimeMultiplier = s[1].toDouble()
                            "cloudsSpawnTimeMultiplier" -> cloudsSpawnTimeMultiplier = s[1].toDouble()
                            "parallaxEffectMultiplier" -> parallaxEffectMultiplier = s[1].toFloat()
                            "parallaxEffectAcceleration" -> parallaxEffectAcceleration = s[1].toFloat()
                            "enableParallaxEffect" -> enableParallaxEffect = s[1].toBoolean()
                            "baseTextureQualityLevel" -> baseTextureQualityLevel = s[1].toInt()
                            "textureQualityLevel" -> textureQualityLevel = s[1].toInt()
                            "enableScrollingEffect" -> enableScrollingEffect = s[1].toBoolean()
                            "scrollingEffectMultiplier" -> scrollingEffectMultiplier = s[1].toFloat()
                            "currentTheme" -> currentTheme = s[1].toInt()
                            "askDownloadDefaultTheme" -> askDownloadDefaultTheme = s[1].toBoolean()
                        }
                    }
                }
            }
        } catch(e : FileNotFoundException) {
            resetSettings()
            logw("No settings file found!\n")
        } catch(e : NumberFormatException) {
            resetSettings()
            logw("Cannot parse settings!\n")
        }
        if(version != CONFIG_VERSION) {
            resetSettings()
        }
        if(!ThemeInfo.themes[currentTheme].setActive(context, null)) {
            currentTheme = 0
        }
    }
}
