package drwdrd.ktdev.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils
import java.io.FileInputStream
import java.nio.ByteBuffer


class Texture {

    lateinit var target : Target
        private set
    lateinit var format : Format
        private set

    private val _glTextureId = intArrayOf(0)

    var wrapMode = arrayOf(WrapMode.ClampToEdge, WrapMode.ClampToEdge)
    var filtering = arrayOf(Filtering.LinearMipmapLinear, Filtering.Linear)
    var mipmappingEnabled = true

    val glTextureId
        get() = _glTextureId[0]

    val isValid
        get() = (_glTextureId[0] != 0)


    enum class Format(val glFormat : Int, val glType : Int, val typeSize : Int) {

        RGBA8(GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, 4),
        RGB8(GLES20.GL_RGB, GLES20.GL_UNSIGNED_BYTE, 3),
        RGBA4(GLES20.GL_RGBA, GLES20.GL_UNSIGNED_SHORT_4_4_4_4, 2),
        RGB5A1(GLES20.GL_RGBA, GLES20.GL_UNSIGNED_SHORT_5_5_5_1, 2),
        RGB565(GLES20.GL_RGB, GLES20.GL_UNSIGNED_SHORT_5_6_5, 2),
        LUMINANCE_ALPHA(GLES20.GL_LUMINANCE_ALPHA, GLES20.GL_UNSIGNED_BYTE, 2),
        LUMINANCE(GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, 1),
        ALPHA(GLES20.GL_ALPHA, GLES20.GL_UNSIGNED_BYTE, 1);
    }

    enum class Target(val glTarget : Int) {
        Texture2D(GLES20.GL_TEXTURE_2D),
        TextureCubemap(GLES20.GL_TEXTURE_CUBE_MAP);
    }

    enum class WrapMode(val glWrap : Int) {
        ClampToEdge(GLES20.GL_CLAMP_TO_EDGE),
        Repeat(GLES20.GL_REPEAT),
        MirroredRepeat(GLES20.GL_MIRRORED_REPEAT);
    }

    enum class Filtering(val glFilter : Int) {
        Nearest(GLES20.GL_NEAREST),
        Linear(GLES20.GL_LINEAR),
        NearestMipmapNearest(GLES20.GL_NEAREST_MIPMAP_NEAREST),
        NearestMipmapLinear(GLES20.GL_NEAREST_MIPMAP_LINEAR),
        LinearMipmapNearest(GLES20.GL_LINEAR_MIPMAP_NEAREST),
        LinearMipmapLinear(GLES20.GL_LINEAR_MIPMAP_LINEAR);
    }

    fun create() {
        GLES20.glGenTextures(1, _glTextureId, 0)
        check(glTextureId != 0) { "Cannot create TextureObject" }
    }

    fun create(target : Target) {
        this.target = target
        GLES20.glGenTextures(1, _glTextureId, 0)
        check(glTextureId != 0) { "Cannot create TextureObject" }
    }

    fun delete() {
        GLES20.glDeleteTextures(1, _glTextureId, 0)
        _glTextureId[0] = 0
    }

    fun bind(activeTexture : Int) {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + activeTexture)
        GLES20.glBindTexture(target.glTarget, glTextureId)
    }

    fun release(activeTexture: Int) {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + activeTexture)
        GLES20.glBindTexture(target.glTarget, 0)
    }

    private fun createTexture2D(_format : Format, width : Int, height : Int,  data : ByteBuffer) {
        check(data.remaining() >= width * height * _format.typeSize)
        target = Target.Texture2D
        format = _format
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(target.glTarget, _glTextureId[0])
        GLES20.glTexParameteri(target.glTarget, GLES20.GL_TEXTURE_WRAP_S, wrapMode[0].glWrap)
        GLES20.glTexParameteri(target.glTarget, GLES20.GL_TEXTURE_WRAP_T, wrapMode[1].glWrap)
        GLES20.glTexParameteri(target.glTarget, GLES20.GL_TEXTURE_MIN_FILTER, filtering[0].glFilter)
        GLES20.glTexParameteri(target.glTarget, GLES20.GL_TEXTURE_MAG_FILTER, filtering[1].glFilter)
        GLES20.glTexImage2D(target.glTarget, 0, _format.glFormat, width, height, 0, _format.glFormat, _format.glType, data)
        if(mipmappingEnabled) {
            GLES20.glGenerateMipmap(target.glTarget)
        }
        GLES20.glBindTexture(target.glTarget, 0)
    }

    private fun createTexture2D(bitmap : Bitmap) {
        target = Target.Texture2D

        val glFormat = GLUtils.getInternalFormat(bitmap)
        check(glFormat == GLES20.GL_RGBA || glFormat == GLES20.GL_RGB) { "Unsupported Bitmap pixel format!" }

        val glType = GLUtils.getType(bitmap)
        check(glType == GLES20.GL_UNSIGNED_BYTE) { "Unsupported Bitmap pixel type!" }


        when(glType) {
        GLES20.GL_UNSIGNED_BYTE -> when(glFormat) {
            GLES20.GL_RGBA -> format = Format.RGBA8
            GLES20.GL_RGB -> format = Format.RGB8
            }
        }

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(target.glTarget, _glTextureId[0])
        GLES20.glTexParameteri(target.glTarget, GLES20.GL_TEXTURE_WRAP_S, wrapMode[0].glWrap)
        GLES20.glTexParameteri(target.glTarget, GLES20.GL_TEXTURE_WRAP_T, wrapMode[1].glWrap)
        GLES20.glTexParameteri(target.glTarget, GLES20.GL_TEXTURE_MIN_FILTER, filtering[0].glFilter)
        GLES20.glTexParameteri(target.glTarget, GLES20.GL_TEXTURE_MAG_FILTER, filtering[1].glFilter)
        GLUtils.texImage2D(target.glTarget, 0, glFormat, bitmap, glType, 0)
        if(mipmappingEnabled) {
            GLES20.glGenerateMipmap(target.glTarget)
        }
        GLES20.glBindTexture(target.glTarget, 0)
    }

    private fun createCubemapFace(face : Int, bitmap : Bitmap) {
        val glFormat = GLUtils.getInternalFormat(bitmap)
        check(glFormat == GLES20.GL_RGBA || glFormat == GLES20.GL_RGB) { "Unsupported Bitmap pixel format!" }

        val glType = GLUtils.getType(bitmap)
        check(glType == GLES20.GL_UNSIGNED_BYTE) { "Unsupported Bitmap pixel type!" }


        when (glType) {
            GLES20.GL_UNSIGNED_BYTE -> when (glFormat) {
                GLES20.GL_RGBA -> format = Format.RGBA8
                GLES20.GL_RGB -> format = Format.RGB8
            }
        }
        GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_X + face, 0, glFormat, bitmap, glType, 0)
    }

    companion object {

        fun emptyTexture2D() : Texture {
            val texture = Texture()
            texture.create()
            texture.target = Target.Texture2D
            return texture
        }

        fun loadFromAssetsCubemap(context : Context, name : Array<String>, level : Int, wrapModeS : WrapMode, wrapModeT : WrapMode, minFilter : Filtering, magFilter : Filtering) : Texture {
            val texture = Texture()
            texture.create()
            texture.wrapMode = arrayOf(wrapModeS, wrapModeT)
            texture.filtering = arrayOf(minFilter, magFilter)
            texture.mipmappingEnabled = when(minFilter) {
                Filtering.Linear -> false
                Filtering.Nearest -> false
                else -> true

            }

            texture.target = Target.TextureCubemap
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            GLES20.glBindTexture(texture.target.glTarget, texture.glTextureId)
            GLES20.glTexParameteri(texture.target.glTarget, GLES20.GL_TEXTURE_WRAP_S, texture.wrapMode[0].glWrap)
            GLES20.glTexParameteri(texture.target.glTarget, GLES20.GL_TEXTURE_WRAP_T, texture.wrapMode[1].glWrap)
            GLES20.glTexParameteri(texture.target.glTarget, GLES20.GL_TEXTURE_MIN_FILTER, texture.filtering[0].glFilter)
            GLES20.glTexParameteri(texture.target.glTarget, GLES20.GL_TEXTURE_MAG_FILTER, texture.filtering[1].glFilter)

            for(i in 0 .. 5) {
                val inputStream = context.assets.open(name[i])
                val bitmap = inputStream.use { BitmapFactory.decodeStream(it) }
                if(level > 0) {
                    val w = bitmap.width shr level
                    val h = bitmap.height shr level
                    val b = Bitmap.createScaledBitmap(bitmap, w, h, true)
                    texture.createCubemapFace(i, b)
                    b.recycle()
                } else {
                    texture.createCubemapFace(i, bitmap)
                }
                bitmap.recycle()


            }
            if(texture.mipmappingEnabled) {
                GLES20.glGenerateMipmap(texture.target.glTarget)
            }
            GLES20.glBindTexture(texture.target.glTarget, 0)
            return texture
        }

        fun loadFromPath(path : String, level : Int, wrapModeS : WrapMode, wrapModeT : WrapMode, minFilter : Filtering, magFilter : Filtering) : Texture {
            val texture = Texture()
            texture.create()
            texture.wrapMode = arrayOf(wrapModeS, wrapModeT)
            texture.filtering = arrayOf(minFilter, magFilter)
            texture.mipmappingEnabled = when(minFilter) {
                Filtering.Linear -> false
                Filtering.Nearest -> false
                else -> true

            }
            val inputStream = FileInputStream(path)
            val bitmap = inputStream.use { BitmapFactory.decodeStream(it) }
            if(level > 0) {
                val w = bitmap.width shr level
                val h = bitmap.height shr level
                val b = Bitmap.createScaledBitmap(bitmap, w, h, true)
                texture.createTexture2D(b)
                b.recycle()
            } else {
                texture.createTexture2D(bitmap)
            }
            bitmap.recycle()
            return texture
        }

        fun loadFromAssets2D(context : Context, name : String, level : Int, wrapModeS : WrapMode, wrapModeT : WrapMode, minFilter : Filtering, magFilter : Filtering) : Texture {
            val texture = Texture()
            texture.create()
            texture.wrapMode = arrayOf(wrapModeS, wrapModeT)
            texture.filtering = arrayOf(minFilter, magFilter)
            texture.mipmappingEnabled = when(minFilter) {
                Filtering.Linear -> false
                Filtering.Nearest -> false
                else -> true

            }
            val inputStream = context.assets.open(name)
            val bitmap = inputStream.use { BitmapFactory.decodeStream(it) }
            if(level > 0) {
                val w = bitmap.width shr level
                val h = bitmap.height shr level
                val b = Bitmap.createScaledBitmap(bitmap, w, h, true)
                texture.createTexture2D(b)
                b.recycle()
            } else {
                texture.createTexture2D(bitmap)
            }
            bitmap.recycle()
            return texture
        }
    }

}
