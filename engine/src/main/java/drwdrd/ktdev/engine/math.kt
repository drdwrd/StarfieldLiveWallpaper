package drwdrd.ktdev.engine


const val M_PI: Float = 3.141592653589793f
const val M_E: Float = 2.718281828459045f


fun deg2rad(deg: Float) : Float {
    return deg / 180.0f * M_PI
}
