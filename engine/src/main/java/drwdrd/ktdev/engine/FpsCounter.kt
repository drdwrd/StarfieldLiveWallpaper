package drwdrd.ktdev.engine

class FpsCounter {

    interface OnMeasureListener {
        fun onMeasure(frameTime : Double)
    }

    private var duration = 0.0
    private var counter = 0

    var onMeasureListener : OnMeasureListener? = null

    var frameTime = 0.0
        private set

    fun tick(deltaTime : Double) {
        counter++
        duration += deltaTime
        if(duration > 5.0) {
            frameTime = 1000.0 * duration / counter
            duration = 0.0
            counter = 0
            onMeasureListener?.onMeasure(frameTime)
        }
    }
}