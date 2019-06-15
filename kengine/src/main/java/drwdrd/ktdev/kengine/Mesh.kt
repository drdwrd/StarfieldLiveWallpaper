package drwdrd.ktdev.kengine


abstract class Mesh {

    abstract fun create()
    abstract fun destroy()
    abstract fun bind()
    abstract fun draw()
    abstract fun release()

}


class Plane2D : Mesh() {

    private lateinit var vertexBuffer : VertexBufferObject
    private lateinit var indexBuffer : IndexBufferObject

    val vertexFormat : VertexFormat
        get() = vertexBuffer.vertexFormat

    override fun create() {

        val vertexFormat = VertexFormat()
        vertexFormat[VertexFormat.VertexAttribute.VertexPosition] = VertexFormat.VertexAttributeInfo("position", 0, VertexFormat.Type.Float, 2, false)

        vertexBuffer = VertexBufferObject(vertexFormat)


        val plane = floatArrayOf(
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, 1.0f)

        vertexBuffer.alloc(4)
        vertexBuffer.vertexData.asFloatBuffer().put(plane)
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
        vertexBuffer.vertexData.asFloatBuffer().put(plane)
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

        vertexBuffer.alloc(48)
        val vertexDataBuffer = vertexBuffer.vertexData.asFloatBuffer()

        val p01 = vector3f(-1.0f, -1.0f, 0.0f)
        val uv01 = vector2f(0.0f, 0.0f)

        val p02 = vector3f(1.0f, -1.0f, 0.0f)
        val uv02 = vector2f(1.0f, 0.0f)

        val p03 = vector3f(-1.0f, 1.0f, 0.0f)
        val uv03 = vector2f(0.0f,1.0f)

        val p04 = vector3f(1.0f, 1.0f, 0.0f)
        val uv04 = vector2f(1.0f, 1.0f)

        val normal0 = vector3f(0.0f, 0.0f, 1.0f)

        for(i in 0 .. 3) {
            val rotationMatrix = matrix4f()
            rotationMatrix.loadIdentity()
            rotationMatrix.setAxisRotation(vector3f(0.0f, 1.0f, 0.0f), i * M_PI / 4.0f)

            val n = rotationMatrix.rotated(normal0)

            val p1 = rotationMatrix.rotated(p01)
            vertexDataBuffer.put(p1.toFloatArray())
            vertexDataBuffer.put(n.toFloatArray())
            vertexDataBuffer.put(uv01.toFloatArray())

            val p2 = rotationMatrix.rotated(p02)
            vertexDataBuffer.put(p2.toFloatArray())
            vertexDataBuffer.put(n.toFloatArray())
            vertexDataBuffer.put(uv02.toFloatArray())

            val p3 = rotationMatrix.rotated(p03)
            vertexDataBuffer.put(p3.toFloatArray())
            vertexDataBuffer.put(n.toFloatArray())
            vertexDataBuffer.put(uv03.toFloatArray())

            val p4 = rotationMatrix.rotated(p04)
            vertexDataBuffer.put(p4.toFloatArray())
            vertexDataBuffer.put(n.toFloatArray())
            vertexDataBuffer.put(uv04.toFloatArray())
        }

        val p11 = vector3f(-1.0f, 0.0f, -1.0f)
        val uv11 = vector2f(0.0f, 0.0f)

        val p12 = vector3f(-1.0f, 0.0f, 1.0f)
        val uv12 = vector2f(1.0f, 0.0f)

        val p13 = vector3f(1.0f, 0.0f, -1.0f)
        val uv13 = vector2f(0.0f,1.0f)

        val p14 = vector3f(1.0f, 0.0f, 1.0f)
        val uv14 = vector2f(1.0f, 1.0f)

        val normal1 = vector3f(0.0f, 1.0f, 0.0f)

        for(i in 0 .. 3) {
            val rotationMatrix = matrix4f()
            rotationMatrix.loadIdentity()
            rotationMatrix.setAxisRotation(vector3f(0.0f, 0.0f, 1.0f), i * M_PI / 4.0f)

            vertexDataBuffer.put(rotationMatrix.rotated(p11).toFloatArray())
            vertexDataBuffer.put(rotationMatrix.rotated(normal1).toFloatArray())
            vertexDataBuffer.put(uv11.toFloatArray())

            vertexDataBuffer.put(rotationMatrix.rotated(p12).toFloatArray())
            vertexDataBuffer.put(rotationMatrix.rotated(normal1).toFloatArray())
            vertexDataBuffer.put(uv12.toFloatArray())

            vertexDataBuffer.put(rotationMatrix.rotated(p13).toFloatArray())
            vertexDataBuffer.put(rotationMatrix.rotated(normal1).toFloatArray())
            vertexDataBuffer.put(uv13.toFloatArray())

            vertexDataBuffer.put(rotationMatrix.rotated(p14).toFloatArray())
            vertexDataBuffer.put(rotationMatrix.rotated(normal1).toFloatArray())
            vertexDataBuffer.put(uv14.toFloatArray())
        }

        val p21 = vector3f(0.0f, -1.0f, -1.0f)
        val uv21 = vector2f(0.0f, 0.0f)

        val p22 = vector3f(0.0f, 1.0f, -1.0f)
        val uv22 = vector2f(1.0f, 0.0f)

        val p23 = vector3f(0.0f, -1.0f, 1.0f)
        val uv23 = vector2f(0.0f,1.0f)

        val p24 = vector3f(0.0f, 1.0f, 1.0f)
        val uv24 = vector2f(1.0f, 1.0f)

        val normal2 = vector3f(1.0f, 0.0f, 0.0f)

        for(i in 0 .. 3) {
            val rotationMatrix = matrix4f()
            rotationMatrix.loadIdentity()
            rotationMatrix.setAxisRotation(vector3f(1.0f, 0.0f, 0.0f), i * M_PI / 4.0f)

            vertexDataBuffer.put(rotationMatrix.rotated(p21).toFloatArray())
            vertexDataBuffer.put(rotationMatrix.rotated(normal2).toFloatArray())
            vertexDataBuffer.put(uv21.toFloatArray())

            vertexDataBuffer.put(rotationMatrix.rotated(p22).toFloatArray())
            vertexDataBuffer.put(rotationMatrix.rotated(normal2).toFloatArray())
            vertexDataBuffer.put(uv22.toFloatArray())

            vertexDataBuffer.put(rotationMatrix.rotated(p23).toFloatArray())
            vertexDataBuffer.put(rotationMatrix.rotated(normal2).toFloatArray())
            vertexDataBuffer.put(uv23.toFloatArray())

            vertexDataBuffer.put(rotationMatrix.rotated(p24).toFloatArray())
            vertexDataBuffer.put(rotationMatrix.rotated(normal2).toFloatArray())
            vertexDataBuffer.put(uv24.toFloatArray())
        }

/*        val plane = floatArrayOf(
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
        vertexBuffer.put(plane)*/
        vertexBuffer.flush()
        vertexBuffer.create()

        val indicesFormat = IndicesFormat(IndicesFormat.Layout.Triangles, IndicesFormat.Type.UByte)

        indexBuffer = IndexBufferObject(indicesFormat)

/*        val indices = byteArrayOf(
            0, 1, 2,
            2, 1, 3,

            4, 5, 6,
            6, 5, 7,

            8, 9, 10,
            10, 9, 11)

        indexBuffer.alloc(18)
        indexBuffer.put(indices)*/

        indexBuffer.alloc(72)
        for(i in 0 .. 11) {
            val indices = byteArrayOf(
                (4 * i).toByte(), (4 * i + 1).toByte(), (4 * i + 2).toByte(),
                (4 * i + 2).toByte(), (4 * i + 1).toByte(), (4 * i + 3).toByte())
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

        val vertexDataBuffer = vertexBuffer.vertexData.asFloatBuffer()

        for(index in 0 until instanceCount) {

            val instanceId = index.toFloat()

            val plane = floatArrayOf(
                -1.0f, -1.0f, 0.0f, instanceId,
                1.0f, -1.0f, 0.0f, instanceId,
                -1.0f, 1.0f, 0.0f, instanceId,
                1.0f, 1.0f, 0.0f, instanceId)

            vertexDataBuffer.put(plane)
        }
        vertexBuffer.flush()
        vertexBuffer.create()

        val indicesFormat = IndicesFormat(IndicesFormat.Layout.Triangles, IndicesFormat.Type.UShort)

        indexBuffer = IndexBufferObject(indicesFormat)

        indexBuffer.alloc(6 * instanceCount)
        val indexDataBuffer = indexBuffer.indexData.asShortBuffer()

        for(index in 0 until instanceCount) {

            val x = 4 * index

            val indices = shortArrayOf(
                x.toShort(), (x + 1).toShort(), (x + 2).toShort(),
                (x + 2).toShort(), (x + 1).toShort(), (x + 3).toShort())

            indexDataBuffer.put(indices)
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


class Sphere3D(res : Int) : Mesh() {

    val meshResolution = res
    private lateinit var vertexBuffer : VertexBufferObject
    private lateinit var indexBuffer : IndexBufferObject

    val vertexFormat : VertexFormat
        get() = vertexBuffer.vertexFormat

    override fun create() {

        val vertexFormat = VertexFormat()
        vertexFormat[VertexFormat.VertexAttribute.VertexPosition] = VertexFormat.VertexAttributeInfo("position", 0, VertexFormat.Type.Float, 3, false)
        vertexFormat[VertexFormat.VertexAttribute.TexCoordUV] = VertexFormat.VertexAttributeInfo("uvCoord", 1, VertexFormat.Type.Float, 2, false)


        vertexBuffer = VertexBufferObject(vertexFormat)


        val d = 1.0f / (meshResolution - 1).toFloat()

        vertexBuffer.alloc(6 * meshResolution * meshResolution)
        val vertexDataBuffer = vertexBuffer.vertexData.asFloatBuffer()

        //px
        val pxFaceTransform = matrix3f(	0.0f,  0.0f,  1.0f,
                                        0.0f,  1.0f,  0.0f,
                                        1.0f,  0.0f,  0.0f)
        for(j in 0 until meshResolution) {
            for(i in 0 until meshResolution) {
                val q = 1.0f - 2.0f * i * d
                val p = 1.0f - 2.0f * j * d
                val r = 1.0f
                val pos = pxFaceTransform * vector3f(q, p, r).normalized()
                val uv = vector2f(1.0f - d * i,1.0f - d * j)
                vertexDataBuffer.put(pos.toFloatArray())
                vertexDataBuffer.put(uv.toFloatArray())
            }
        }

        //nx
        val nxFaceTransform = matrix3f(	0.0f,  0.0f, -1.0f,
                                        0.0f,  1.0f,  0.0f,
                                        -1.0f,  0.0f,  0.0f)

        for(j in 0 until meshResolution) {
            for(i in 0 until meshResolution) {
                val q = 1.0f - 2.0f * i * d
                val p = 1.0f - 2.0f * j * d
                val r = 1.0f
                val pos = nxFaceTransform * vector3f(q, p, r).normalized()
                val uv = vector2f(1.0f - d * i,1.0f - d * j)
                vertexDataBuffer.put(pos.toFloatArray())
                vertexDataBuffer.put(uv.toFloatArray())
            }
        }

        //py
        val pyFaceTransform = matrix3f(	-1.0f,  0.0f,  0.0f,
                                        0.0f,  0.0f, -1.0f,
                                        0.0f,  1.0f,  0.0f)

        for(j in 0 until meshResolution) {
            for(i in 0 until meshResolution) {
                val q = 1.0f - 2.0f * i * d
                val p = 1.0f - 2.0f * j * d
                val r = 1.0f
                val pos = pyFaceTransform * vector3f(q, p, r).normalized()
                val uv = vector2f(1.0f - d * j,1.0f - d * i)
                vertexDataBuffer.put(pos.toFloatArray())
                vertexDataBuffer.put(uv.toFloatArray())
            }
        }

        //ny
        val nyFaceTransform = matrix3f(	-1.0f,  0.0f,  0.0f,
                                        0.0f,  0.0f,  1.0f,
                                        0.0f, -1.0f,  0.0f)


        for(j in 0 until meshResolution) {
            for(i in 0 until meshResolution) {
                val q = 1.0f - 2.0f * i * d
                val p = 1.0f - 2.0f * j * d
                val r = 1.0f
                val pos = nyFaceTransform * vector3f(q, p, r).normalized()
                val uv = vector2f(1.0f - d * j,1.0f - d * i)
                vertexDataBuffer.put(pos.toFloatArray())
                vertexDataBuffer.put(uv.toFloatArray())
            }
        }

        //pz
        val pzFaceTransform = matrix3f(	-1.0f,  0.0f,  0.0f,
                                        0.0f,  1.0f,  0.0f,
                                        0.0f,  0.0f,  1.0f)

        for(j in 0 until meshResolution) {
            for(i in 0 until meshResolution) {
                val q = 1.0f - 2.0f * i * d
                val p = 1.0f - 2.0f * j * d
                val r = 1.0f
                val pos = pzFaceTransform * vector3f(q, p, r).normalized()
                val uv = vector2f(1.0f - d * i,1.0f - d * j)
                vertexDataBuffer.put(pos.toFloatArray())
                vertexDataBuffer.put(uv.toFloatArray())
            }
        }

        //nz
        val nzFaceTransform = matrix3f(	1.0f,  0.0f,  0.0f,
                                        0.0f,  1.0f,  0.0f,
                                        0.0f,  0.0f, -1.0f)

        for(j in 0 until meshResolution) {
            for(i in 0 until meshResolution) {
                val q = 1.0f - 2.0f * i * d
                val p = 1.0f - 2.0f * j * d
                val r = 1.0f
                val pos = nzFaceTransform * vector3f(q, p, r).normalized()
                val uv = vector2f(1.0f - d * i,1.0f - d * j)
                vertexDataBuffer.put(pos.toFloatArray())
                vertexDataBuffer.put(uv.toFloatArray())
            }
        }

        vertexBuffer.flush()
        vertexBuffer.create()

        val indicesFormat = IndicesFormat(IndicesFormat.Layout.Triangles, IndicesFormat.Type.UShort)

        indexBuffer = IndexBufferObject(indicesFormat)

        indexBuffer.alloc(36 * (meshResolution - 1) * (meshResolution - 1))
        val indexDataBuffer = indexBuffer.indexData.asShortBuffer()

        for (k in 0 .. 5) {
            var index = k * meshResolution * meshResolution
            for (j in 0 until meshResolution - 1) {
                for (i in 0 until meshResolution - 1) {
                    indexDataBuffer.put(index.toShort())
                    indexDataBuffer.put((index + 1).toShort())
                    indexDataBuffer.put((index + meshResolution).toShort())

                    indexDataBuffer.put((index + meshResolution).toShort())
                    indexDataBuffer.put((index + 1).toShort())
                    indexDataBuffer.put((index + meshResolution + 1).toShort())

                    index++
                }
                index++
            }
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