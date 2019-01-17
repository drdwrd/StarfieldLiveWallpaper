package drwdrd.ktdev.engine

class Frustum {

    var left : Plane
    var right : Plane
    var top : Plane
    var bottom : Plane
    var near : Plane
    var far : Plane


    constructor(frustumMatrix : matrix4f) {
        left = Plane(frustumMatrix[3] + frustumMatrix[0], frustumMatrix[7] + frustumMatrix[4], frustumMatrix[11] + frustumMatrix[8], frustumMatrix[15] + frustumMatrix[12])
        right = Plane(frustumMatrix[3] - frustumMatrix[0], frustumMatrix[7] - frustumMatrix[4], frustumMatrix[11] - frustumMatrix[8], frustumMatrix[15] - frustumMatrix[12])

        top = Plane(frustumMatrix[3] - frustumMatrix[1], frustumMatrix[7] - frustumMatrix[5], frustumMatrix[11] - frustumMatrix[9], frustumMatrix[15] - frustumMatrix[13])
        bottom = Plane(frustumMatrix[3] + frustumMatrix[1], frustumMatrix[7] + frustumMatrix[5], frustumMatrix[11] + frustumMatrix[9], frustumMatrix[15] + frustumMatrix[13])

        near = Plane(frustumMatrix[3] + frustumMatrix[2], frustumMatrix[7] + frustumMatrix[6], frustumMatrix[11] + frustumMatrix[10], frustumMatrix[15] + frustumMatrix[14])
        far = Plane(frustumMatrix[3] - frustumMatrix[2], frustumMatrix[7] - frustumMatrix[6], frustumMatrix[11] - frustumMatrix[10], frustumMatrix[15] - frustumMatrix[14])
    }

    fun contains(p : vector3f) : Boolean {
        if(left.distance(p) < 0.0f || right.distance(p) < 0.0f ||
            top.distance(p) < 0.0f || bottom.distance(p) < 0.0f ||
            near.distance(p) < 0.0f || far.distance(p) < 0.0f) {
            return false
        }
        return true
    }

    fun contains(s : BoundingSphere) : Boolean {
        if(left.distance(s.center) < -s.radius || right.distance(s.center) < -s.radius ||
            top.distance(s.center) < -s.radius || bottom.distance(s.center) < -s.radius ||
            near.distance(s.center) < -s.radius || far.distance(s.center) < -s.radius) {
            return false
        }
        return true
    }
}