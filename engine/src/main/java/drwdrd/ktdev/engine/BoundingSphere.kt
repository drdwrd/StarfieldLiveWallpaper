package drwdrd.ktdev.engine

class BoundingSphere {

    var center : vector3f
        private set

    var radius : Float
        private set

    constructor(c : vector3f, r : Float) {
        this.center = c
        this.radius = r
    }

    fun contains(p : vector3f) : Boolean {
        val dist = (p - center).length()
        if(dist < radius) {
            return true
        }
        return false
    }

}