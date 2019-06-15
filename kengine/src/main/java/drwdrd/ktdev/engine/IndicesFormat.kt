package drwdrd.ktdev.engine

import android.opengl.GLES20


class IndicesFormat(val layout : Layout, val type : Type) {

    enum class Type(val glType: Int, val size : Int) {

        UByte(GLES20.GL_UNSIGNED_BYTE, 1),
        UShort(GLES20.GL_UNSIGNED_SHORT, 2),
        UInt(GLES20.GL_UNSIGNED_INT, 4);

    }

    enum class Layout(val glMode: Int) {

        Points(GLES20.GL_POINTS),
        LineStrips(GLES20.GL_LINE_STRIP),
        LineLoops(GLES20.GL_LINE_LOOP),
        Lines(GLES20.GL_LINES),
        TriangleStrips(GLES20.GL_TRIANGLE_STRIP),
        TriangleFan(GLES20.GL_TRIANGLE_FAN),
        Triangles(GLES20.GL_TRIANGLES);

    }
}