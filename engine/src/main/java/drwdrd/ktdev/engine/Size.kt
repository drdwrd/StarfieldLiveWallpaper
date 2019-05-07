package drwdrd.ktdev.engine

class Size(val width : Float, val height : Float) {

    constructor() : this(0.0f, 0.0f)

    constructor(pt : Size) : this(pt.width, pt.height)

    fun isEqual(sz : Size) : Boolean {
        return (width == sz.width) && (height == sz.height)
    }
}
