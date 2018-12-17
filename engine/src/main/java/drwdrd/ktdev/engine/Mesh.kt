package drwdrd.ktdev.engine


abstract class Mesh {

    abstract fun create()
    abstract fun destroy()
    abstract fun bind()
    abstract fun draw()
    abstract fun release()

}


class SimplePlane : Mesh() {

    private lateinit var vertexBuffer : VertexBuffer
    private lateinit var indexBuffer : IndexBuffer

    val vertexFormat : VertexFormat
        get() = vertexBuffer.vertexFormat

    override fun create() {

        val vertexFormat = VertexFormat()
        vertexFormat[VertexFormat.VertexAttribute.VertexPosition] = VertexFormat.VertexAttributeInfo("position", 0, VertexFormat.Type.Float, 2, false)

        vertexBuffer = VertexBuffer(vertexFormat)


        val plane = floatArrayOf(
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, 1.0f)

        vertexBuffer.alloc(4)
        vertexBuffer.put(plane)
        vertexBuffer.flush()

        val indicesFormat = IndicesFormat(IndicesFormat.Layout.Triangles, IndicesFormat.Type.UByte)

        indexBuffer = IndexBuffer(indicesFormat)

        val indices = byteArrayOf(
            0, 1, 2,
            2, 1, 3)

        indexBuffer.alloc(6)
        indexBuffer.put(indices)
        indexBuffer.flush()

    }

    override fun destroy() {

    }

    override fun draw() {
        indexBuffer.drawElements()
    }

    override fun bind() {
        vertexBuffer.enableVertexArray()
    }

    override fun release() {
        vertexBuffer.disableVertexArray()
    }
}

class Plane3D : Mesh() {

    private lateinit var vertexBuffer : VertexBufferObject
    private lateinit var indexBuffer : IndexBufferObject

    val vertexFormat : VertexFormat
        get() = vertexBuffer.vertexFormat

    override fun create() {

        val vertexFormat = VertexFormat()
        vertexFormat[VertexFormat.VertexAttribute.VertexPosition] = VertexFormat.VertexAttributeInfo("position", 0, VertexFormat.Type.Float, 3, false)
        vertexFormat[VertexFormat.VertexAttribute.TexCoordUV] = VertexFormat.VertexAttributeInfo("uvCoord", 1, VertexFormat.Type.Float, 2, false)


        vertexBuffer = VertexBufferObject(vertexFormat)


        val plane = floatArrayOf(
            -1.0f, -1.0f, 0.0f, 0.0f, 0.0f,
            1.0f, -1.0f, 0.0f, 1.0f, 0.0f,
            -1.0f, 1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f, 1.0f)


        vertexBuffer.alloc(4)
        vertexBuffer.put(plane)
        vertexBuffer.flush()
        vertexBuffer.create()

        val indicesFormat = IndicesFormat(IndicesFormat.Layout.Triangles, IndicesFormat.Type.UByte)

        indexBuffer = IndexBufferObject(indicesFormat)

        val indices = byteArrayOf(
            0, 1, 2,
            2, 1, 3)

        indexBuffer.alloc(6)
        indexBuffer.put(indices)
        indexBuffer.flush()
        indexBuffer.create()

    }

    override fun destroy() {
        vertexBuffer.destroy()
        indexBuffer.destroy()
    }

    override fun draw() {
        indexBuffer.drawElements()
    }

    override fun bind() {
        vertexBuffer.bind()
        indexBuffer.bind()
        vertexBuffer.enableVertexArray()
    }

    override fun release() {
        vertexBuffer.disableVertexArray()
        indexBuffer.release()
        vertexBuffer.release()
    }
}

class Sprite3D : Mesh() {

    private lateinit var vertexBuffer : VertexBufferObject
    private lateinit var indexBuffer : IndexBufferObject

    val vertexFormat : VertexFormat
        get() = vertexBuffer.vertexFormat

    override fun create() {

        val vertexFormat = VertexFormat()
        vertexFormat[VertexFormat.VertexAttribute.VertexPosition] = VertexFormat.VertexAttributeInfo("position", 0, VertexFormat.Type.Float, 3, false)
        vertexFormat[VertexFormat.VertexAttribute.VertexNormal] = VertexFormat.VertexAttributeInfo("normal", 1, VertexFormat.Type.Float, 3, false)
        vertexFormat[VertexFormat.VertexAttribute.TexCoordUV] = VertexFormat.VertexAttributeInfo("uvCoord", 2, VertexFormat.Type.Float, 2, false)


        vertexBuffer = VertexBufferObject(vertexFormat)


        val plane = floatArrayOf(
            -1.0f, -1.0f, 0.0f,     0.0f, 0.0f, 1.0f,       0.0f, 0.0f,
            1.0f, -1.0f, 0.0f,      0.0f, 0.0f, 1.0f,       1.0f, 0.0f,
            -1.0f, 1.0f, 0.0f,      0.0f, 0.0f, 1.0f,       0.0f, 1.0f,
            1.0f, 1.0f, 0.0f,       0.0f, 0.0f, 1.0f,       1.0f, 1.0f,

            -1.0f, 0.0f, -1.0f,     0.0f, 1.0f, 0.0f,       0.0f, 0.0f,
            -1.0f, 0.0f, 1.0f,      0.0f, 1.0f, 0.0f,       1.0f, 0.0f,
            1.0f, 0.0f, -1.0f,      0.0f, 1.0f, 0.0f,       0.0f, 1.0f,
            1.0f, 0.0f, 1.0f,       0.0f, 1.0f, 0.0f,       1.0f, 1.0f,

            0.0f, -1.0f, -1.0f,     1.0f, 0.0f, 0.0f,       0.0f, 0.0f,
            0.0f, 1.0f, -1.0f,      1.0f, 0.0f, 0.0f,       1.0f, 0.0f,
            0.0f, -1.0f, 1.0f,      1.0f, 0.0f, 0.0f,       0.0f, 1.0f,
            0.0f, 1.0f, 1.0f,       1.0f, 0.0f, 0.0f,       1.0f, 1.0f)


        vertexBuffer.alloc(12)
        vertexBuffer.put(plane)
        vertexBuffer.flush()
        vertexBuffer.create()

        val indicesFormat = IndicesFormat(IndicesFormat.Layout.Triangles, IndicesFormat.Type.UByte)

        indexBuffer = IndexBufferObject(indicesFormat)

        val indices = byteArrayOf(
            0, 1, 2,
            2, 1, 3,

            4, 5, 6,
            6, 5, 7,

            8, 9, 10,
            10, 9, 11)

        indexBuffer.alloc(18)
        indexBuffer.put(indices)
        indexBuffer.flush()
        indexBuffer.create()

    }

    override fun destroy() {
        vertexBuffer.destroy()
        indexBuffer.destroy()
    }

    override fun draw() {
        indexBuffer.drawElements()
    }

    override fun bind() {
        vertexBuffer.bind()
        indexBuffer.bind()
        vertexBuffer.enableVertexArray()
    }

    override fun release() {
        vertexBuffer.disableVertexArray()
        indexBuffer.release()
        vertexBuffer.release()
    }
}


class InstancedPlane3D(_instanceCount : Int) : Mesh() {

    private lateinit var vertexBuffer : VertexBufferObject
    private lateinit var indexBuffer : IndexBufferObject

    val instanceCount = _instanceCount

    val vertexFormat : VertexFormat
        get() = vertexBuffer.vertexFormat

    override fun create() {

        val vertexFormat = VertexFormat()
        vertexFormat[VertexFormat.VertexAttribute.VertexPosition] = VertexFormat.VertexAttributeInfo("position", 0, VertexFormat.Type.Float, 3, false)
        vertexFormat[VertexFormat.VertexAttribute.VertexAttrib2] = VertexFormat.VertexAttributeInfo("instanceId", 1, VertexFormat.Type.Float, 1,false)

        vertexBuffer = VertexBufferObject(vertexFormat)



        vertexBuffer.alloc(4 * instanceCount)
        for(index in 0 until instanceCount) {

            val instanceId = index.toFloat()

            val plane = floatArrayOf(
                -1.0f, -1.0f, 0.0f, instanceId,
                1.0f, -1.0f, 0.0f, instanceId,
                -1.0f, 1.0f, 0.0f, instanceId,
                1.0f, 1.0f, 0.0f, instanceId)

            vertexBuffer.put(plane)
        }
        vertexBuffer.flush()
        vertexBuffer.create()

        val indicesFormat = IndicesFormat(IndicesFormat.Layout.Triangles, IndicesFormat.Type.UShort)

        indexBuffer = IndexBufferObject(indicesFormat)

        indexBuffer.alloc(6 * instanceCount)
        for(index in 0 until instanceCount) {

            val x = 4 * index

            val indices = shortArrayOf(
                x.toShort(), (x + 1).toShort(), (x + 2).toShort(),
                (x + 2).toShort(), (x + 1).toShort(), (x + 3).toShort())

            indexBuffer.put(indices)
        }
        indexBuffer.flush()
        indexBuffer.create()

    }

    override fun destroy() {
        vertexBuffer.destroy()
        indexBuffer.destroy()
    }

    override fun draw() {
        indexBuffer.drawElements()
    }

    override fun bind() {
        vertexBuffer.bind()
        indexBuffer.bind()
        vertexBuffer.enableVertexArray()
    }

    override fun release() {
        vertexBuffer.disableVertexArray()
        indexBuffer.release()
        vertexBuffer.release()
    }
}
