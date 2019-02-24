package drwdrd.ktdev.starfield

import android.content.res.Configuration
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import drwdrd.ktdev.engine.*
import kotlin.math.max
import kotlin.math.min
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

private const val gFilter = 0.8f


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

class SensorDataBuffer(size : Int) {

    private val buffer = Array(size) { vector3f() }
    private var bufferDataPos = 0

    fun putEventData(data : FloatArray) {
        buffer[bufferDataPos++] = vector3f(data[0], data[1], data[2])
        bufferDataPos %= buffer.size
    }

    fun getData() : vector3f {
        val v = vector3f(0.0f, 0.0f, 0.0f)
        for(i in bufferDataPos until bufferDataPos + buffer.size) {
            v += buffer[i % buffer.size]
        }
        return v / buffer.size.toFloat()
    }

}

class AccelerometerParallaxEffectEngine : ParallaxEffectEngine {

    private val gravitySensorData = SensorDataBuffer(24)
    private val magnetometerSensorData = SensorDataBuffer(24)
    private val gyroSensorData = SensorDataBuffer(12)
    private var prevRotationMatrix = matrix4f()
    private var rotationVector = vector3f(0.0f, 0.0f, 0.0f)
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

    private val sensorEventListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { }

        override fun onSensorChanged(event: SensorEvent?) {
            when(event?.sensor?.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    gravitySensorData.putEventData(event.values)
                }
                Sensor.TYPE_MAGNETIC_FIELD -> {
                    magnetometerSensorData.putEventData(event.values)
                }
            }
        }
    }

    override fun connect(sensorManager: SensorManager) {
        parallaxEffectScale = SettingsProvider.parallaxEffectMultiplier
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if(!sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_GAME)) {
            Log.debug("Cannot connect to accelerometer sensor.\n")
        }
        val magnetic = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        if(!sensorManager.registerListener(sensorEventListener, magnetic, SensorManager.SENSOR_DELAY_GAME)) {
            Log.debug("Cannot connect to magnetic sensor.\n")
        }
    }

    override fun disconnect(sensorManager: SensorManager) {
        sensorManager.unregisterListener(sensorEventListener)
    }

    override fun onTick(deltaTime: Float) {
        val rotationMatrix = matrix4f()
        val rotAngles = vector3f(0.0f, 0.0f, 0.0f)

        val gravityVector = gravitySensorData.getData()
        val magneticVector = magnetometerSensorData.getData()

        SensorManager.getRotationMatrix(rotationMatrix.toFloatArray(), null, gravityVector.toFloatArray(), magneticVector.toFloatArray())
        SensorManager.getAngleChange(rotAngles.toFloatArray(), rotationMatrix.toFloatArray(), prevRotationMatrix.toFloatArray())
        prevRotationMatrix = rotationMatrix

        gyroSensorData.putEventData(rotAngles.toFloatArray())

        rotationVector = gyroSensorData.getData()

        val dx : Float
        val dy : Float
        if(reset) {
            backgroundOffset = vector2f(0.0f, 0.0f)
            rotationVector = vector3f(0.0f, 0.0f, 0.0f)
            dx = 0.0f
            dy = 0.0f
            reset = false
        } else {
            dx = parallaxEffectScale * rotationVector[2]
            dy = parallaxEffectScale * rotationVector[1]
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
    private val gyroSensorDataBuffer = SensorDataBuffer(8)
    private var lastXOffset = 0.0f
    private var dxOffset = 0.0f
    private var precessionSpeed = 0.0f

    private val gyroSensorListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

        }

        override fun onSensorChanged(event: SensorEvent?) {
            when(event?.sensor?.type) {
                Sensor.TYPE_GYROSCOPE -> {
                    gyroSensorDataBuffer.putEventData(event.values)
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
        rotationVector = gyroSensorDataBuffer.getData()
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
