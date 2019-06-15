package drwdrd.ktdev.engine

import android.opengl.GLES20

object GLError {

    fun check() {
        val error = GLES20.glGetError()
        check(error == GLES20.GL_NO_ERROR) { getErrorMessage(error) }
    }

    private fun getErrorMessage(error : Int) : String {
        return when(error) {
            GLES20.GL_NO_ERROR -> "GL_NO_ERROR"
            GLES20.GL_INVALID_ENUM -> "GL_INVALID_ENUM"
            GLES20.GL_INVALID_VALUE -> "GL_INVALID_VALUE"
            GLES20.GL_INVALID_OPERATION -> "GL_INVALID_OPERATION"
            else -> "unknown error code: $error"
        }
    }
}