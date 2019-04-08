package drwdrd.ktdev.starfield

import android.content.Context
import drwdrd.ktdev.engine.Flag
import drwdrd.ktdev.engine.KTXLoader
import drwdrd.ktdev.engine.Log
import drwdrd.ktdev.engine.Texture

private const val TAG = "drwdrd.ktdev.starfield.Theme"

interface Theme {
    fun starfieldTexture(context : Context, textureQuality : Int, textureCompressionMode: Flag<SettingsProvider.TextureCompressionMode>) : Texture?
    fun starsTexture(context : Context, textureQuality : Int, textureCompressionMode: Flag<SettingsProvider.TextureCompressionMode>) : Texture?
    fun cloudsTexture(context : Context, textureQuality : Int, textureCompressionMode: Flag<SettingsProvider.TextureCompressionMode>) : Texture?
    fun hasClouds() : Boolean
    fun hasStars() : Boolean
    fun hasBackground() : Boolean
}

class Starfield2Theme : Theme {

    override fun starfieldTexture(context : Context, textureQuality : Int, textureCompressionMode: Flag<SettingsProvider.TextureCompressionMode>) : Texture? {
//        return Texture.loadFromAssets2D(context, "themes/starfield2/starfield2.png", textureQuality, Texture.WrapMode.Repeat, Texture.WrapMode.Repeat,
//            Texture.Filtering.LinearMipmapLinear, Texture.Filtering.Linear)
        return KTXLoader.loadFromAssets(context, "themes/starfield2/astc/starfield2.ktx", textureQuality, Texture.WrapMode.Repeat, Texture.WrapMode.Repeat,
            Texture.Filtering.LinearMipmapLinear, Texture.Filtering.Linear)
    }

    override fun starsTexture(context : Context, textureQuality : Int, textureCompressionMode: Flag<SettingsProvider.TextureCompressionMode>) : Texture? {
//        return Texture.loadFromAssets2D(context, "themes/starfield2/starsprites2.png", textureQuality, Texture.WrapMode.ClampToEdge, Texture.WrapMode.ClampToEdge,
//            Texture.Filtering.LinearMipmapLinear, Texture.Filtering.Linear)
        return KTXLoader.loadFromAssets(context, "themes/starfield2/astc/starsprites2.ktx", textureQuality, Texture.WrapMode.ClampToEdge, Texture.WrapMode.ClampToEdge,
            Texture.Filtering.LinearMipmapLinear, Texture.Filtering.Linear)
    }

    override fun cloudsTexture(context : Context, textureQuality : Int, textureCompressionMode: Flag<SettingsProvider.TextureCompressionMode>) : Texture? {
        return null
    }

    override fun hasBackground(): Boolean = true

    override fun hasClouds(): Boolean = false

    override fun hasStars(): Boolean = true
}

class DefaultTheme : Theme {

    override fun starfieldTexture(context : Context, textureQuality : Int, textureCompressionMode: Flag<SettingsProvider.TextureCompressionMode>) : Texture? {
        return when {
            SettingsProvider.textureCompressionMode.hasFlag(SettingsProvider.TextureCompressionMode.ASTC) -> {
                //astc
                KTXLoader.loadFromAssets(
                    context,
                    "themes/default/astc/starfield.ktx",
                    textureQuality,
                    Texture.WrapMode.Repeat,
                    Texture.WrapMode.Repeat,
                    Texture.Filtering.LinearMipmapLinear,
                    Texture.Filtering.Linear
                )
            }
            SettingsProvider.textureCompressionMode.hasFlag(SettingsProvider.TextureCompressionMode.ETC2) -> {
                //etc2
                KTXLoader.loadFromAssets(
                    context,
                    "themes/default/etc2/starfield.ktx",
                    textureQuality,
                    Texture.WrapMode.Repeat,
                    Texture.WrapMode.Repeat,
                    Texture.Filtering.LinearMipmapLinear,
                    Texture.Filtering.Linear
                )
            }
            SettingsProvider.textureCompressionMode.hasFlag(SettingsProvider.TextureCompressionMode.ETC1) -> {
                //etc
                KTXLoader.loadFromAssets(
                    context,
                    "themes/default/etc/starfield.ktx",
                    textureQuality,
                    Texture.WrapMode.Repeat,
                    Texture.WrapMode.Repeat,
                    Texture.Filtering.LinearMipmapLinear,
                    Texture.Filtering.Linear
                )
            }
            else -> {
                Log.error(TAG, "Unsupported texture compression format : ${textureCompressionMode.flags}")
                null
            }
        }

    }

    override fun starsTexture(context : Context, textureQuality : Int, textureCompressionMode: Flag<SettingsProvider.TextureCompressionMode>) : Texture? {
        return when {
            SettingsProvider.textureCompressionMode.hasFlag(SettingsProvider.TextureCompressionMode.ASTC) -> {
                //astc
                KTXLoader.loadFromAssets(
                    context,
                    "themes/default/astc/starsprites.ktx",
                    textureQuality,
                    Texture.WrapMode.ClampToEdge,
                    Texture.WrapMode.ClampToEdge,
                    Texture.Filtering.LinearMipmapLinear,
                    Texture.Filtering.Linear
                )
            }
            SettingsProvider.textureCompressionMode.hasFlag(SettingsProvider.TextureCompressionMode.ETC2) -> {
                //etc2
                KTXLoader.loadFromAssets(
                    context,
                    "themes/default/etc2/starsprites.ktx",
                    textureQuality,
                    Texture.WrapMode.ClampToEdge,
                    Texture.WrapMode.ClampToEdge,
                    Texture.Filtering.LinearMipmapLinear,
                    Texture.Filtering.Linear
                )
            }
            SettingsProvider.textureCompressionMode.hasFlag(SettingsProvider.TextureCompressionMode.ETC1) -> {
                //etc
                Texture.loadFromAssets2D(
                    context,
                    "themes/default/png/starsprites.png",
                    textureQuality,
                    Texture.WrapMode.ClampToEdge,
                    Texture.WrapMode.ClampToEdge,
                    Texture.Filtering.LinearMipmapLinear,
                    Texture.Filtering.Linear
                )
            }
            else -> {
                //png
                Texture.loadFromAssets2D(
                    context,
                    "themes/default/png/starsprites.png",
                    textureQuality,
                    Texture.WrapMode.ClampToEdge,
                    Texture.WrapMode.ClampToEdge,
                    Texture.Filtering.LinearMipmapLinear,
                    Texture.Filtering.Linear
                )
            }
        }
    }

    override fun cloudsTexture(context : Context, textureQuality : Int, textureCompressionMode: Flag<SettingsProvider.TextureCompressionMode>) : Texture {
        return Texture.loadFromAssets2D(context, "themes/default/png/cloud.png", textureQuality, Texture.WrapMode.ClampToEdge, Texture.WrapMode.ClampToEdge, Texture.Filtering.LinearMipmapLinear, Texture.Filtering.Linear)
    }

    override fun hasBackground(): Boolean = true

    override fun hasClouds(): Boolean = false

    override fun hasStars(): Boolean = true
}