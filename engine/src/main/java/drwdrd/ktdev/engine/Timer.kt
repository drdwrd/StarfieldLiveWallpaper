package drwdrd.ktdev.engine

import android.os.SystemClock
import android.R.attr.tag


class Timer {

    private var lastTime: Long = 0

    var currentTime = 0.0
        private set

    var deltaTime = 0.0
        private set

    fun reset() {
        currentTime = 0.0
        deltaTime = 0.0
        lastTime = SystemClock.uptimeMillis()
    }

    fun tick() {
        val time = SystemClock.uptimeMillis()
        deltaTime = 0.001 * (time - lastTime)
        lastTime = time
        currentTime += deltaTime
    }
}