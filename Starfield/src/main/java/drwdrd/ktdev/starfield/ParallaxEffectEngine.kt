package drwdrd.ktdev.starfield

import android.content.res.Configuration
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import drwdrd.ktdev.engine.*
import kotlin.math.*


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
            backgroundOffset.zero()
            offset.zero()
            reset = false
        }
        backgroundOffset.plusAssign(vector2f(dxOffset, 0.0f))
        offset = vector3f(0.0f, dxOffset, 0.0f)
        dxOffset = precessionSpeed
    }
}

class SensorDataBuffer(_weights : FloatArray) {

    constructor(size : Int) : this(FloatArray(size) { 1.0f })

    private val weights = _weights
    private var normalizationFactor = calculateNormalizationFactor(_weights)
    private val buffer = Array(_weights.size) { vector3f() }
    private var bufferDataPos = 0

    private fun calculateNormalizationFactor(w : FloatArray) : Float {
        var n = 0.0f
        for(w in weights) {
            n += w
        }
        return n
    }

    fun clear() {
        bufferDataPos = 0
        buffer.forEach { it.zero() }
    }

    fun putEventData(data : FloatArray) {
        buffer[bufferDataPos++] = vector3f(data[0], data[1], data[2])
        bufferDataPos %= buffer.size
    }

    fun getData() : vector3f {
        val v = vector3f(0.0f, 0.0f, 0.0f)
        for(i in bufferDataPos until bufferDataPos + buffer.size) {
            val index = i % buffer.size
            v += weights[index] * buffer[index]
        }
        return v / normalizationFactor
    }

}

//TODO: still wonky, remapping????
class AccelerometerParallaxEffectEngine : ParallaxEffectEngine {

    private val gravitySensorData = SensorDataBuffer(12)
    private val magnetometerSensorData = SensorDataBuffer(12)
    private val gyroSensorData = SensorDataBuffer(18)
    private var prevRotationMatrix = matrix3f()
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

    private var sensorEventListener = object : SensorEventListener {
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
        if (!sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_GAME)) {
            Log.error("Cannot connect to accelerometer sensor.\n")
        }
        val magnetic = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        if (!sensorManager.registerListener(sensorEventListener, magnetic, SensorManager.SENSOR_DELAY_GAME)) {
            Log.error("Cannot connect to magnetic sensor.\n")
        }
    }

    override fun disconnect(sensorManager: SensorManager) {
        sensorManager.unregisterListener(sensorEventListener)
    }

    override fun onTick(deltaTime: Float) {
        val rotationMatrix = matrix3f()
        val rotAngles = vector3f(0.0f, 0.0f, 0.0f)

        val accelerometerValues = gravitySensorData.getData()
        val magneticValues = magnetometerSensorData.getData()

        val rm = matrix3f()
        val gravity = vector3f(0f, 0f, 9.81f)

        SensorManager.getRotationMatrix(rm.toFloatArray(), null, accelerometerValues.toFloatArray(), magneticValues.toFloatArray())
        val gravityVector = rm * gravity
        SensorManager.getRotationMatrix(rotationMatrix.toFloatArray(), null, gravityVector.toFloatArray(), magneticValues.toFloatArray())
        SensorManager.getAngleChange(rotAngles.toFloatArray(), rotationMatrix.toFloatArray(), prevRotationMatrix.toFloatArray())
        prevRotationMatrix = rotationMatrix

        gyroSensorData.putEventData(rotAngles.toFloatArray())

        rotationVector = gyroSensorData.getData()

        val dx : Float
        val dy : Float
        if(reset) {
            backgroundOffset.zero()
            rotationVector.zero()
            gravitySensorData.clear()
            magnetometerSensorData.clear()
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

//TODO: still wonky, remapping????
class GravityParallaxEffectEngine : ParallaxEffectEngine {

    private val gravitySensorData = SensorDataBuffer(4)
    private val magnetometerSensorData = SensorDataBuffer(4)
    private val gyroSensorData = SensorDataBuffer(18)
    private var prevRotationMatrix = matrix3f()
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

    private var sensorEventListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { }

        override fun onSensorChanged(event: SensorEvent?) {
            when(event?.sensor?.type) {
                Sensor.TYPE_GRAVITY -> {
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
        val gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
        if(!sensorManager.registerListener(sensorEventListener, gravitySensor, SensorManager.SENSOR_DELAY_GAME)) {
            Log.error("Cannot connect to gravity sensor.\n")
        }
        val magnetic = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        if(!sensorManager.registerListener(sensorEventListener, magnetic, SensorManager.SENSOR_DELAY_GAME)) {
            Log.error("Cannot connect to magnetic sensor.\n")
        }
    }

    override fun disconnect(sensorManager: SensorManager) {
        sensorManager.unregisterListener(sensorEventListener)
    }

    override fun onTick(deltaTime: Float) {
        val rotationMatrix = matrix3f()
        val rotAngles = vector3f(0.0f, 0.0f, 0.0f)

        val accelerometerValues = gravitySensorData.getData()
        val magneticValues = magnetometerSensorData.getData()

        val rm = matrix3f()
        val gravity = vector3f(0f, 0f, 9.81f)

        SensorManager.getRotationMatrix(rm.toFloatArray(), null, accelerometerValues.toFloatArray(), magneticValues.toFloatArray())
        val gravityVector = rm * gravity
        SensorManager.getRotationMatrix(rotationMatrix.toFloatArray(), null, gravityVector.toFloatArray(), magneticValues.toFloatArray())
        SensorManager.getAngleChange(rotAngles.toFloatArray(), rotationMatrix.toFloatArray(), prevRotationMatrix.toFloatArray())
        prevRotationMatrix = rotationMatrix

        gyroSensorData.putEventData(rotAngles.toFloatArray())

        rotationVector = gyroSensorData.getData()

        val dx : Float
        val dy : Float
        if(reset) {
            backgroundOffset.zero()
            rotationVector.zero()
            gravitySensorData.clear()
            magnetometerSensorData.clear()
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
            Log.error("Cannot connect to gyro sensor.\n")
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
            backgroundOffset.zero()
            rotationVector.zero()
            gyroSensorDataBuffer.clear()
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
