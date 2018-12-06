package drwdrd.ktdev.engine


class Point(_x : Float, _y : Float) {

    constructor() : this(0.0f, 0.0f)

    constructor(pt : Point) : this(pt.x, pt.y)

    val x = _x
    val y = _y


    override operator fun equals(other : Any?) : Boolean {
        return when(other) {
            is Point -> ((x == other.x) && (y == other.y))
            else -> false
        }
    }
}
