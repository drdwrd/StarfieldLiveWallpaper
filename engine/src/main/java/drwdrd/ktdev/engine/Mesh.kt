package drwdrd.ktdev.engine


abstract class Mesh {

    abstract fun create()
    abstract fun destroy()
    abstract fun draw()

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

        vertexBuffer.enableVertexArray()
        indexBuffer.drawElements()
        vertexBuffer.disableVertexArray()

    }
}

class Plane3D : Mesh() {

    private lateinit var vertexBuffer : VertexBuffer
    private lateinit var indexBuffer : IndexBuffer

    val vertexFormat : VertexFormat
        get() = vertexBuffer.vertexFormat

    override fun create() {

        val vertexFormat = VertexFormat()
        vertexFormat[VertexFormat.VertexAttribute.VertexPosition] = VertexFormat.VertexAttributeInfo("position", 0, VertexFormat.Type.Float, 3, false)

        vertexBuffer = VertexBuffer(vertexFormat)


        val plane = floatArrayOf(
            -1.0f, -1.0f, 0.0f,
            1.0f, -1.0f, 0.0f,
            -1.0f, 1.0f, 0.0f,
            1.0f, 1.0f, 0.0f)

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

        vertexBuffer.enableVertexArray()
        indexBuffer.drawElements()
        vertexBuffer.disableVertexArray()

    }
}