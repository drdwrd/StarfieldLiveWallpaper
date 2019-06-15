package drwdrd.ktdev.starfield

import android.content.res.Configuration
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import drwdrd.ktdev.kengine.*

interface ParallaxEffectEngine {

    var reset : Boolean
    var orientation : Int
    val offset : vector3f
    val parallaxEffectScale : Float

    fun connect(sensorManager: SensorManager)
    fun disconnect(sensorManager: SensorManager)
    fun onTick(currentTime : Float, deltaTime : Float)
    fun onOffsetChanged(xOffset: Float, yOffset: Float, xOffsetStep: Float, yOffsetStep: Float, xPixelOffset: Int, yPixelOffset: Int)
}


open class ScrollingWallpaperEffectEngine : ParallaxEffectEngine {

    override var reset = false

    override var orientation = Configuration.ORIENTATION_UNDEFINED

    override var offset = vector3f(0.0f, 0.0f, 0.0f)
        protected set

    override var parallaxEffectScale = SettingsProvider.parallaxEffectMultiplier
        protected set

    private var dxOffset = 0.0f
    private var lastXOffset = 0.0f
    private var scrollingEffectScale = SettingsProvider.scrollingEffectMultiplier
    private var cameraRotationSpeed = SettingsProvider.cameraRotationSpeed
    private var parallaxEffectAcceleration = SettingsProvider.parallaxEffectAcceleration

    protected fun calculateWallpaperOffset(dx : Float, dy : Float, currentTime: Float, deltaTime: Float) {
        val cameraOffset = vector3f(0.0f, 0.0f, cameraRotationSpeed) * deltaTime
        when(orientation) {
            Configuration.ORIENTATION_PORTRAIT -> {
                offset = (vector3f(-dy,dx + dxOffset, 0.0f) + cameraOffset) * parallaxEffectAcceleration + (1.0f - parallaxEffectAcceleration) * offset
            }

            Configuration.ORIENTATION_LANDSCAPE -> {
                offset = (vector3f(-dx, dy + dxOffset, 0.0f) + cameraOffset) * parallaxEffectAcceleration + (1.0f - parallaxEffectAcceleration) * offset
            }
        }
        dxOffset = 0.0f
    }

    override fun connect(sensorManager: SensorManager) {
        cameraRotationSpeed = SettingsProvider.cameraRotationSpeed
        parallaxEffectAcceleration = SettingsProvider.parallaxEffectAcceleration
        scrollingEffectScale = if(SettingsProvider.enableScrollingEffect) {
            SettingsProvider.scrollingEffectMultiplier
        } else {
            0.0f
        }
    }

    override fun disconnect(sensorManager: SensorManager) {

    }

    override fun onOffsetChanged(xOffset: Float, yOffset: Float, xOffsetStep: Float, yOffsetStep: Float, xPixelOffset: Int, yPixelOffset: Int) {
        dxOffset = scrollingEffectScale * (xOffset - lastXOffset)
        lastXOffset = xOffset
    }

    override fun onTick(currentTime: Float, deltaTime: Float) {
        if(reset) {
            offset.zero()
            reset = false
        }
        calculateWallpaperOffset(0.0f, 0.0f, currentTime, deltaTime)
    }
}

class SensorDataBuffer(_weights : FloatArray) {

    constructor(size : Int) : this(FloatArray(size) { 1.0f })

    private val weights = _weights
    private var normalizationFactor = calculateNormalizationFactor(_weights)
    private val buffer = Array(_weights.size) { vector3f() }
    private var bufferDataPos = 0

    private fun calculateNormalizationFactor(_weights: FloatArray) : Float {
        var n = 0.0f
        _weights.forEach { n += it }
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
        for(i in 0 until buffer.size) {
            val index = (bufferDataPos + i) % buffer.size
            v += weights[i] * buffer[index]
        }
        return v / normalizationFactor
    }

}

//TODO: still wonky, remapping????
class AccelerometerParallaxEffectEngine : ScrollingWallpaperEffectEngine() {

    private val gravitySensorData = SensorDataBuffer(12)
    private val magnetometerSensorData = SensorDataBuffer(8)
    private val gyroSensorData = SensorDataBuffer(24)
    private var prevRotationMatrix = matrix3f()
    private var rotationVector = vector3f(0.0f, 0.0f, 0.0f)

    override var reset = true

    override var orientation = Configuration.ORIENTATION_UNDEFINED

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
        super.connect(sensorManager)
        parallaxEffectScale = SettingsProvider.parallaxEffectMultiplier
        if(SettingsProvider.enableParallaxEffect) {
            val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            if (!sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_GAME)) {
                loge("Cannot connect to accelerometer sensor.\n")
            }
            val magnetic = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
            if (!sensorManager.registerListener(sensorEventListener, magnetic, SensorManager.SENSOR_DELAY_GAME)) {
                loge("Cannot connect to magnetic sensor.\n")
            }
        }
    }

    override fun disconnect(sensorManager: SensorManager) {
        super.disconnect(sensorManager)
        if(SettingsProvider.enableParallaxEffect) {
            sensorManager.unregisterListener(sensorEventListener)
        }
    }

    override fun onTick(currentTime: Float, deltaTime: Float) {
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
            offset.zero()
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
        calculateWallpaperOffset(dx, dy,currentTime, deltaTime)
    }
}

//TODO: still wonky, remapping????
class GravityParallaxEffectEngine : ScrollingWallpaperEffectEngine() {

    private val gravitySensorData = SensorDataBuffer(8)
    private val magnetometerSensorData = SensorDataBuffer(8)
    private val gyroSensorData = SensorDataBuffer(24)
    private var prevRotationMatrix = matrix3f()
    private var rotationVector = vector3f(0.0f, 0.0f, 0.0f)

    override var reset = true

    override var orientation = Configuration.ORIENTATION_UNDEFINED

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
        super.connect(sensorManager)
        parallaxEffectScale = SettingsProvider.parallaxEffectMultiplier
        if(SettingsProvider.enableParallaxEffect) {
            val gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
            if (!sensorManager.registerListener(sensorEventListener, gravitySensor, SensorManager.SENSOR_DELAY_GAME)) {
                loge("Cannot connect to gravity sensor.\n")
            }
            val magnetic = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
            if (!sensorManager.registerListener(sensorEventListener, magnetic, SensorManager.SENSOR_DELAY_GAME)) {
                loge("Cannot connect to magnetic sensor.\n")
            }
        }
    }

    override fun disconnect(sensorManager: SensorManager) {
        super.disconnect(sensorManager)
        if(SettingsProvider.enableParallaxEffect) {
            sensorManager.unregisterListener(sensorEventListener)
        }
    }

    override fun onTick(currentTime: Float, deltaTime: Float) {
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
            offset.zero()
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
        calculateWallpaperOffset(dx, dy,currentTime, deltaTime)
    }
}

class GyroParallaxEffectEngine : ScrollingWallpaperEffectEngine() {

    override var reset = true

    override var orientation = Configuration.ORIENTATION_UNDEFINED

    private var rotationVector = vector3f(0.0f, 0.0f, 0.0f)
    private val gyroSensorDataBuffer = SensorDataBuffer(12)

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
        super.connect(sensorManager)
        parallaxEffectScale = SettingsProvider.parallaxEffectMultiplier
        if(SettingsProvider.enableParallaxEffect) {
            val gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
            if (!sensorManager.registerListener(gyroSensorListener, gyro, SensorManager.SENSOR_DELAY_GAME)) {
                loge("Cannot connect to gyro sensor.\n")
            }
        }
    }

    override fun disconnect(sensorManager: SensorManager) {
        super.disconnect(sensorManager)
        if(SettingsProvider.enableParallaxEffect) {
            sensorManager.unregisterListener(gyroSensorListener)
        }
    }

    override fun onTick(currentTime: Float, deltaTime: Float) {
        val dx : Float
        val dy : Float
        rotationVector = gyroSensorDataBuffer.getData()
        if(reset) {
            offset.zero()
            rotationVector.zero()
            gyroSensorDataBuffer.clear()
            dx = 0.0f
            dy = 0.0f
            reset = false
        } else {
            dx = parallaxEffectScale * rotationVector[1] * deltaTime
            dy = -parallaxEffectScale * rotationVector[0] * deltaTime
        }
        calculateWallpaperOffset(dx, dy,currentTime, deltaTime)
    }
}
