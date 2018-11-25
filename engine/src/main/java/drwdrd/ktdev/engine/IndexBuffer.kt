package drwdrd.ktdev.engine

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder

class IndexBuffer(_indicesFormat : IndicesFormat) {

    constructor(_indicesFormat: IndicesFormat, _indicesCount: Int) : this(_indicesFormat) {
        alloc(_indicesCount)
    }

    val indicesFormat: IndicesFormat = _indicesFormat

    var indicesCount : Int = 0
        private set

    lateinit var indexData : ByteBuffer
        private set

    val indexBufferSize : Int
        get() = indicesCount * indicesFormat.type.size


    fun alloc(_indicesCount : Int) {
        indicesCount = _indicesCount
        indexData = ByteBuffer.allocateDirect(indexBufferSize).order(ByteOrder.nativeOrder())
    }

    fun drawElements() {
        GLES20.glDrawElements(indicesFormat.layout.glMode, indicesCount, indicesFormat.type.glType, indexData)
    }

    fun put(data : ByteArray) {
        indexData.put(data)
    }

    fun put(data : ShortArray) {
        indexData.asShortBuffer().put(data)
    }

    fun put(data : IntArray) {
        indexData.asIntBuffer().put(data)
    }

    fun flush() {
        indexData.rewind()
    }
}