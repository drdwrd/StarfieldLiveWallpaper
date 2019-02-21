package drwdrd.ktdev.engine

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLES30
import java.io.*
import java.lang.Exception
import java.lang.RuntimeException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.max


const val HEADER_LENGTH = 12

class KTXLoaderException(msg : String) : RuntimeException(msg)

object KTXLoader {

    val headerSignature = byteArrayOf(
        0x0AB.toByte(),
        0x04B.toByte(),
        0x054.toByte(),
        0x058.toByte(),
        0x020.toByte(),
        0x031.toByte(),
        0x031.toByte(),
        0x0BB.toByte(),
        0x00D.toByte(),
        0x00A.toByte(),
        0x01A.toByte(),
        0x00A.toByte()
    )

    //TODO: add ktx compression support, right now this files cant be compressed in apk
    //TODO: clean up
    fun loadFromAssets(context : Context, name : String, level : Int, wrapModeS : Texture.WrapMode, wrapModeT : Texture.WrapMode, minFilter : Texture.Filtering, magFilter : Texture.Filtering) : Texture {
        val fd = context.assets.openFd(name)
        val byteArray = fd.createInputStream().readBytes()
        val buffer = ByteBuffer.wrap(byteArray)
        return load(buffer, level, wrapModeS, wrapModeT, minFilter, magFilter)

    }

    fun load(buffer : ByteBuffer, level : Int, wrapModeS : Texture.WrapMode, wrapModeT : Texture.WrapMode, minFilter : Texture.Filtering, magFilter : Texture.Filtering) : Texture {
        for( i in 0 until HEADER_LENGTH) {
            if(buffer.get() != headerSignature[i]) {
                throw KTXLoaderException("Invalid KTX file header")
            }
        }
        val defaultEndianness = buffer.order()
        val endianness = when(buffer.getInt()) {
            0x04030201 -> buffer.order()
            0x01020304 -> if(buffer.order() == ByteOrder.BIG_ENDIAN) ByteOrder.LITTLE_ENDIAN else ByteOrder.BIG_ENDIAN
            else -> throw KTXLoaderException("Invalid KTX file header")
        }
        buffer.order(endianness)
        val glType = buffer.getInt()
        val glTypeSize = buffer.getInt()
        val glFormat = buffer.getInt()

        if((glType != 0) or (glTypeSize != 1) or (glFormat != 0)) {
            throw KTXLoaderException("Unsupported texture type: glType = $glType, glTypeSize = $glTypeSize, glFormat = $glFormat")
        }

        val glInternalFormat = buffer.getInt()
        val glBaseInternalFormat = buffer.getInt()
        val pixelWidth = buffer.getInt()
        val pixelHeight = buffer.getInt()

        val pixelDepth = buffer.getInt()
        if(pixelDepth != 0) {
            KTXLoaderException("Unsupported texture type: pixelDepth = $pixelDepth")
        }

        val numberOfArrayElements = buffer.getInt()
        if(numberOfArrayElements != 0) {
            KTXLoaderException("Unsupported texture type: numberOfArrayElements = $numberOfArrayElements")
        }

        val numberOfFaces = buffer.getInt()
        if(numberOfFaces != 1) {
            KTXLoaderException("Unsupported texture type: numberOfFaces = $numberOfFaces")
        }

        val numberOfMipmapLevels = buffer.getInt()
        val bytesOfKeyValueData = buffer.getInt()

        Log.info("KTX Texture info : glInternalFormat = $glInternalFormat, glBaseInternalFormat = $glBaseInternalFormat, pixelWidth = $pixelWidth, pixelHeight = $pixelHeight, " +
                "numberOfMipmapLevels = $numberOfMipmapLevels, bytesOfKeyValueData = $bytesOfKeyValueData")

        buffer.position(buffer.position() + bytesOfKeyValueData)

        GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, 4)

        val texture = Texture()
        texture.create(Texture.Target.Texture2D)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(texture.target.glTarget, texture.glTextureId)
        GLES20.glTexParameteri(texture.target.glTarget, GLES20.GL_TEXTURE_WRAP_S, wrapModeS.glWrap)
        GLES20.glTexParameteri(texture.target.glTarget, GLES20.GL_TEXTURE_WRAP_T, wrapModeT.glWrap)
        GLES20.glTexParameteri(texture.target.glTarget, GLES20.GL_TEXTURE_MIN_FILTER, minFilter.glFilter)
        GLES20.glTexParameteri(texture.target.glTarget, GLES20.GL_TEXTURE_MAG_FILTER, magFilter.glFilter)

        for(mip in 0 until numberOfMipmapLevels) {
            buffer.order(endianness)
            val imageSize = buffer.getInt()
            buffer.order(defaultEndianness)
            val imagePadding = 4 - (imageSize and 0x03) and 0x03
            val width = max(1, pixelWidth shr mip)
            val height = max(1, pixelHeight shr mip)
            if(mip >= level) {
                val db = ByteBuffer.allocateDirect(imageSize)
                db.put(buffer.array(), buffer.position(), imageSize)
                db.rewind()
                GLES20.glCompressedTexImage2D(texture.target.glTarget, mip - level, glInternalFormat, width, height, 0, imageSize, db)
            }
            buffer.position(buffer.position() + imageSize + imagePadding)
        }

        GLES20.glBindTexture(Texture.Target.Texture2D.glTarget, 0)

        return texture
    }
}