package drwdrd.ktdev.starfield

import android.content.Context
import drwdrd.ktdev.engine.Flag
import drwdrd.ktdev.engine.KTXLoader
import drwdrd.ktdev.engine.Log
import drwdrd.ktdev.engine.Texture
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.lang.Exception
import javax.xml.parsers.DocumentBuilderFactory

private const val TAG = "drwdrd.ktdev.starfield.Theme"

interface Theme {

    val backgroundScale : Float
    val starsParticleScale : Float
    val cloudsParticleScale : Float
    val cloudColors : Array<Long>

    fun loadTheme(context: Context) : Boolean

    fun starfieldTexture(context : Context, textureQuality : Int, textureCompressionMode: Flag<SettingsProvider.TextureCompressionMode>) : Texture?
    fun starsTexture(context : Context, textureQuality : Int, textureCompressionMode: Flag<SettingsProvider.TextureCompressionMode>) : Texture?
    fun cloudsTexture(context : Context, textureQuality : Int, textureCompressionMode: Flag<SettingsProvider.TextureCompressionMode>) : Texture?
    fun hasClouds() : Boolean
    fun hasStars() : Boolean
    fun hasBackground() : Boolean
}

class ThemePackage(name : String) : Theme {

    private inner class ThemeTextureInfo {

        constructor() {
            isValid = false
            name = ""
            textureCompressionMode = SettingsProvider.TextureCompressionMode.UNKNOWN
        }

        constructor(_format : String, _name : String) {
            isValid = true
            name = _name
            textureCompressionMode = parseTextureCompressionFormat(_format)
        }

        val isValid : Boolean
        val name : String
        val textureCompressionMode : SettingsProvider.TextureCompressionMode

        private fun parseTextureCompressionFormat(format : String) : SettingsProvider.TextureCompressionMode {
            return when(format) {
                "astc" -> SettingsProvider.TextureCompressionMode.ASTC
                "etc2" -> SettingsProvider.TextureCompressionMode.ETC2
                "etc" -> SettingsProvider.TextureCompressionMode.ETC1
                "png" -> SettingsProvider.TextureCompressionMode.NONE
                else -> SettingsProvider.TextureCompressionMode.UNKNOWN
            }
        }
    }

    override var backgroundScale: Float = 1.0f
        private set

    override var cloudsParticleScale: Float = 1.0f
        private set

    override var starsParticleScale: Float = 1.0f
        private set

    override val cloudColors: Array<Long> = emptyArray()


    private val themeName : String = name
    private lateinit var themePath : String
    private var starfieldInfo = ThemeTextureInfo()
    private var starsInfo = ThemeTextureInfo()
    private var cloudsInfo = ThemeTextureInfo()

    override fun loadTheme(context: Context) : Boolean {
        val cacheDir = context.getExternalFilesDir(null) ?: return false
        val location = File(cacheDir, themeName)
        themePath = location.absolutePath
        val xmlFile = "$themePath/$themeName.xml"
        val inputStream = try { FileInputStream(xmlFile) } catch (e : FileNotFoundException) { return false }
        try {
            inputStream.use {
                val builderFactory = DocumentBuilderFactory.newInstance()
                val builder = builderFactory.newDocumentBuilder()
                val dom = builder.parse(it)
                val themeNode = dom.getElementsByTagName("theme").item(0)
                for (i in 0 until themeNode.childNodes.length) {
                    val node = themeNode.childNodes.item(i)
                    when (node.nodeName) {
                        "background" -> {
                            val name = node.attributes.getNamedItem("name")?.nodeValue ?: return false
                            val format = node.attributes.getNamedItem("format")?.nodeValue ?: return false
                            starfieldInfo = ThemeTextureInfo(format, name)
                            backgroundScale = node.attributes.getNamedItem("scale")?.nodeValue?.toFloat() ?: 1.0f
                        }
                        "starsprites" -> {
                            val name = node.attributes.getNamedItem("name")?.nodeValue ?: return false
                            val format = node.attributes.getNamedItem("format")?.nodeValue ?: return false
                            starsInfo = ThemeTextureInfo(format, name)
                            starsParticleScale = node.attributes.getNamedItem("scale")?.nodeValue?.toFloat() ?: 1.0f
                        }
                        "cloudsprites" -> {
                            val name = node.attributes.getNamedItem("name")?.nodeValue ?: return false
                            val format = node.attributes.getNamedItem("format")?.nodeValue ?: return false
                            cloudsInfo = ThemeTextureInfo(format, name)
                            cloudsParticleScale = node.attributes.getNamedItem("scale")?.nodeValue?.toFloat() ?: 1.0f
                        }
                    }
                }
            }
        } catch (e : Exception) {
            return false
        }
        return true
    }

    override fun starfieldTexture(context: Context, textureQuality: Int, textureCompressionMode: Flag<SettingsProvider.TextureCompressionMode>): Texture? {
        return when(starfieldInfo.textureCompressionMode) {
            SettingsProvider.TextureCompressionMode.NONE ->
                Texture.loadFromPath(context, "$themePath/${starfieldInfo.name}", textureQuality, Texture.WrapMode.Repeat, Texture.WrapMode.Repeat,
                    Texture.Filtering.LinearMipmapLinear, Texture.Filtering.Linear)
            SettingsProvider.TextureCompressionMode.ASTC, SettingsProvider.TextureCompressionMode.ETC2, SettingsProvider.TextureCompressionMode.ETC1 ->
                KTXLoader.loadFromPath(context, "$themePath/${starfieldInfo.name}", textureQuality, Texture.WrapMode.Repeat, Texture.WrapMode.Repeat,
                    Texture.Filtering.LinearMipmapLinear, Texture.Filtering.Linear)
            else -> null
        }
    }

    override fun starsTexture(context: Context, textureQuality: Int, textureCompressionMode: Flag<SettingsProvider.TextureCompressionMode>): Texture? {
        return when(starsInfo.textureCompressionMode) {
            SettingsProvider.TextureCompressionMode.NONE ->
                Texture.loadFromPath(context, "$themePath/${starsInfo.name}", textureQuality, Texture.WrapMode.ClampToEdge, Texture.WrapMode.ClampToEdge,
                    Texture.Filtering.LinearMipmapLinear, Texture.Filtering.Linear)
            SettingsProvider.TextureCompressionMode.ASTC, SettingsProvider.TextureCompressionMode.ETC2, SettingsProvider.TextureCompressionMode.ETC1 ->
                KTXLoader.loadFromPath(context, "$themePath/${starsInfo.name}", textureQuality, Texture.WrapMode.ClampToEdge, Texture.WrapMode.ClampToEdge,
                    Texture.Filtering.LinearMipmapLinear, Texture.Filtering.Linear)
            else -> null
        }
    }

    override fun cloudsTexture(context: Context, textureQuality: Int, textureCompressionMode: Flag<SettingsProvider.TextureCompressionMode>): Texture? {
        return when(cloudsInfo.textureCompressionMode) {
            SettingsProvider.TextureCompressionMode.NONE ->
                Texture.loadFromPath(context, "$themePath/${cloudsInfo.name}", textureQuality, Texture.WrapMode.ClampToEdge, Texture.WrapMode.ClampToEdge,
                    Texture.Filtering.LinearMipmapLinear, Texture.Filtering.Linear)
            SettingsProvider.TextureCompressionMode.ASTC, SettingsProvider.TextureCompressionMode.ETC2, SettingsProvider.TextureCompressionMode.ETC1 ->
                KTXLoader.loadFromPath(context, "$themePath/${cloudsInfo.name}", textureQuality, Texture.WrapMode.ClampToEdge, Texture.WrapMode.ClampToEdge,
                    Texture.Filtering.LinearMipmapLinear, Texture.Filtering.Linear)
            else -> null
        }
    }

    override fun hasBackground(): Boolean = starfieldInfo.isValid

    override fun hasStars(): Boolean = starsInfo.isValid

    override fun hasClouds(): Boolean = cloudsInfo.isValid
}

class TestTheme : Theme {

    override val backgroundScale: Float = 1.0f
    override val cloudsParticleScale: Float = 1.0f
    override val starsParticleScale: Float = 1.0f
    override val cloudColors: Array<Long> = emptyArray()

    override fun loadTheme(context: Context): Boolean {
        return true
    }

    override fun starfieldTexture(context: Context, textureQuality: Int, textureCompressionMode: Flag<SettingsProvider.TextureCompressionMode>): Texture? {
        return null
    }

    override fun cloudsTexture(context: Context, textureQuality: Int, textureCompressionMode: Flag<SettingsProvider.TextureCompressionMode>): Texture? {
        return null
    }

    override fun starsTexture(context: Context, textureQuality: Int, textureCompressionMode: Flag<SettingsProvider.TextureCompressionMode>): Texture? {
        return Texture.loadFromAssets2D(context,"themes/stars_atlas.png", textureQuality, Texture.WrapMode.ClampToEdge, Texture.WrapMode.ClampToEdge,
            Texture.Filtering.LinearMipmapLinear, Texture.Filtering.Linear)
    }

    override fun hasBackground(): Boolean = false

    override fun hasClouds(): Boolean = false

    override fun hasStars(): Boolean = true
}

class DefaultTheme : Theme {

    override val backgroundScale: Float = 1.0f
    override val cloudsParticleScale: Float = 1.2f
    override val starsParticleScale: Float = 1.5f

    override val cloudColors: Array<Long> = arrayOf(0xff0c134e, 0xff360e3a, 0xff70b3ff)

    override fun loadTheme(context: Context): Boolean {
        return true
    }

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
        return when {
            SettingsProvider.textureCompressionMode.hasFlag(SettingsProvider.TextureCompressionMode.ASTC) -> {
                //astc
                KTXLoader.loadFromAssets(
                    context,
                    "themes/default/astc/cloudsprites.ktx",
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
                    "themes/default/etc2/cloudsprites.ktx",
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
                    "themes/default/png/cloudsprites.png",
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
                    "themes/default/png/cloudsprites.png",
                    textureQuality,
                    Texture.WrapMode.ClampToEdge,
                    Texture.WrapMode.ClampToEdge,
                    Texture.Filtering.LinearMipmapLinear,
                    Texture.Filtering.Linear
                )
            }
        }
    }

    override fun hasBackground(): Boolean = true

    override fun hasClouds(): Boolean = true

    override fun hasStars(): Boolean = true
}