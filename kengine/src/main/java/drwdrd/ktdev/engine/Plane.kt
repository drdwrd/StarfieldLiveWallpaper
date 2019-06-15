package drwdrd.ktdev.engine

class Plane {

    private var normal : vector3f
    private var intercept : Float

    constructor(a : Float, b : Float, c : Float, d : Float) {
        val n = vector3f(a, b, c)
        this.normal = n.normalized()
        this.intercept = d / n.length()
    }

    constructor(normal : vector3f, intercept : Float) {
        this.normal = normal
        this.intercept = intercept
    }

    constructor(n : vector3f, p : vector3f) {
        this.normal = n.normalized()
        this.intercept = -vector3f.dot(n.normalized(), p)
    }

    constructor(p1 : vector3f, p2 : vector3f, p3 : vector3f) {
        val n = (p2 - p1).cross(p3 - p1)
        this.normal = n.normalized()
        this.intercept = vector3f.dot(this.normal, p1)
    }

    constructor(plane : Plane) : this(plane.normal, plane.intercept)

    fun distance(p : vector3f) = intercept + vector3f.dot(normal, p)

}