package drwdrd.ktdev.starfield

import android.content.Context
import drwdrd.ktdev.engine.*
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
    fun starfieldShader(context: Context, vertexFormat: VertexFormat, textureCompressionMode: Flag<SettingsProvider.TextureCompressionMode>) : ProgramObject?
    fun starsShader(context: Context, vertexFormat: VertexFormat, textureCompressionMode: Flag<SettingsProvider.TextureCompressionMode>) : ProgramObject?
    fun cloudsShader(context: Context, vertexFormat: VertexFormat, textureCompressionMode: Flag<SettingsProvider.TextureCompressionMode>) : ProgramObject?
    fun hasClouds() : Boolean
    fun hasStars() : Boolean
    fun hasBackground() : Boolean
}

class ThemePackage(private val themeName : String) : Theme {

    private inner class ThemeTextureInfo(val name : String, format : String, val isValid : Boolean = true) {

        constructor() : this("null", "unknown", false)

        val textureCompressionMode = parseTextureCompressionFormat(format)

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

    override var cloudColors: Array<Long> = emptyArray()
        private set


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
                            starfieldInfo = ThemeTextureInfo(name, format)
                            backgroundScale = node.attributes.getNamedItem("scale")?.nodeValue?.toFloat() ?: 1.0f
                        }
                        "starsprites" -> {
                            val name = node.attributes.getNamedItem("name")?.nodeValue ?: return false
                            val format = node.attributes.getNamedItem("format")?.nodeValue ?: return false
                            starsInfo = ThemeTextureInfo(name, format)
                            starsParticleScale = node.attributes.getNamedItem("scale")?.nodeValue?.toFloat() ?: 1.0f
                        }
                        "cloudsprites" -> {
                            val name = node.attributes.getNamedItem("name")?.nodeValue ?: return false
                            val format = node.attributes.getNamedItem("format")?.nodeValue ?: return false
                            cloudsInfo = ThemeTextureInfo(name, format)
                            cloudsParticleScale = node.attributes.getNamedItem("scale")?.nodeValue?.toFloat() ?: 1.0f
                            val colorList = ArrayList<String>()
                            for (j in 0 until node.childNodes.length) {
                                val childNode = node.childNodes.item(j)
                                if(childNode.nodeName == "color") {
                                    colorList.add(childNode.textContent)
                                }
                            }
                            cloudColors = Array(colorList.size) { index -> colorList[index].removePrefix("0x").toLong(16) }
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
                Texture.loadFromPath("$themePath/${starfieldInfo.name}", textureQuality, Texture.WrapMode.Repeat, Texture.WrapMode.Repeat,
                    Texture.Filtering.LinearMipmapLinear, Texture.Filtering.Linear)
            SettingsProvider.TextureCompressionMode.ASTC, SettingsProvider.TextureCompressionMode.ETC2, SettingsProvider.TextureCompressionMode.ETC1 ->
                KTXLoader.loadFromPath("$themePath/${starfieldInfo.name}", textureQuality, Texture.WrapMode.Repeat, Texture.WrapMode.Repeat,
                    Texture.Filtering.LinearMipmapLinear, Texture.Filtering.Linear)
            else -> null
        }
    }

    override fun starsTexture(context: Context, textureQuality: Int, textureCompressionMode: Flag<SettingsProvider.TextureCompressionMode>): Texture? {
        return when(starsInfo.textureCompressionMode) {
            SettingsProvider.TextureCompressionMode.NONE ->
                Texture.loadFromPath("$themePath/${starsInfo.name}", textureQuality, Texture.WrapMode.ClampToEdge, Texture.WrapMode.ClampToEdge,
                    Texture.Filtering.LinearMipmapLinear, Texture.Filtering.Linear)
            SettingsProvider.TextureCompressionMode.ASTC, SettingsProvider.TextureCompressionMode.ETC2, SettingsProvider.TextureCompressionMode.ETC1 ->
                KTXLoader.loadFromPath("$themePath/${starsInfo.name}", textureQuality, Texture.WrapMode.ClampToEdge, Texture.WrapMode.ClampToEdge,
                    Texture.Filtering.LinearMipmapLinear, Texture.Filtering.Linear)
            else -> null
        }
    }

    override fun cloudsTexture(context: Context, textureQuality: Int, textureCompressionMode: Flag<SettingsProvider.TextureCompressionMode>): Texture? {
        return when(cloudsInfo.textureCompressionMode) {
            SettingsProvider.TextureCompressionMode.NONE ->
                Texture.loadFromPath("$themePath/${cloudsInfo.name}", textureQuality, Texture.WrapMode.ClampToEdge, Texture.WrapMode.ClampToEdge,
                    Texture.Filtering.LinearMipmapLinear, Texture.Filtering.Linear)
            SettingsProvider.TextureCompressionMode.ASTC, SettingsProvider.TextureCompressionMode.ETC2, SettingsProvider.TextureCompressionMode.ETC1 ->
                KTXLoader.loadFromPath("$themePath/${cloudsInfo.name}", textureQuality, Texture.WrapMode.ClampToEdge, Texture.WrapMode.ClampToEdge,
                    Texture.Filtering.LinearMipmapLinear, Texture.Filtering.Linear)
            else -> null
        }
    }

    override fun starfieldShader(context: Context, vertexFormat: VertexFormat, textureCompressionMode: Flag<SettingsProvider.TextureCompressionMode>) : ProgramObject? {
        return ProgramObject.loadFromAssets(context, "shaders/starfield.vert", "shaders/starfield.frag", vertexFormat)
    }

    override fun starsShader(context: Context, vertexFormat: VertexFormat, textureCompressionMode: Flag<SettingsProvider.TextureCompressionMode>) : ProgramObject? {
        return if(SettingsProvider.textureCompressionMode.hasFlag(SettingsProvider.TextureCompressionMode.ASTC) || SettingsProvider.textureCompressionMode.hasFlag(SettingsProvider.TextureCompressionMode.ETC2)) {
            ProgramObject.loadFromAssets(context, "shaders/starsprite.vert", "shaders/starsprite_pm.frag", vertexFormat)
        } else {
            ProgramObject.loadFromAssets(context, "shaders/starsprite.vert", "shaders/starsprite.frag", vertexFormat)
        }
    }

    override fun cloudsShader(context: Context, vertexFormat: VertexFormat, textureCompressionMode: Flag<SettingsProvider.TextureCompressionMode>) : ProgramObject? {
        return if(SettingsProvider.textureCompressionMode.hasFlag(SettingsProvider.TextureCompressionMode.ASTC) || SettingsProvider.textureCompressionMode.hasFlag(SettingsProvider.TextureCompressionMode.ETC2)) {
            ProgramObject.loadFromAssets(context, "shaders/cloudsprite.vert", "shaders/cloudsprite_pm.frag", vertexFormat)
        } else {
            ProgramObject.loadFromAssets(context, "shaders/cloudsprite.vert", "shaders/cloudsprite.frag", vertexFormat)
        }
    }

    override fun hasBackground(): Boolean = starfieldInfo.isValid

    override fun hasStars(): Boolean = starsInfo.isValid

    override fun hasClouds(): Boolean = cloudsInfo.isValid
}
/*
class TestTheme : Theme {

    override val backgroundScale: Float = 1.0f
    override val cloudsParticleScale: Float = 1.0f
    override val starsParticleScale: Float = 0.075f
    override val cloudColors: Array<Long> = arrayOf(0xff7f7f7f, 0xff7f7f7f)

    override fun loadTheme(context: Context): Boolean {
        return true
    }

    override fun starfieldTexture(context: Context, textureQuality: Int, textureCompressionMode: Flag<SettingsProvider.TextureCompressionMode>): Texture? {
        /*return Texture.loadFromAssets2D(context,"themes/test/starfield2.png", textureQuality, Texture.WrapMode.Repeat, Texture.WrapMode.Repeat,
            Texture.Filtering.LinearMipmapLinear, Texture.Filtering.Linear)*/
        return null
    }

    override fun cloudsTexture(context: Context, textureQuality: Int, textureCompressionMode: Flag<SettingsProvider.TextureCompressionMode>): Texture? {
        /*return Texture.loadFromAssets2D(context,"themes/test/cloudsprites.png", textureQuality, Texture.WrapMode.ClampToEdge, Texture.WrapMode.ClampToEdge,
            Texture.Filtering.LinearMipmapLinear, Texture.Filtering.Linear)*/
        return null
    }

    override fun starsTexture(context: Context, textureQuality: Int, textureCompressionMode: Flag<SettingsProvider.TextureCompressionMode>): Texture? {
        return Texture.loadFromAssets2D(context,"themes/test/stars_atlas.png", textureQuality, Texture.WrapMode.ClampToEdge, Texture.WrapMode.ClampToEdge,
            Texture.Filtering.LinearMipmapLinear, Texture.Filtering.Linear)
    }

    override fun starfieldShader(context: Context, vertexFormat: VertexFormat, textureCompressionMode: Flag<SettingsProvider.TextureCompressionMode>) : ProgramObject? {
        return ProgramObject.loadFromAssets(context, "shaders/starfield.vert", "shaders/starfield.frag", vertexFormat)
    }

    override fun starsShader(context: Context, vertexFormat: VertexFormat, textureCompressionMode: Flag<SettingsProvider.TextureCompressionMode>) : ProgramObject? {
        return ProgramObject.loadFromAssets(context, "shaders/starsprite.vert", "shaders/starsprite.frag", vertexFormat)
    }

    override fun cloudsShader(context: Context, vertexFormat: VertexFormat, textureCompressionMode: Flag<SettingsProvider.TextureCompressionMode>) : ProgramObject? {
        return ProgramObject.loadFromAssets(context, "shaders/cloudsprite.vert", "shaders/cloudsprite.frag", vertexFormat)
    }

    override fun hasBackground(): Boolean = false

    override fun hasClouds(): Boolean = false

    override fun hasStars(): Boolean = true
}
*/
class DefaultTheme : Theme {

    override val backgroundScale: Float = 1.0f
    override val cloudsParticleScale: Float = 2.5f
    override val starsParticleScale: Float = 0.07f

    override val cloudColors: Array<Long> = arrayOf(0xff0c134e, 0xff360e3a, 0xff70b3ff)

    override fun loadTheme(context: Context): Boolean {
        return true
    }

    override fun starfieldTexture(context : Context, textureQuality : Int, textureCompressionMode: Flag<SettingsProvider.TextureCompressionMode>) : Texture? {
        return when {
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
        return Texture.loadFromAssets2D(context,"themes/default/png/starsprites.png", textureQuality, Texture.WrapMode.ClampToEdge, Texture.WrapMode.ClampToEdge,
                    Texture.Filtering.LinearMipmapLinear, Texture.Filtering.Linear)
    }

    override fun cloudsTexture(context : Context, textureQuality : Int, textureCompressionMode: Flag<SettingsProvider.TextureCompressionMode>) : Texture {
        return Texture.loadFromAssets2D(context,"themes/default/png/cloudsprites.png", textureQuality, Texture.WrapMode.ClampToEdge, Texture.WrapMode.ClampToEdge,
                    Texture.Filtering.LinearMipmapLinear, Texture.Filtering.Linear)
    }

    override fun starfieldShader(context: Context, vertexFormat: VertexFormat, textureCompressionMode: Flag<SettingsProvider.TextureCompressionMode>) : ProgramObject? {
        return ProgramObject.loadFromAssets(context, "shaders/starfield.vert", "shaders/starfield.frag", vertexFormat)
    }

    override fun starsShader(context: Context, vertexFormat: VertexFormat, textureCompressionMode: Flag<SettingsProvider.TextureCompressionMode>) : ProgramObject? {
        return ProgramObject.loadFromAssets(context, "shaders/starsprite.vert", "shaders/starsprite.frag", vertexFormat)
    }

    override fun cloudsShader(context: Context, vertexFormat: VertexFormat, textureCompressionMode: Flag<SettingsProvider.TextureCompressionMode>) : ProgramObject? {
        return ProgramObject.loadFromAssets(context, "shaders/cloudsprite.vert", "shaders/cloudsprite.frag", vertexFormat)
    }

    override fun hasBackground(): Boolean = true

    override fun hasClouds(): Boolean = true

    override fun hasStars(): Boolean = true
}