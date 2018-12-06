package drwdrd.ktdev.engine

class Size(w : Float, h : Float) {

    constructor() : this(0.0f, 0.0f)

    constructor(pt : Size) : this(pt.width, pt.height)

    val width = w
    val height = h


    override operator fun equals(other : Any?) : Boolean {
        return when(other) {
            is Size -> ((width == other.width) && (height == other.height))
            else -> false
        }
    }
}
