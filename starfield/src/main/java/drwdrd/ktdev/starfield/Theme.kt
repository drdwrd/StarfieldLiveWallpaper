package drwdrd.ktdev.starfield

import android.content.Context
import drwdrd.ktdev.kengine.*
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.lang.Exception
import javax.xml.parsers.DocumentBuilderFactory

interface Theme {

    val backgroundScale : Float
    val starsParticleScale : Float
    val cloudsParticleScale : Float
    val cloudColors : Array<Long>

    val starsDensity : Double
    val cloudDensity : Double

    fun loadTheme(context: Context) : Boolean

    fun starfieldTexture(context : Context, textureQuality : Int, textureCompressionMode: Flag<TextureCompressionMode>) : Texture?
    fun starsTexture(context : Context, textureQuality : Int, textureCompressionMode: Flag<TextureCompressionMode>) : Texture?
    fun cloudsTexture(context : Context, textureQuality : Int, textureCompressionMode: Flag<TextureCompressionMode>) : Texture?
    fun starfieldShader(context: Context, vertexFormat: VertexFormat, textureCompressionMode: Flag<TextureCompressionMode>) : ProgramObject?
    fun starsShader(context: Context, vertexFormat: VertexFormat, textureCompressionMode: Flag<TextureCompressionMode>) : ProgramObject?
    fun cloudsShader(context: Context, vertexFormat: VertexFormat, textureCompressionMode: Flag<TextureCompressionMode>) : ProgramObject?
    fun hasClouds() : Boolean
    fun hasStars() : Boolean
    fun hasBackground() : Boolean
}

class ThemePackage(private val themeName : String) : Theme {

    private inner class ThemeTextureInfo(val name : String, format : String, val shader : String, val isValid : Boolean = true) {

        constructor() : this("null", "unknown", "unknown", false)

        val textureCompressionMode = parseTextureCompressionFormat(format)

        private fun parseTextureCompressionFormat(format : String) : TextureCompressionMode {
            return when(format) {
                "astc" -> TextureCompressionMode.ASTC
                "etc2" -> TextureCompressionMode.ETC2
                "etc" -> TextureCompressionMode.ETC1
                "png" -> TextureCompressionMode.NONE
                else -> TextureCompressionMode.UNKNOWN
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

    override var starsDensity: Double = 0.025
        private set

    override var cloudDensity: Double = 0.25
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
                            val shader = node.attributes.getNamedItem("shader")?.nodeValue ?: "starfield"
                            starfieldInfo = ThemeTextureInfo(name, format, shader)
                            backgroundScale = node.attributes.getNamedItem("scale")?.nodeValue?.toFloat() ?: 1.0f
                        }
                        "starsprites" -> {
                            val name = node.attributes.getNamedItem("name")?.nodeValue ?: return false
                            val format = node.attributes.getNamedItem("format")?.nodeValue ?: return false
                            val shader = node.attributes.getNamedItem("shader")?.nodeValue ?: "starsprite"
                            starsInfo = ThemeTextureInfo(name, format, shader)
                            starsDensity = node.attributes.getNamedItem("density")?.nodeValue?.toDouble() ?: 0.025
                            starsParticleScale = node.attributes.getNamedItem("scale")?.nodeValue?.toFloat() ?: 1.0f
                        }
                        "cloudsprites" -> {
                            val name = node.attributes.getNamedItem("name")?.nodeValue ?: return false
                            val format = node.attributes.getNamedItem("format")?.nodeValue ?: return false
                            val shader = node.attributes.getNamedItem("shader")?.nodeValue ?: "cloudsprite"
                            cloudsInfo = ThemeTextureInfo(name, format, shader)
                            cloudDensity = node.attributes.getNamedItem("density")?.nodeValue?.toDouble() ?: 0.25
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

    override fun starfieldTexture(context: Context, textureQuality: Int, textureCompressionMode: Flag<TextureCompressionMode>): Texture? {
        return when(starfieldInfo.textureCompressionMode) {
            TextureCompressionMode.NONE ->
                Texture.loadFromPath("$themePath/${starfieldInfo.name}", textureQuality, Texture.WrapMode.Repeat, Texture.WrapMode.Repeat,
                    Texture.Filtering.LinearMipmapLinear, Texture.Filtering.Linear)
            TextureCompressionMode.ASTC, TextureCompressionMode.ETC2, TextureCompressionMode.ETC1 ->
                KTXLoader.loadFromPath("$themePath/${starfieldInfo.name}", textureQuality, Texture.WrapMode.Repeat, Texture.WrapMode.Repeat,
                    Texture.Filtering.LinearMipmapLinear, Texture.Filtering.Linear)
            else -> null
        }
    }

    override fun starsTexture(context: Context, textureQuality: Int, textureCompressionMode: Flag<TextureCompressionMode>): Texture? {
        return when(starsInfo.textureCompressionMode) {
            TextureCompressionMode.NONE ->
                Texture.loadFromPath("$themePath/${starsInfo.name}", textureQuality, Texture.WrapMode.ClampToEdge, Texture.WrapMode.ClampToEdge,
                    Texture.Filtering.LinearMipmapLinear, Texture.Filtering.Linear)
            TextureCompressionMode.ASTC, TextureCompressionMode.ETC2, TextureCompressionMode.ETC1 ->
                KTXLoader.loadFromPath("$themePath/${starsInfo.name}", textureQuality, Texture.WrapMode.ClampToEdge, Texture.WrapMode.ClampToEdge,
                    Texture.Filtering.LinearMipmapLinear, Texture.Filtering.Linear)
            else -> null
        }
    }

    override fun cloudsTexture(context: Context, textureQuality: Int, textureCompressionMode: Flag<TextureCompressionMode>): Texture? {
        return when(cloudsInfo.textureCompressionMode) {
            TextureCompressionMode.NONE ->
                Texture.loadFromPath("$themePath/${cloudsInfo.name}", textureQuality, Texture.WrapMode.ClampToEdge, Texture.WrapMode.ClampToEdge,
                    Texture.Filtering.LinearMipmapLinear, Texture.Filtering.Linear)
            TextureCompressionMode.ASTC, TextureCompressionMode.ETC2, TextureCompressionMode.ETC1 ->
                KTXLoader.loadFromPath("$themePath/${cloudsInfo.name}", textureQuality, Texture.WrapMode.ClampToEdge, Texture.WrapMode.ClampToEdge,
                    Texture.Filtering.LinearMipmapLinear, Texture.Filtering.Linear)
            else -> null
        }
    }

    private fun resolveFragmentShaderName(shader : String, textureCompressionMode: Flag<TextureCompressionMode>, hasAlpha : Boolean = true) : String {
        return if(hasAlpha && textureCompressionMode.supportsAlpha()) {
            "shaders/${shader}_pm.frag"
        } else {
            "shaders/$shader.frag"
        }
    }

    override fun starfieldShader(context: Context, vertexFormat: VertexFormat, textureCompressionMode: Flag<TextureCompressionMode>) : ProgramObject? {
        val fragmentShader = resolveFragmentShaderName(starfieldInfo.shader, textureCompressionMode, false)
        return ProgramObject.loadFromAssets(context, "shaders/starfield.vert", fragmentShader, vertexFormat)
    }

    override fun starsShader(context: Context, vertexFormat: VertexFormat, textureCompressionMode: Flag<TextureCompressionMode>) : ProgramObject? {
        val fragmentShader = resolveFragmentShaderName(starsInfo.shader, textureCompressionMode)
        return ProgramObject.loadFromAssets(context, "shaders/starsprite.vert", fragmentShader, vertexFormat)
    }

    override fun cloudsShader(context: Context, vertexFormat: VertexFormat, textureCompressionMode: Flag<TextureCompressionMode>) : ProgramObject? {
        val fragmentShader = resolveFragmentShaderName(cloudsInfo.shader, textureCompressionMode)
        return ProgramObject.loadFromAssets(context, "shaders/cloudsprite.vert", fragmentShader, vertexFormat)
    }

    override fun hasBackground(): Boolean = starfieldInfo.isValid

    override fun hasStars(): Boolean = starsInfo.isValid

    override fun hasClouds(): Boolean = cloudsInfo.isValid
}

class TestTheme : Theme {

    override val backgroundScale: Float = 0.75f
    override val cloudsParticleScale: Float = 1.0f
    override val starsParticleScale: Float = 0.02f
    override val cloudColors: Array<Long> = arrayOf(0xff0b1d35, 0xff0b1d35)
    override val cloudDensity: Double = 0.5
    override val starsDensity: Double = 0.035

    override fun loadTheme(context: Context): Boolean {
        return true
    }

    override fun starfieldTexture(context: Context, textureQuality: Int, textureCompressionMode: Flag<TextureCompressionMode>): Texture? {
        return Texture.loadFromAssets2D(context,"themes/test/aurora.png", textureQuality, Texture.WrapMode.Repeat, Texture.WrapMode.Repeat,
            Texture.Filtering.LinearMipmapLinear, Texture.Filtering.Linear)
//        return null
    }

    override fun cloudsTexture(context: Context, textureQuality: Int, textureCompressionMode: Flag<TextureCompressionMode>): Texture? {
/*        return Texture.loadFromAssets2D(context,"themes/test/cloudsprites.png", textureQuality, Texture.WrapMode.ClampToEdge, Texture.WrapMode.ClampToEdge,
            Texture.Filtering.LinearMipmapLinear, Texture.Filtering.Linear)*/
        return null
    }

    override fun starsTexture(context: Context, textureQuality: Int, textureCompressionMode: Flag<TextureCompressionMode>): Texture? {
        return Texture.loadFromAssets2D(context,"themes/test/stars_atlas.png", textureQuality, Texture.WrapMode.ClampToEdge, Texture.WrapMode.ClampToEdge,
            Texture.Filtering.LinearMipmapLinear, Texture.Filtering.Linear)
    }

    override fun starfieldShader(context: Context, vertexFormat: VertexFormat, textureCompressionMode: Flag<TextureCompressionMode>) : ProgramObject? {
        return ProgramObject.loadFromAssets(context, "shaders/starfield.vert", "shaders/starfield2.frag", vertexFormat)
    }

    override fun starsShader(context: Context, vertexFormat: VertexFormat, textureCompressionMode: Flag<TextureCompressionMode>) : ProgramObject? {
        return ProgramObject.loadFromAssets(context, "shaders/starsprite.vert", "shaders/starsprite2.frag", vertexFormat)
    }

    override fun cloudsShader(context: Context, vertexFormat: VertexFormat, textureCompressionMode: Flag<TextureCompressionMode>) : ProgramObject? {
        return ProgramObject.loadFromAssets(context, "shaders/cloudsprite.vert", "shaders/cloudsprite.frag", vertexFormat)
    }

    override fun hasBackground(): Boolean = true

    override fun hasClouds(): Boolean = false

    override fun hasStars(): Boolean = true
}


class DefaultTheme : Theme {

    override val backgroundScale: Float = 1.0f
    override val cloudsParticleScale: Float = 2.5f
    override val starsParticleScale: Float = 0.07f

    override val cloudColors: Array<Long> = arrayOf(0xff0c134e, 0xff360e3a, 0xff70b3ff)

    override val starsDensity: Double = 0.075
    override val cloudDensity: Double = 0.8

    override fun loadTheme(context: Context): Boolean {
        return true
    }

    override fun starfieldTexture(context : Context, textureQuality : Int, textureCompressionMode: Flag<TextureCompressionMode>) : Texture? {
        return when {
            SettingsProvider.textureCompressionMode.hasFlag(TextureCompressionMode.ETC1) -> {
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
                loge("Unsupported texture compression format : ${textureCompressionMode.flags}")
                null
            }
        }
    }

    override fun starsTexture(context : Context, textureQuality : Int, textureCompressionMode: Flag<TextureCompressionMode>) : Texture? {
        return Texture.loadFromAssets2D(context,"themes/default/png/starsprites.png", textureQuality, Texture.WrapMode.ClampToEdge, Texture.WrapMode.ClampToEdge,
                    Texture.Filtering.LinearMipmapLinear, Texture.Filtering.Linear)
    }

    override fun cloudsTexture(context : Context, textureQuality : Int, textureCompressionMode: Flag<TextureCompressionMode>) : Texture {
        return Texture.loadFromAssets2D(context,"themes/default/png/cloudsprites.png", textureQuality, Texture.WrapMode.ClampToEdge, Texture.WrapMode.ClampToEdge,
                    Texture.Filtering.LinearMipmapLinear, Texture.Filtering.Linear)
    }

    override fun starfieldShader(context: Context, vertexFormat: VertexFormat, textureCompressionMode: Flag<TextureCompressionMode>) : ProgramObject? {
        return ProgramObject.loadFromAssets(context, "shaders/starfield.vert", "shaders/starfield.frag", vertexFormat)
    }

    override fun starsShader(context: Context, vertexFormat: VertexFormat, textureCompressionMode: Flag<TextureCompressionMode>) : ProgramObject? {
        return ProgramObject.loadFromAssets(context, "shaders/starsprite.vert", "shaders/starsprite.frag", vertexFormat)
    }

    override fun cloudsShader(context: Context, vertexFormat: VertexFormat, textureCompressionMode: Flag<TextureCompressionMode>) : ProgramObject? {
        return ProgramObject.loadFromAssets(context, "shaders/cloudsprite.vert", "shaders/cloudsprite.frag", vertexFormat)
    }

    override fun hasBackground(): Boolean = true

    override fun hasClouds(): Boolean = true

    override fun hasStars(): Boolean = true
}