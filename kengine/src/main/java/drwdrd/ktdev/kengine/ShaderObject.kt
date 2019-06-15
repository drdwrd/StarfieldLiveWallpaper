package drwdrd.ktdev.kengine

import android.opengl.GLES20
import android.content.Context
import java.io.InputStream


class ShaderObject(val name : String, val type : ShaderType) {

    var glShaderId : Int = 0
        private set

    val isValid : Boolean
        get() = (glShaderId != 0)

    enum class ShaderType(val glType : Int) {
        VertexShader(GLES20.GL_VERTEX_SHADER),
        FragmentShader(GLES20.GL_FRAGMENT_SHADER);
    }

    fun create() {
        glShaderId = GLES20.glCreateShader(type.glType)
        check(glShaderId != 0) { "Cannot create ShaderObject" }
    }

    fun delete() {
        GLES20.glDeleteShader(glShaderId)
        glShaderId = 0
    }

    fun compile(source : String) : Boolean {
        GLES20.glShaderSource(glShaderId, source)
        GLES20.glCompileShader(glShaderId)
        val status = IntArray(1)
        GLES20.glGetShaderiv(glShaderId, GLES20.GL_COMPILE_STATUS, status, 0)
        if(status[0] == GLES20.GL_FALSE) {
            val info = GLES20.glGetShaderInfoLog(glShaderId)
            loge("Cannot compile shader: $info")
            return false
        }
        return true
    }

    companion object {

        fun loadFromSources(name : String, shaderType: ShaderType, source : String) : ShaderObject {
            val shader = ShaderObject(name, shaderType)
            shader.create()
            require(shader.isValid)
            if(!shader.compile(source)) {
                shader.delete()
            }
            return shader
       }

        fun loadFromAssets(context : Context, file : String, shaderType: ShaderType) : ShaderObject {
            val shader = ShaderObject(file, shaderType)
            shader.create()
            require(shader.isValid)
            val inputStream : InputStream = context.assets.open(file)
            val source = inputStream.bufferedReader().use { it.readText() }
            if(!shader.compile(source)) {
                shader.delete()
            }
            return shader
        }
    }
}
