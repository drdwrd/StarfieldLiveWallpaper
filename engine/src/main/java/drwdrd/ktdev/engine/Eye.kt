package drwdrd.ktdev.engine

import android.R

class Eye() {

    var position : vector3f = vector3f()
        private set

    var forward : vector3f = vector3f()
        private set

    var up : vector3f = vector3f()
        private set

    var right : vector3f = vector3f()
        private set

    private var direction : vector3f = vector3f()

    private var frustum : Frustum = Frustum()


    var viewMatrix : matrix4f = matrix4f()
        private set
        get() {
            var rotationMatrix = matrix4f(
                right[0], up[0], direction[0], 0.0f,
                right[1], up[1], direction[1], 0.0f,
                right[2], up[2], direction[2], 0.0f,
                0.0f,       0.0f,    0.0f,1.0f)

            var translationMatrix = matrix4f(
                1.0f,           0.0f,           0.0f,          0.0f,
                0.0f,           1.0f,           0.0f,          0.0f,
                0.0f,           0.0f,           1.0f,          0.0f,
                -position[0], -position[1], -position[2], 1.0f)

            field = rotationMatrix * translationMatrix
            return field
        }

    var projectionMatrix : matrix4f = matrix4f()
        private set
        get() {
            require(frustum.viewportSize.x > 0.0f)
            require(frustum.viewportSize.y > 0.0f)
            require(frustum.fov > 0.0f)
            when (frustum.projection) {
                Projection.Perspective -> {
                    val aspect = frustum.viewportSize.x / frustum.viewportSize.y
                    field.setPerspectiveProjectionV(deg2rad(frustum.fov), aspect, frustum.znear, frustum.zfar)
                }

                Projection.Orthogonal -> {
                    field.setOrthoProjection(
                        frustum.left,
                        frustum.right,
                        frustum.bottom,
                        frustum.top,
                        frustum.znear,
                        frustum.zfar
                    )
                }
            }
            return field
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
    }

    fun setPerspective(fovy : Float, znear : Float, zfar : Float) {
        frustum.znear = znear
        frustum.zfar = zfar
        frustum.fov = fovy
        frustum.projection = Projection.Perspective
    }

    fun setOrtho(left : Float, right : Float, top : Float, bottom : Float, znear : Float, zfar : Float) {
        frustum.left = left
        frustum.right = right
        frustum.top = top
        frustum.bottom = bottom
        frustum.znear = znear
        frustum.zfar = zfar
        frustum.projection = Projection.Orthogonal
    }

    fun setLookAt(_position : vector3f , _target : vector3f , _up : vector3f) {
        position = _position
        val dir = _position - _target
        direction = dir.normalized()
        right = vector3f.cross(_up, direction).normalized()
        up = vector3f.cross(direction, right)
    }
}
