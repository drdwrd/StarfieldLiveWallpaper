package drwdrd.ktdev.kengine

class Frustum(frustumMatrix: matrix4f) {

    val left = Plane(frustumMatrix[3] + frustumMatrix[0], frustumMatrix[7] + frustumMatrix[4], frustumMatrix[11] + frustumMatrix[8], frustumMatrix[15] + frustumMatrix[12])
    val right = Plane(frustumMatrix[3] - frustumMatrix[0], frustumMatrix[7] - frustumMatrix[4], frustumMatrix[11] - frustumMatrix[8], frustumMatrix[15] - frustumMatrix[12])
    val top = Plane(frustumMatrix[3] - frustumMatrix[1], frustumMatrix[7] - frustumMatrix[5], frustumMatrix[11] - frustumMatrix[9], frustumMatrix[15] - frustumMatrix[13])
    val bottom = Plane(frustumMatrix[3] + frustumMatrix[1], frustumMatrix[7] + frustumMatrix[5], frustumMatrix[11] + frustumMatrix[9], frustumMatrix[15] + frustumMatrix[13])
    val near = Plane(frustumMatrix[3] + frustumMatrix[2], frustumMatrix[7] + frustumMatrix[6], frustumMatrix[11] + frustumMatrix[10], frustumMatrix[15] + frustumMatrix[14])
    val far = Plane(frustumMatrix[3] - frustumMatrix[2], frustumMatrix[7] - frustumMatrix[6], frustumMatrix[11] - frustumMatrix[10], frustumMatrix[15] - frustumMatrix[14])

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

    fun isInDistance(p : vector3f, d : Float) : Boolean {
        if(left.distance(p) < d || right.distance(p) < d ||
            top.distance(p) < d || bottom.distance(p) < d ||
            near.distance(p) < d || far.distance(p) < d) {
            return false
        }
        return true
    }
}