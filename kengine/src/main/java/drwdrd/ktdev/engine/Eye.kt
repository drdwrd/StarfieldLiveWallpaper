package drwdrd.ktdev.engine

class Eye {

    var position = vector3f()
        private set

    var up = vector3f()
        private set

    var right = vector3f()
        private set

    private var direction = vector3f()

    private var frustum = Frustum()

    val forward : vector3f
        get() = viewMatrix.inverseRotated(direction)

    var viewMatrix = matrix4f()
        private set

    var projectionMatrix = matrix4f()
        private set

    private var rotation = matrix4f.identity()

    private fun calculateViewMatrix() {
        val rotationMatrix = matrix4f(
            right[0], up[0], direction[0], 0.0f,
            right[1], up[1], direction[1], 0.0f,
            right[2], up[2], direction[2], 0.0f,
            0.0f,       0.0f,    0.0f,1.0f)

        val translationMatrix = matrix4f(
            1.0f,           0.0f,           0.0f,          0.0f,
            0.0f,           1.0f,           0.0f,          0.0f,
            0.0f,           0.0f,           1.0f,          0.0f,
            -position[0], -position[1], -position[2], 1.0f)

        viewMatrix = rotation * rotationMatrix * translationMatrix
    }

    private fun calculateProjectionMatrix() {
        require(frustum.viewportSize.x > 0.0f)
        require(frustum.viewportSize.y > 0.0f)
        require(frustum.fov > 0.0f)
        when (frustum.projection) {
            Projection.Perspective -> {
                val aspect = frustum.viewportSize.x / frustum.viewportSize.y
                projectionMatrix.setPerspectiveProjectionV(deg2rad(frustum.fov), aspect, frustum.znear, frustum.zfar)
            }

            Projection.Orthogonal -> {
                projectionMatrix.setOrthoProjection(
                    frustum.left,
                    frustum.right,
                    frustum.bottom,
                    frustum.top,
                    frustum.znear,
                    frustum.zfar
                )
            }
        }
    }

    val viewProjectionMatrix : matrix4f
        get() = projectionMatrix * viewMatrix

    enum class Projection {
        Perspective,
        Orthogonal
    }

    private data class Frustum(var left : Float = -1.0f, var right : Float = 1.0f, var top : Float = 1.0f, var bottom : Float = -1.0f,
                               var znear : Float = 0.1f, var zfar : Float = 100.0f, var fov : Float = 90.0f,
                               var projection : Projection = Projection.Perspective, var viewportSize : vector2f = vector2f(1.0f, 1.0f))


    fun setViewport(size: vector2f) {
        frustum.viewportSize = size
        calculateProjectionMatrix()
    }

    fun setPerspective(fovy : Float, znear : Float, zfar : Float) {
        frustum.znear = znear
        frustum.zfar = zfar
        frustum.fov = fovy
        frustum.projection = Projection.Perspective
        calculateProjectionMatrix()
    }

    fun setOrtho(left : Float, right : Float, top : Float, bottom : Float, znear : Float, zfar : Float) {
        frustum.left = left
        frustum.right = right
        frustum.top = top
        frustum.bottom = bottom
        frustum.znear = znear
        frustum.zfar = zfar
        frustum.projection = Projection.Orthogonal
        calculateProjectionMatrix()
    }

    fun setLookAt(_position : vector3f , _target : vector3f , _up : vector3f) {
        position = _position
        val dir = _position - _target
        direction = dir.normalized()
        right = vector3f.cross(_up, direction).normalized()
        up = vector3f.cross(direction, right)
        //reset rotation
        rotation.loadIdentity()
        calculateViewMatrix()
    }

    fun rotateBy(eulerAngles : vector3f) {
        val m = matrix4f()
        m.setEulerRotation(eulerAngles.x, eulerAngles.y, eulerAngles.z)
        rotation = m * rotation
        calculateViewMatrix()
    }

    fun rotateBy(q : quaternion) {
        val m = q.getRotationMatrix()
        rotation = m * rotation
        calculateViewMatrix()
    }

    fun rotateBy(m : matrix4f) {
        rotation = m * rotation
        calculateViewMatrix()
    }

    fun setRotation(m : matrix4f) {
        rotation = m
        calculateViewMatrix()
    }

    fun setRotation(eulerAngles : vector3f) {
        rotation.setEulerRotation(eulerAngles.x, eulerAngles.y, eulerAngles.z)
        calculateViewMatrix()
    }
}
