package drwdrd.ktdev.kengine

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder

class IndexBufferObject(val indicesFormat : IndicesFormat) {

    constructor(_indicesFormat: IndicesFormat, _indicesCount: Int) : this(_indicesFormat) {
        alloc(_indicesCount)
    }

    val bufferObject = StaticBuffer(BufferObject.Type.ElementArray)

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

    fun flush() {
        indexData.position(0)
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