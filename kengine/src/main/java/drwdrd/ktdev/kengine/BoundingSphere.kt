package drwdrd.ktdev.kengine

class BoundingSphere(val center : vector3f, val radius : Float) {

    fun contains(p : vector3f) : Boolean {
        val dist = (p - center).length()
        if(dist < radius) {
            return true
        }
        return false
    }

}