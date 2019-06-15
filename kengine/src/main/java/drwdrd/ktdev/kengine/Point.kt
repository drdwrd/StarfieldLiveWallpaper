package drwdrd.ktdev.kengine


class Point(val x : Float, val y : Float) {

    constructor() : this(0.0f, 0.0f)

    constructor(pt : Point) : this(pt.x, pt.y)

    fun isEqual(p : Point) : Boolean {
        return (x == p.x) && (y == p.y)
    }
}
