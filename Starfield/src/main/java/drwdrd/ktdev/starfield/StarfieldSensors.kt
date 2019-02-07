package drwdrd.ktdev.starfield

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import drwdrd.ktdev.engine.matrix4f
import drwdrd.ktdev.engine.vector2f
import drwdrd.ktdev.engine.vector3f
import kotlin.math.sign

interface StarfieldParallaxEffect {

    var reset : Boolean
    val offset : vector3f
    val backgroundOffset : vector2f
    val parallaxEffectScale : Float

    fun connect(sensorManager: SensorManager)
    fun disconnect(sensorManager: SensorManager)
    fun onTick()
    fun onOffsetChanged(xOffset: Float, yOffset: Float, xOffsetStep: Float, yOffsetStep: Float, xPixelOffset: Int, yPixelOffset: Int)
}

const val gravityFilter = 0.95f         //TODO : move to settings???

class StarfieldParallaxEffectSimple : StarfieldParallaxEffect {

    private val gravityVector = vector3f(0.0f, 0.0f, 0.0f)
    private var lastGravity = vector3f(0.0f,0.0f, 0.0f)
    private var lastXOffset = 0.0f
    private var dxOffset = 0.0f
    private var precessionSpeed = 0.0f

    override var reset = false

    override var offset = vector3f(0.0f, 0.0f, 0.0f)
        private set

    override var backgroundOffset = vector2f(0.0f, 0.0f)
        private set

    override var parallaxEffectScale = SettingsProvider.parallaxEffectMultiplier
        private set


    inner class AccelerometerSensorEventListener : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { }

        override fun onSensorChanged(event: SensorEvent?) {
            when(event?.sensor?.type) {
                Sensor.TYPE_GRAVITY -> {
                    gravityVector[0] = gravityVector[0] * gravityFilter + (1.0f - gravityFilter) * event.values[0]
                    gravityVector[1] = gravityVector[1] * gravityFilter + (1.0f - gravityFilter) * event.values[1]
                    gravityVector[2] = gravityVector[2] * gravityFilter + (1.0f - gravityFilter) * event.values[2]
                }
            }
        }
    }

    private val accelerometerSensorEventListener = AccelerometerSensorEventListener()

    override fun connect(sensorManager: SensorManager) {
        parallaxEffectScale = SettingsProvider.parallaxEffectMultiplier
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
        sensorManager.registerListener(accelerometerSensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_FASTEST)
    }

    override fun disconnect(sensorManager: SensorManager) {
        sensorManager.unregisterListener(accelerometerSensorEventListener)
    }

    override fun onTick() {
        val g = gravityVector.normalized()
        val dg : vector2f
        if(reset) {
            backgroundOffset = vector2f(0.0f, 0.0f)
            dg = vector2f(0.0f, 0.0f)
            reset = false
        } else {
            val v = vector3f()
            val m = matrix4f()
            m.setAxisRotation(g, lastGravity)
            SensorManager.getOrientation(m.toFloatArray(), v.toFloatArray())
            if(v[2].isNaN()) {
                v[2] = 0.0f
            }
            if(v[1].isNaN()) {
                v[1] = 0.0f
            }
            dg = vector2f(parallaxEffectScale * v[2], parallaxEffectScale * v[1])
        }
        backgroundOffset.plusAssign(dg)
        backgroundOffset.plusAssign(vector2f(dxOffset, 0.0f))
        lastGravity = g
        offset = vector3f(-dg.y,dg.x + dxOffset, 0.0f)
        dxOffset = precessionSpeed
    }

    override fun onOffsetChanged(xOffset: Float, yOffset: Float, xOffsetStep: Float, yOffsetStep: Float, xPixelOffset: Int, yPixelOffset: Int) {
        dxOffset = 0.05f * parallaxEffectScale * (xOffset - lastXOffset)
        lastXOffset = xOffset
        precessionSpeed = sign(dxOffset) * SettingsProvider.precessionSpeed
    }
}