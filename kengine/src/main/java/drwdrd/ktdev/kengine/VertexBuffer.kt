package drwdrd.ktdev.kengine

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder

class VertexBuffer(val vertexFormat : VertexFormat) {

    constructor(vertexFormat: VertexFormat, vertexCount : Int) : this(vertexFormat) {
        alloc(vertexCount)
    }

    var vertexCount : Int = 0
        private set

    lateinit var vertexData : ByteBuffer
        private set

    val vertexBufferSize : Int
        get() = vertexCount * vertexFormat.vertexSize


    fun alloc(vertexCount : Int) {
        this.vertexCount = vertexCount
        this.vertexData = ByteBuffer.allocateDirect(vertexBufferSize).order(ByteOrder.nativeOrder())
    }

    fun enableVertexArray() {
        var countMul = 1
        if (vertexFormat.vertexLayout == VertexFormat.Layout.Streamed) {
            countMul = vertexCount
        }
        for (vertexAttribute in vertexFormat) {
            if (vertexAttribute.size > 0 && vertexAttribute.index > -1) {
                vertexData.position(countMul * vertexAttribute.offset)
                GLES20.glVertexAttribPointer(vertexAttribute.index, vertexAttribute.size, vertexAttribute.type.glType, vertexAttribute.isNormalized, vertexFormat.vertexSize, vertexData)
                GLES20.glEnableVertexAttribArray(vertexAttribute.index)
            }
        }
    }

    fun disableVertexArray() {
        for (vertexAttribute in vertexFormat) {
            if (vertexAttribute.size > 0 && vertexAttribute.index > -1) {
                GLES20.glDisableVertexAttribArray(vertexAttribute.index)
            }
        }
    }

    fun flush() {
        vertexData.position(0)
    }
}
