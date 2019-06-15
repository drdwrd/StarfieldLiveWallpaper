package drwdrd.ktdev.engine

import android.content.Context
import android.opengl.GLES20


fun Array<vector2f>.toFloatArray() : FloatArray {
    val array = FloatArray(2 * this.size)
    for(index in this.indices) {
        this[index].put(array, 2 * index)
    }
    return array
}


fun Array<vector3f>.toFloatArray() : FloatArray {
    val array = FloatArray(3 * this.size)
    for(index in this.indices) {
        this[index].put(array, 3 * index)
    }
    return array
}

fun Array<vector4f>.toFloatArray() : FloatArray {
    val array = FloatArray(4 * this.size)
    for(index in this.indices) {
        this[index].put(array, 4 * index)
    }
    return array
}

fun Array<quaternion>.toFloatArray() : FloatArray {
    val array = FloatArray(4 * this.size)
    for(index in this.indices) {
        this[index].put(array, 4 * index)
    }
    return array
}

fun Array<matrix2f>.toFloatArray() : FloatArray {
    val array = FloatArray(4 * this.size)
    for(index in this.indices) {
        this[index].put(array, 4 * index)
    }
    return array
}

fun Array<matrix3f>.toFloatArray() : FloatArray {
    val array = FloatArray(12 * this.size)
    for(index in this.indices) {
        this[index].put(array, 12 * index)
    }
    return array
}

fun Array<matrix4f>.toFloatArray() : FloatArray {
    val array = FloatArray(16 * this.size)
    for(index in this.indices) {
        this[index].put(array, 16 * index)
    }
    return array
}


class Uniform() {

    constructor(_name : String, _location : Int) : this() {
        name = _name
        location = _location
    }

    var name = ""
        private set

    var location = -1
        private set
}

class ProgramObject(val name : String = "") {

    private val uniformMap = hashMapOf<String, Int>()
    private lateinit var uniforms : Array<Uniform>
    private var glProgramId : Int = 0

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
            loge("Cannot link program: $info")
            return false
        }
        val count = intArrayOf(1)
        GLES20.glGetProgramiv(glProgramId, GLES20.GL_ACTIVE_UNIFORMS, count, 0)
        val buf = intArrayOf(2)
        for(index in 0 until count[0]) {
            val name = GLES20.glGetActiveUniform(glProgramId, index, buf, 0, buf, 1)
            uniformMap[name] = index
        }
        uniforms = Array(16) { Uniform() }
        return true
    }

    fun registerUniform(name : String, id : Int) {
        uniforms[id] = Uniform(name, uniformMap[name] ?: -1)
    }

    fun bindVertexAttributeLocations(vertexFormat: VertexFormat) {
        for(vertexAttribute in vertexFormat) {
            if (vertexAttribute.size > 0 && vertexAttribute.index > -1) {
                GLES20.glBindAttribLocation(glProgramId, vertexAttribute.index, vertexAttribute.name)
            }
        }
    }

    fun setSampler(name : String, value : Int) {
        val location = uniformMap[name]
        check(location != null) { "Uniform $name not found in program this.$name" }
        GLES20.glUniform1i(location, value)
    }

    fun setSampler(id : Int, value : Int) {
        GLES20.glUniform1i(uniforms[id].location, value)
    }

    fun setUniformValue(name : String, value : Float) {
        val location = uniformMap[name]
        check(location != null) { "Uniform $name not found in program this.$name" }
        GLES20.glUniform1f(location, value)
    }

    fun setUniformValue(id : Int, value : Float) {
        GLES20.glUniform1f(uniforms[id].location, value)
    }

    fun setUniformValue(name : String, value : FloatArray) {
        val location = uniformMap[name]
        check(location != null) { "Uniform $name not found in program this.$name" }
        GLES20.glUniform1fv(location, value.size, value, 0)
    }

    fun setUniformValue(id : Int, value : FloatArray) {
        GLES20.glUniform1fv(uniforms[id].location, value.size, value, 0)
    }

    fun setUniformValue(name : String, value : vector2f) {
        val location = uniformMap[name]
        check(location != null) { "Uniform $name not found in program this.$name" }
        GLES20.glUniform2fv(location, 1, value.toFloatArray(), 0)
    }

    fun setUniformValue(id : Int, value : vector2f) {
        GLES20.glUniform2fv(uniforms[id].location, 1, value.toFloatArray(), 0)
    }

    fun setUniformValue(name : String, value : Array<vector2f>) {
        val location = uniformMap[name]
        check(location != null) { "Uniform $name not found in program this.$name" }
        GLES20.glUniform2fv(location, value.size, value.toFloatArray(), 0)
    }

    fun setUniformValue(id : Int, value : Array<vector2f>) {
        GLES20.glUniform2fv(uniforms[id].location, value.size, value.toFloatArray(), 0)
    }

    fun setUniformValue(name : String, value : vector3f) {
        val location = uniformMap[name]
        check(location != null) { "Uniform $name not found in program this.$name" }
        GLES20.glUniform3fv(location, 1, value.toFloatArray(), 0)
    }

    fun setUniformValue(id : Int, value : vector3f) {
        GLES20.glUniform3fv(uniforms[id].location, 1, value.toFloatArray(), 0)
    }

    fun setUniformValue(name : String, value : Array<vector3f>) {
        val location = uniformMap[name]
        check(location != null) { "Uniform $name not found in program this.$name" }
        GLES20.glUniform3fv(location, value.size, value.toFloatArray(), 0)
    }

    fun setUniformValue(id : Int, value : Array<vector3f>) {
        GLES20.glUniform3fv(uniforms[id].location, value.size, value.toFloatArray(), 0)
    }

    fun setUniformValue(name : String, value : vector4f) {
        val location = uniformMap[name]
        check(location != null) { "Uniform $name not found in program this.$name" }
        GLES20.glUniform4fv(location, 1, value.toFloatArray(), 0)
    }

    fun setUniformValue(id : Int, value : vector4f) {
        GLES20.glUniform4fv(uniforms[id].location, 1, value.toFloatArray(), 0)
    }

    fun setUniformValue(name : String, value : Array<vector4f>) {
        val location = uniformMap[name]
        check(location != null) { "Uniform $name not found in program this.$name" }
        GLES20.glUniform4fv(location, value.size, value.toFloatArray(), 0)
    }

    fun setUniformValue(id : Int, value : Array<vector4f>) {
        GLES20.glUniform4fv(uniforms[id].location, value.size, value.toFloatArray(), 0)
    }

    fun setUniformValue(name : String, value : quaternion) {
        val location = uniformMap[name]
        check(location != null) { "Uniform $name not found in program this.$name" }
        GLES20.glUniform4fv(location, 1, value.toFloatArray(), 0)
    }

    fun setUniformValue(id : Int, value : quaternion) {
        GLES20.glUniform4fv(uniforms[id].location, 1, value.toFloatArray(), 0)
    }

    fun setUniformValue(name : String, value : Array<quaternion>) {
        val location = uniformMap[name]
        check(location != null) { "Uniform $name not found in program this.$name" }
        GLES20.glUniform4fv(location, value.size, value.toFloatArray(), 0)
    }

    fun setUniformValue(id : Int, value : Array<quaternion>) {
        GLES20.glUniform4fv(uniforms[id].location, value.size, value.toFloatArray(), 0)
    }

    fun setUniformValue(name : String, value : Rectangle) {
        val location = uniformMap[name]
        check(location != null) { "Uniform $name not found in program this.$name" }
        GLES20.glUniform4fv(location, 1, value.toFloatArray(), 0)
    }

    fun setUniformValue(id : Int, value : Rectangle) {
        GLES20.glUniform4fv(uniforms[id].location, 1, value.toFloatArray(), 0)
    }

    fun setUniformValue(name : String, value : matrix2f) {
        val location = uniformMap[name]
        check(location != null) { "Uniform $name not found in program this.$name" }
        GLES20.glUniformMatrix2fv(location, 1, false, value.toFloatArray(), 0)
    }

    fun setUniformValue(id : Int, value : matrix2f) {
        GLES20.glUniformMatrix2fv(uniforms[id].location, 1, false, value.toFloatArray(), 0)
    }

    fun setUniformValue(name : String, value : Array<matrix2f>) {
        val location = uniformMap[name]
        check(location != null) { "Uniform $name not found in program this.$name" }
        GLES20.glUniformMatrix2fv(location, value.size, false, value.toFloatArray(), 0)
    }

    fun setUniformValue(id : Int, value : Array<matrix2f>) {
        GLES20.glUniformMatrix2fv(uniforms[id].location, value.size, false, value.toFloatArray(), 0)
    }

    fun setUniformValue(name : String, value : matrix3f) {
        val location = uniformMap[name]
        check(location != null) { "Uniform $name not found in program this.$name" }
        GLES20.glUniformMatrix3fv(location, 1, false, value.toFloatArray(), 0)
    }

    fun setUniformValue(id : Int, value : matrix3f) {
        GLES20.glUniformMatrix3fv(uniforms[id].location, 1, false, value.toFloatArray(), 0)
    }

    fun setUniformValue(name : String, value : Array<matrix3f>) {
        val location = uniformMap[name]
        check(location != null) { "Uniform $name not found in program this.$name" }
        GLES20.glUniformMatrix3fv(location, value.size, false, value.toFloatArray(), 0)
    }

    fun setUniformValue(id : Int, value : Array<matrix3f>) {
        GLES20.glUniformMatrix3fv(uniforms[id].location, value.size, false, value.toFloatArray(), 0)
    }

    fun setUniformValue(name : String, value : matrix4f) {
        val location = uniformMap[name]
        check(location != null) { "Uniform $name not found in program this.$name" }
        GLES20.glUniformMatrix4fv(location, 1, false, value.toFloatArray(), 0)
    }

    fun setUniformValue(id : Int, value : matrix4f) {
        GLES20.glUniformMatrix4fv(uniforms[id].location, 1, false, value.toFloatArray(), 0)
    }

    fun setUniformValue(name : String, value : Array<matrix4f>) {
        val location = uniformMap[name]
        check(location != null) { "Uniform $name not found in program this.$name" }
        GLES20.glUniformMatrix4fv(location, value.size, false, value.toFloatArray(), 0)
    }

    fun setUniformValue(id : Int, value : Array<matrix4f>) {
        GLES20.glUniformMatrix4fv(uniforms[id].location, value.size, false, value.toFloatArray(), 0)
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