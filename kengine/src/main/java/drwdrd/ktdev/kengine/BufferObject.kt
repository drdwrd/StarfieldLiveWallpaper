package drwdrd.ktdev.kengine

import android.opengl.GLES20
import java.nio.ByteBuffer

open class BufferObject(val type : Type, val usage : Usage) {

    private val _glBufferId = intArrayOf(0)

    val glBufferId
        get() = _glBufferId[0]

    val isValid
        get() = (_glBufferId[0] != 0)


    enum class Type(val target : Int) {
        VertexArray(GLES20.GL_ARRAY_BUFFER),
        ElementArray(GLES20.GL_ELEMENT_ARRAY_BUFFER);
    }

    enum class Usage(val usage : Int) {
        Static(GLES20.GL_STATIC_DRAW),
        Dynamic(GLES20.GL_DYNAMIC_DRAW);
    }

    fun create() {
        GLES20.glGenBuffers(1, _glBufferId, 0)
        check(glBufferId != 0) { "Cannot create BufferObject" }
    }

    fun destroy() {
        GLES20.glDeleteBuffers(1, _glBufferId, 0)
        _glBufferId[0] = 0
    }

    fun bind() {
        GLES20.glBindBuffer(type.target, glBufferId)
    }

    fun release() {
        GLES20.glBindBuffer(type.target, 0)
    }
}


class StaticBuffer(_type : Type) : BufferObject(_type, Usage.Static) {

    var size : Int = 0
        private set


    fun create(_size : Int) {
        size = _size
        super.create()
        super.bind()
        GLES20.glBufferData(type.target, size, null, usage.usage)
    }

    fun create(data : ByteBuffer) {
        size = data.capacity()
        super.create()
        super.bind()
        GLES20.glBufferData(type.target, data.capacity(), data, usage.usage)
    }

}