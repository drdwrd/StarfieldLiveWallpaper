package drwdrd.ktdev.engine


class Point(_x : Float, _y : Float) {

    constructor() : this(0.0f, 0.0f)

    constructor(pt : Point) : this(pt.x, pt.y)

    val x = _x
    val y = _y

    fun isEqual(p : Point) : Boolean {
        return (x == p.x) && (y == p.y)
    }
}
