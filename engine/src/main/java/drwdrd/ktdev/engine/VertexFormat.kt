package drwdrd.ktdev.engine

import android.opengl.GLES20


class VertexFormat(_vertexLayout: Layout) {

    constructor() : this(Layout.Interleaved)

    val vertexLayout : Layout = _vertexLayout

    var vertexSize: Int = 0
        private set

    private val vertexFormat: Array<VertexAttributeInfo> = Array(VertexAttribute.VertexAttributesCount) { VertexAttributeInfo() }


    enum class VertexAttribute(val index: Int) {

        //special definitions
        VertexPosition(0),
        VertexColor(1),
        VertexNormal(2),
        TexCoordUV(3),

        //generic attributes
        VertexAttrib1(0),
        VertexAttrib2(1),
        VertexAttrib3(2),
        VertexAttrib4(3),
        VertexAttrib5(4),
        VertexAttrib6(5),
        VertexAttrib7(6),
        VertexAttrib8(7),
        VertexAttrib9(8),
        VertexAttrib10(9),
        VertexAttrib11(10),
        VertexAttrib12(11),
        VertexAttrib13(12),
        VertexAttrib14(13),
        VertexAttrib15(14),
        VertexAttrib16(15);

        companion object {
            const val VertexAttributesCount = 16
        }
    }

    enum class Type(val glType: kotlin.Int, val size: kotlin.Int) {

        Byte(GLES20.GL_BYTE, 1),
        UByte(GLES20.GL_UNSIGNED_BYTE, 1),
        Short(GLES20.GL_SHORT, 2),
        UShort(GLES20.GL_UNSIGNED_SHORT, 2),
        Int(GLES20.GL_INT, 4),
        UInt(GLES20.GL_UNSIGNED_INT, 4),
        Float(GLES20.GL_FLOAT, 4),
    }

    enum class Layout(val layout: Int) {
        Interleaved(1),
        Streamed(2);
    }

    class VertexAttributeInfo(_name: String, _index: Int, _type: Type, _size: Int, _normalized: Boolean) {

        constructor() : this("",-1, Type.Byte, 0, false)

        var name : String = _name
            internal set

        var index : Int = _index
            internal set

        var type : Type = _type
            internal set

        var size : Int = _size
            internal set

        var isNormalized : Boolean = _normalized
            internal set

        internal var offset : Int = 0

    }

    operator fun get(vertexAttribute: VertexAttribute) : VertexAttributeInfo {
        return vertexFormat[vertexAttribute.index]
    }

    operator fun set(vertexAttribute: VertexAttribute, vertexAttributeInfo: VertexAttributeInfo) {
        vertexFormat[vertexAttribute.index] = vertexAttributeInfo
        calculateVertexFormat()
    }

    operator fun iterator() : Iterator<VertexAttributeInfo> {
        return vertexFormat.iterator()
    }

    private fun calculateVertexFormat() {
        vertexSize = 0
        for (i in vertexFormat.indices) {
            vertexSize += vertexFormat[i].size * vertexFormat[i].type.size
            if (vertexFormat[i].size > 0) {
                var offset = 0
                for (j in vertexFormat.indices) {
                    if (vertexFormat[j].index < vertexFormat[i].index) {
                        offset += vertexFormat[j].size * vertexFormat[j].type.size
                    }
                }
                vertexFormat[i].offset = offset
            }
        }
    }
}