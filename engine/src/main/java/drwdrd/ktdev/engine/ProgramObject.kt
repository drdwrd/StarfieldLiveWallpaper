package drwdrd.ktdev.engine

import android.content.Context
import android.opengl.GLES20


fun Array<vector2f>.toFloatArray() : FloatArray {
    var array = FloatArray(2 * this.size)
    for(index in this.indices) {
        array[2 * index] = this[index].x
        array[2 * index + 1] = this[index].y
    }
    return array
}


fun Array<vector3f>.toFloatArray() : FloatArray {
    var array = FloatArray(3 * this.size)
    for(index in this.indices) {
        array[3 * index] = this[index].x
        array[3 * index + 1] = this[index].y
        array[3 * index + 2] = this[index].z
    }
    return array
}

fun Array<vector4f>.toFloatArray() : FloatArray {
    var array = FloatArray(4 * this.size)
    for(index in this.indices) {
        array[4 * index] = this[index].x
        array[4 * index + 1] = this[index].y
        array[4 * index + 2] = this[index].z
        array[4 * index + 3] = this[index].w
    }
    return array
}


class ProgramObject(_name : String = "") {

    val name : String = _name

    var glProgramId : Int = 0
        private set

    val isValid : Boolean
        get() = (glProgramId != 0)

    fun create() {
        glProgramId = GLES20.glCreateProgram()
        check(glProgramId != 0) { "Cannot create ProgramObject" }
    }

    fun delete() {
        GLES20.glDeleteProgram(glProgramId)
        glProgramId = 0
    }

    fun bind() {
        GLES20.glUseProgram(glProgramId)
    }

    fun release() {
        GLES20.glUseProgram(0)
    }

    fun attach(shader : ShaderObject) {
        GLES20.glAttachShader(glProgramId, shader.glShaderId)
    }

    fun detach(shader : ShaderObject) {
        GLES20.glDetachShader(glProgramId, shader.glShaderId)
    }

    fun link() : Boolean {
        GLES20.glLinkProgram(glProgramId)
        val status = intArrayOf(1)
        GLES20.glGetProgramiv(glProgramId, GLES20.GL_LINK_STATUS, status, 0)
        if(status[0] == GLES20.GL_FALSE) {
            val info = GLES20.glGetProgramInfoLog(glProgramId)
            Log.error("Cannot link program: $info")
            return false
        }
        return true
    }

    fun bindVertexAttributeLocations(vertexFormat: VertexFormat) {
        for(vertexAttribute in vertexFormat) {
            if (vertexAttribute.size > 0 && vertexAttribute.index > -1) {
                GLES20.glBindAttribLocation(glProgramId, vertexAttribute.index, vertexAttribute.name)
            }
        }
    }

    fun setSampler(name : String, value : Int) {
        val location = GLES20.glGetUniformLocation(glProgramId, name)
        check(location != -1) { "Uniform $name not found in program this.$name" }
        GLES20.glUniform1i(location, value)
    }

    fun setUniformValue(name : String, value : Float) {
        val location = GLES20.glGetUniformLocation(glProgramId, name)
        check(location != -1) { "Uniform $name not found in program this.$name" }
        GLES20.glUniform1f(location, value)
    }

    fun setUniformValue(name : String, value : FloatArray) {
        val location = GLES20.glGetUniformLocation(glProgramId, name)
        check(location != -1) { "Uniform $name not found in program this.$name" }
        GLES20.glUniform1fv(location, value.size, value, 0)
    }

    fun setUniformValue(name : String, value : vector2f) {
        val location = GLES20.glGetUniformLocation(glProgramId, name)
        check(location != -1) { "Uniform $name not found in program this.$name" }
        GLES20.glUniform2fv(location, 1, value.toFloatArray(), 0)
    }

    fun setUniformValue(name : String, value : Array<vector2f>) {
        val location = GLES20.glGetUniformLocation(glProgramId, name)
        check(location != -1) { "Uniform $name not found in program this.$name" }
        var array = value.toFloatArray()
        GLES20.glUniform2fv(location, value.size, value.toFloatArray(), 0)
    }

    fun setUniformValue(name : String, value : vector3f) {
        val location = GLES20.glGetUniformLocation(glProgramId, name)
        check(location != -1) { "Uniform $name not found in program this.$name" }
        GLES20.glUniform3fv(location, 1, value.toFloatArray(), 0)
    }

    fun setUniformValue(name : String, value : Array<vector3f>) {
        val location = GLES20.glGetUniformLocation(glProgramId, name)
        check(location != -1) { "Uniform $name not found in program this.$name" }
        GLES20.glUniform3fv(location, value.size, value.toFloatArray(), 0)
    }

    fun setUniformValue(name : String, value : vector4f) {
        val location = GLES20.glGetUniformLocation(glProgramId, name)
        check(location != -1) { "Uniform $name not found in program this.$name" }
        GLES20.glUniform4fv(location, 1,value.toFloatArray(), 0)
    }

    fun setUniformValue(name : String, value : Array<vector4f>) {
        val location = GLES20.glGetUniformLocation(glProgramId, name)
        check(location != -1) { "Uniform $name not found in program this.$name" }
        GLES20.glUniform4fv(location, value.size, value.toFloatArray(), 0)
    }

    fun setUniformValue(name : String, value : matrix2f) {
        val location = GLES20.glGetUniformLocation(glProgramId, name)
        check(location != -1) { "Uniform $name not found in program this.$name" }
        GLES20.glUniformMatrix2fv(location, 1, false, value.toFloatArray(), 0)
    }

    fun setUniformValue(name : String, value : matrix3f) {
        val location = GLES20.glGetUniformLocation(glProgramId, name)
        check(location != -1) { "Uniform $name not found in program this.$name" }
        GLES20.glUniformMatrix3fv(location, 1, false, value.toFloatArray(), 0)
    }

    fun setUniformValue(name : String, value : matrix4f) {
        val location = GLES20.glGetUniformLocation(glProgramId, name)
        check(location != -1) { "Uniform $name not found in program this.$name" }
        GLES20.glUniformMatrix4fv(location, 1, false, value.toFloatArray(), 0)
    }

    companion object {

        fun load(vertexShader : ShaderObject, fragmentShader : ShaderObject, vertexFormat: VertexFormat) : ProgramObject {
            require(vertexShader.isValid)
            require(vertexShader.type == ShaderObject.ShaderType.VertexShader)
            require(fragmentShader.isValid)
            require(fragmentShader.type == ShaderObject.ShaderType.FragmentShader)
            val program = ProgramObject()
            program.create()
            require(program.isValid)
            program.bindVertexAttributeLocations(vertexFormat)
            program.attach(vertexShader)
            program.attach(fragmentShader)
            if(program.link()) {
                program.detach(vertexShader)
                program.detach(fragmentShader)
            } else {
                program.delete()
            }
            return program
        }

        fun loadFromSources(vertexShaderSource : String, fragmentShaderSource : String, vertexFormat: VertexFormat) : ProgramObject {
            val vertexShader = ShaderObject.loadFromSources("", ShaderObject.ShaderType.VertexShader, vertexShaderSource)
            require(vertexShader.isValid)
            val fragmentShader = ShaderObject.loadFromSources("", ShaderObject.ShaderType.FragmentShader, fragmentShaderSource)
            require(fragmentShader.isValid)
            val program = ProgramObject()
            program.create()
            require(program.isValid)
            program.bindVertexAttributeLocations(vertexFormat)
            program.attach(vertexShader)
            program.attach(fragmentShader)
            if(program.link()) {
                program.detach(vertexShader)
                program.detach(fragmentShader)
            } else {
                program.delete()
            }
            return program
        }

        fun loadFromAssets(context: Context, vertexShaderFile : String, fragmentShaderFile : String, vertexFormat: VertexFormat) : ProgramObject {
            val vertexShader = ShaderObject.loadFromAssets(context, vertexShaderFile, ShaderObject.ShaderType.VertexShader)
            val fragmentShader = ShaderObject.loadFromAssets(context, fragmentShaderFile, ShaderObject.ShaderType.FragmentShader)
            val program = ProgramObject()
            if(vertexShader.isValid && fragmentShader.isValid) {
                program.create()
                program.bindVertexAttributeLocations(vertexFormat)
                if(program.isValid) {
                    program.attach(vertexShader)
                    program.attach(fragmentShader)
                    if (program.link()) {
                        program.detach(vertexShader)
                        program.detach(fragmentShader)
                    } else {
                        program.delete()
                    }
                }
            }
            return program
        }
    }
}