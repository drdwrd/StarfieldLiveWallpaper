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

    fun loadFromPath(context: Context, path: String, level: Int, wrapModeS: Texture.WrapMode, wrapModeT: Texture.WrapMode, minFilter: Texture.Filtering, magFilter: Texture.Filtering) : Texture {
        val inputStreamSerializer = InputStreamSerializer(FileInputStream(path), ByteOrder.nativeOrder(), 8192)
        return load(inputStreamSerializer, level, wrapModeS, wrapModeT, minFilter, magFilter)
    }

    fun loadFromAssets(context : Context, name : String, level : Int, wrapModeS : Texture.WrapMode, wrapModeT : Texture.WrapMode, minFilter : Texture.Filtering, magFilter : Texture.Filtering) : Texture {
        val inputStreamSerializer = InputStreamSerializer(context.assets.open(name), ByteOrder.nativeOrder(), 8192)
        return load(inputStreamSerializer, level, wrapModeS, wrapModeT, minFilter, magFilter)

    }

    fun load(inputStream : InputStreamSerializer, level : Int, wrapModeS : Texture.WrapMode, wrapModeT : Texture.WrapMode, minFilter : Texture.Filtering, magFilter : Texture.Filtering) : Texture {
        for( i in 0 until HEADER_LENGTH) {
            if(inputStream.readByte() != headerSignature[i]) {
                throw KTXLoaderException("Invalid KTX file header")
            }
        }
        val defaultEndianness = inputStream.byteOrder
        val endianness = when(inputStream.readInt()) {
            0x04030201 -> inputStream.byteOrder
            0x01020304 -> if(inputStream.byteOrder == ByteOrder.BIG_ENDIAN) ByteOrder.LITTLE_ENDIAN else ByteOrder.BIG_ENDIAN
            else -> throw KTXLoaderException("Invalid KTX file header")
        }
        inputStream.byteOrder = endianness
        val glType = inputStream.readInt()
        val glTypeSize = inputStream.readInt()
        val glFormat = inputStream.readInt()

        if((glType != 0) or (glTypeSize != 1) or (glFormat != 0)) {
            throw KTXLoaderException("Unsupported texture type: glType = $glType, glTypeSize = $glTypeSize, glFormat = $glFormat")
        }

        val glInternalFormat = inputStream.readInt()
        val glBaseInternalFormat = inputStream.readInt()
        val pixelWidth = inputStream.readInt()
        val pixelHeight = inputStream.readInt()

        val pixelDepth = inputStream.readInt()
        if(pixelDepth != 0) {
            KTXLoaderException("Unsupported texture type: pixelDepth = $pixelDepth")
        }

        val numberOfArrayElements = inputStream.readInt()
        if(numberOfArrayElements != 0) {
            KTXLoaderException("Unsupported texture type: numberOfArrayElements = $numberOfArrayElements")
        }

        val numberOfFaces = inputStream.readInt()
        if(numberOfFaces != 1) {
            KTXLoaderException("Unsupported texture type: numberOfFaces = $numberOfFaces")
        }

        val numberOfMipmapLevels = inputStream.readInt()
        val bytesOfKeyValueData = inputStream.readInt()

        Log.info("drwdrd.ktdev.engine.KTXLoader","KTX Texture info : glInternalFormat = $glInternalFormat, glBaseInternalFormat = $glBaseInternalFormat, pixelWidth = $pixelWidth, pixelHeight = $pixelHeight, " +
                "numberOfMipmapLevels = $numberOfMipmapLevels, bytesOfKeyValueData = $bytesOfKeyValueData")

        inputStream.skip(bytesOfKeyValueData)

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
            inputStream.byteOrder = endianness
            val imageSize = inputStream.readInt()
            inputStream.byteOrder = defaultEndianness
            val imagePadding = 4 - (imageSize and 0x03) and 0x03
            val width = max(1, pixelWidth shr mip)
            val height = max(1, pixelHeight shr mip)
            if(mip >= level) {
                val db = ByteBuffer.allocateDirect(imageSize)
                inputStream.read(db, 0, imageSize)
                db.rewind()
                GLES20.glCompressedTexImage2D(texture.target.glTarget, mip - level, glInternalFormat, width, height, 0, imageSize, db)
            } else {
                inputStream.skip(imageSize)
            }
            inputStream.skip(imagePadding)
        }

        GLES20.glBindTexture(Texture.Target.Texture2D.glTarget, 0)

        return texture
    }
}