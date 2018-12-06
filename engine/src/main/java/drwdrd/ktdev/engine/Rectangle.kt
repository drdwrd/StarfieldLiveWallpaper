package drwdrd.ktdev.engine

class Rectangle(_left : Float, _top : Float, _right : Float, _bottom : Float) {

    constructor() : this(0.0f, 0.0f, 0.0f, 0.0f)

    constructor(rect : Rectangle) : this(rect.left, rect.top, rect.left, rect.bottom)

    constructor(location : Point, size : Size) : this(location.x, location.y, location.x + size.width, location.y + size.height)

    constructor(pt1 : Point, pt2 : Point) : this(pt1.x, pt1.y, pt2.x, pt2.y)


    var left = _left
        private set

    var top = _top
        private set

    var right = _right
        private set

    var bottom = _bottom
        private set

    val width
        get() = right - left
    val height
        get() = bottom - top

    val size
        get() = Size(right - left, bottom - top)

    val location
        get() = Point(left, top)

    val center
        get() = Point((left + right) / 2.0f, (top + bottom) / 2.0f)

    val halfSize
        get() = Size((right - left) / 2.0f, (bottom - top) / 2.0f)


    override operator fun equals(other : Any?) : Boolean {
        return when(other) {
            is Rectangle -> ((left == other.left) && (top == other.top) && (right == other.right) && (bottom == other.bottom))
            else -> false
        }
    }

    fun contains(px : Float, py : Float) : Boolean {
        return ((px >= left) && (px <= right) && (py >= top) && (py <= bottom))
    }

    fun contains(pt : Point) : Boolean {
        return ((pt.x >= left) && (pt.x <= right)&&(pt.y >= top) && (pt.y <= bottom))
    }

    fun contains(rect : Rectangle) : Boolean {
        return ((rect.left >= left) && (rect.right <= right) && ((rect.top >= top) && (rect.bottom <= bottom)))
    }

    fun inflate(dx : Float, dy : Float) {
        left -= dx
        top -= dy
        right += dx
        bottom += dy
    }

    fun inflate(sz : Size) {
        left -= sz.width
        top -= sz.height
        right += sz.width
        bottom += sz.height
    }

    fun offset(dx : Float, dy : Float) {
        left += dx
        top += dy
        right += dx
        bottom += dy
    }

    fun offset(p : Point) {
        left += p.x
        top += p.y
        right += p.x
        bottom += p.y
    }

    fun intersectsWith(r : Rectangle) : Boolean {
        return ((left < r.right) && (top < r.bottom) && (right > r.left) && (bottom > r.top))
    }

    fun toFloatArray() = floatArrayOf(left, top, right, bottom)
}
