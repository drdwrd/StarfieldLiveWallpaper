package drwdrd.ktdev.starfield

import android.content.res.Configuration
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import drwdrd.ktdev.engine.*
import kotlin.math.sign



interface ParallaxEffectEngine {

    var reset : Boolean
    var orientation : Int
    val offset : vector3f
    val backgroundOffset : vector2f
    val parallaxEffectScale : Float

    fun connect(sensorManager: SensorManager)
    fun disconnect(sensorManager: SensorManager)
    fun onTick(deltaTime : Float)
    fun onOffsetChanged(xOffset: Float, yOffset: Float, xOffsetStep: Float, yOffsetStep: Float, xPixelOffset: Int, yPixelOffset: Int)
}

private const val kFilter = 0.8f


class EmptyParallaxEffectEngine : ParallaxEffectEngine {

    override var reset = false

    override var orientation = Configuration.ORIENTATION_UNDEFINED

    override var offset = vector3f(0.0f, 0.0f, 0.0f)
        private set

    override var backgroundOffset = vector2f(0.0f, 0.0f)
        private set

    override var parallaxEffectScale = SettingsProvider.parallaxEffectMultiplier
        private set

    private var lastXOffset = 0.0f
    private var dxOffset = 0.0f
    private var precessionSpeed = 0.0f

    override fun connect(sensorManager: SensorManager) {

    }

    override fun disconnect(sensorManager: SensorManager) {

    }

    override fun onOffsetChanged(xOffset: Float, yOffset: Float, xOffsetStep: Float, yOffsetStep: Float, xPixelOffset: Int, yPixelOffset: Int) {
        dxOffset = 0.05f * parallaxEffectScale * (xOffset - lastXOffset)
        lastXOffset = xOffset
        precessionSpeed = sign(dxOffset) * SettingsProvider.precessionSpeed
    }

    override fun onTick(deltaTime: Float) {
        if(reset) {
            backgroundOffset = vector2f(0.0f, 0.0f)
            reset = false
        }
        backgroundOffset.plusAssign(vector2f(dxOffset, 0.0f))
        offset = vector3f(0.0f, dxOffset, 0.0f)
        dxOffset = precessionSpeed
    }
}

class GravityParallaxEffectEngine : ParallaxEffectEngine {

    private val gravityVector = vector3f(0.0f, 0.0f, 0.0f)
    private var lastGravity = vector3f(0.0f,0.0f, 0.0f)
    private var lastXOffset = 0.0f
    private var dxOffset = 0.0f
    private var precessionSpeed = 0.0f

    override var reset = true

    override var orientation = Configuration.ORIENTATION_UNDEFINED

    override var offset = vector3f(0.0f, 0.0f, 0.0f)
        private set

    override var backgroundOffset = vector2f(0.0f, 0.0f)
        private set

    override var parallaxEffectScale = SettingsProvider.parallaxEffectMultiplier
        private set

    private val accelerometerSensorEventListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { }

        override fun onSensorChanged(event: SensorEvent?) {
            when(event?.sensor?.type) {
                Sensor.TYPE_GRAVITY -> {
                    gravityVector[0] = kFilter * gravityVector[0] + (1.0f - kFilter) * event.values[0]
                    gravityVector[1] = kFilter * gravityVector[1] + (1.0f - kFilter) * event.values[1]
                    gravityVector[2] = kFilter * gravityVector[2] + (1.0f - kFilter) * event.values[2]
                }
            }
        }
    }

    override fun connect(sensorManager: SensorManager) {
        parallaxEffectScale = SettingsProvider.parallaxEffectMultiplier
        val gravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
        if(!sensorManager.registerListener(accelerometerSensorEventListener, gravity, SensorManager.SENSOR_DELAY_GAME)) {
            Log.debug("Cannot connect to gravity sensor.\n")
        }
    }

    override fun disconnect(sensorManager: SensorManager) {
        sensorManager.unregisterListener(accelerometerSensorEventListener)
    }

    override fun onTick(deltaTime: Float) {
        val g = gravityVector.normalized()
        val dx : Float
        val dy : Float
        if(reset) {
            backgroundOffset = vector2f(0.0f, 0.0f)
            dx = 0.0f
            dy = 0.0f
            reset = false
        } else {
            val v = vector3f()
            val rotationMatrix = matrix4f()
            rotationMatrix.setAxisRotation(g, lastGravity)
            SensorManager.getOrientation(rotationMatrix.toFloatArray(), v.toFloatArray())
            dx = parallaxEffectScale * v[2]
            dy = parallaxEffectScale * v[1]
        }
        when(orientation) {
            Configuration.ORIENTATION_PORTRAIT -> {
                backgroundOffset.plusAssign(vector2f(dx + dxOffset, dy))
                offset = vector3f(-dy,dx + dxOffset, 0.0f)
            }

            Configuration.ORIENTATION_LANDSCAPE -> {
                backgroundOffset.plusAssign(vector2f(dy + dxOffset, dx))
                offset = vector3f(-dx, dy + dxOffset, 0.0f)
            }
        }
        dxOffset = precessionSpeed
        lastGravity = g
    }

    override fun onOffsetChanged(xOffset: Float, yOffset: Float, xOffsetStep: Float, yOffsetStep: Float, xPixelOffset: Int, yPixelOffset: Int) {
        dxOffset = 0.1f * parallaxEffectScale * (xOffset - lastXOffset)
        lastXOffset = xOffset
        precessionSpeed = sign(dxOffset) * SettingsProvider.precessionSpeed
    }
}

class GyroParallaxEffectEngine : ParallaxEffectEngine {

    override var backgroundOffset = vector2f(0.0f, 0.0f)
        private set

    override var offset = vector3f(0.0f, 0.0f, 0.0f)
        private set

    override var parallaxEffectScale = SettingsProvider.parallaxEffectMultiplier
        private set

    override var reset = true

    override var orientation = Configuration.ORIENTATION_UNDEFINED

    private var rotationVector = vector3f(0.0f, 0.0f, 0.0f)
    private var lastXOffset = 0.0f
    private var dxOffset = 0.0f
    private var precessionSpeed = 0.0f

    private val gyroSensorListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

        }

        override fun onSensorChanged(event: SensorEvent?) {
            when(event?.sensor?.type) {
                Sensor.TYPE_GYROSCOPE -> {
                    rotationVector[0] = kFilter * rotationVector[0] + (1.0f - kFilter) * event.values[0]
                    rotationVector[1] = kFilter * rotationVector[1] + (1.0f - kFilter) * event.values[1]
                    rotationVector[2] = kFilter * rotationVector[2] + (1.0f - kFilter) * event.values[2]
                }
            }
        }
    }

    override fun connect(sensorManager: SensorManager) {
        parallaxEffectScale = SettingsProvider.parallaxEffectMultiplier
        val gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        if(!sensorManager.registerListener(gyroSensorListener, gyro, SensorManager.SENSOR_DELAY_GAME)) {
            Log.debug("Cannot connect to gyro sensor.\n")
        }

    }

    override fun disconnect(sensorManager: SensorManager) {
        sensorManager.unregisterListener(gyroSensorListener)
    }

    override fun onOffsetChanged(xOffset: Float, yOffset: Float, xOffsetStep: Float, yOffsetStep: Float, xPixelOffset: Int, yPixelOffset: Int) {
        dxOffset = 0.1f * parallaxEffectScale * (xOffset - lastXOffset)
        lastXOffset = xOffset
        precessionSpeed = sign(dxOffset) * SettingsProvider.precessionSpeed
    }

    override fun onTick(deltaTime: Float) {
        val dx : Float
        val dy : Float
        if(reset) {
            backgroundOffset = vector2f(0.0f, 0.0f)
            rotationVector = vector3f(0.0f, 0.0f, 0.0f)
            dx = 0.0f
            dy = 0.0f
            reset = false
        } else {
            dx = parallaxEffectScale * rotationVector[1] * deltaTime
            dy = -parallaxEffectScale * rotationVector[0] * deltaTime
        }
        when(orientation) {
            Configuration.ORIENTATION_PORTRAIT -> {
                backgroundOffset.plusAssign(vector2f(dx + dxOffset, dy))
                offset = vector3f(-dy,dx + dxOffset, 0.0f)
            }

            Configuration.ORIENTATION_LANDSCAPE -> {
                backgroundOffset.plusAssign(vector2f(dy + dxOffset, dx))
                offset = vector3f(-dx, dy + dxOffset, 0.0f)
            }
        }
        dxOffset = precessionSpeed
    }
}
