package drwdrd.ktdev.kengine

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder

class IndexBuffer(val indicesFormat : IndicesFormat) {

    constructor(indicesFormat: IndicesFormat, indicesCount: Int) : this(indicesFormat) {
        alloc(indicesCount)
    }

    var indicesCount : Int = 0
        private set

    lateinit var indexData : ByteBuffer
        private set

    val indexBufferSize : Int
        get() = indicesCount * indicesFormat.type.size


    fun alloc(indicesCount : Int) {
        this.indicesCount = indicesCount
        this.indexData = ByteBuffer.allocateDirect(indexBufferSize).order(ByteOrder.nativeOrder())
    }

    fun drawElements() {
        GLES20.glDrawElements(indicesFormat.layout.glMode, indicesCount, indicesFormat.type.glType, indexData)
    }

    fun put(data : ByteArray) {
        indexData.put(data)
    }

    fun flush() {
        indexData.position(0)
    }
}
