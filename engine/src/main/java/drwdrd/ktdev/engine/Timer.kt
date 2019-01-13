package drwdrd.ktdev.engine

import android.os.SystemClock
import android.R.attr.tag


class Timer {

    constructor(_timeScale : Double = 1.0) {
        timeScale = _timeScale
    }

    private var lastTime: Long = 0

    var currentTime = 0.0
        private set

    var deltaTime = 0.0
        private set

    var timeScale = 1.0

    fun reset() {
        currentTime = 0.0
        deltaTime = 0.0
        lastTime = SystemClock.uptimeMillis()
    }

    fun tick() {
        val time = SystemClock.uptimeMillis()
        deltaTime = timeScale * (time - lastTime)
        lastTime = time
        currentTime += deltaTime
    }
}