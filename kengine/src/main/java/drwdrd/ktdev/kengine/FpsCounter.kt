package drwdrd.ktdev.kengine

class FpsCounter(private val measureCycle : Double) {

    interface OnMeasureListener {
        fun onStart()
        fun onMeasure(frameTime : Double)
        fun onTick(deltaFrameTime: Double)
    }

    private var duration = 0.0
    private var counter = 0

    var onMeasureListener : OnMeasureListener? = null

    var frameTime = 0.0
        private set

    fun start() {
        onMeasureListener?.onStart()
    }

    fun tick(deltaTime : Double) {
        counter++
        duration += deltaTime
        onMeasureListener?.onTick(1000.0 * deltaTime)
        if(duration > measureCycle) {
            frameTime = 1000.0 * duration / counter
            duration = 0.0
            counter = 0
            onMeasureListener?.onMeasure(frameTime)
        }
    }
}