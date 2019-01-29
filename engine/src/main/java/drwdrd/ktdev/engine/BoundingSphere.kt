package drwdrd.ktdev.engine

class BoundingSphere(c : vector3f, r : Float) {

    val center  = c

    val radius = r

    fun contains(p : vector3f) : Boolean {
        val dist = (p - center).length()
        if(dist < radius) {
            return true
        }
        return false
    }

}