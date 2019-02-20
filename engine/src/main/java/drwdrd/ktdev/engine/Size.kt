package drwdrd.ktdev.engine

class Size(w : Float, h : Float) {

    constructor() : this(0.0f, 0.0f)

    constructor(pt : Size) : this(pt.width, pt.height)

    val width = w
    val height = h

    fun isEqual(sz : Size) : Boolean {
        return (width == sz.width) && (height == sz.height)
    }
}
