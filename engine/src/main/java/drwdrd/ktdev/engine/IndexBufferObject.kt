package drwdrd.ktdev.engine

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder

class IndexBufferObject(_indicesFormat : IndicesFormat) {

    constructor(_indicesFormat: IndicesFormat, _indicesCount: Int) : this(_indicesFormat) {
        alloc(_indicesCount)
    }

    val bufferObject = StaticBuffer(BufferObject.Type.ElementArray)
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
        GLES20.glDrawElements(indicesFormat.layout.glMode, indicesCount, indicesFormat.type.glType, 0)
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

    fun create() {
        bufferObject.create(indexData)
    }

    fun destroy() {
        bufferObject.destroy()
    }

    fun bind() {
        bufferObject.bind()
    }

    fun release() {
        bufferObject.release()
    }

}