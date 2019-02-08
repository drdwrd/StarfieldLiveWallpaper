package drwdrd.ktdev.starfield

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import drwdrd.ktdev.engine.*
import kotlin.math.sign



interface StarfieldParallaxEffect {

    var reset : Boolean
    val offset : vector3f
    val backgroundOffset : vector2f
    val parallaxEffectScale : Float
    val rotationMatrix : matrix4f

    fun connect(sensorManager: SensorManager)
    fun disconnect(sensorManager: SensorManager)
    fun onTick()
    fun onOffsetChanged(xOffset: Float, yOffset: Float, xOffsetStep: Float, yOffsetStep: Float, xPixelOffset: Int, yPixelOffset: Int)
}

private const val kFilter = 0.8f
private const val gravityFilter = 0.8f         //TODO : move to settings???
private const val magFilter = 0.8f

class StarfieldParallaxEffectQ : StarfieldParallaxEffect {


    private var rotationVector  = vector4f()

    override var backgroundOffset = vector2f(0.0f, 0.0f)
        private set

    override var offset = vector3f(0.0f, 0.0f, 0.0f)
        private set

    override var parallaxEffectScale = SettingsProvider.parallaxEffectMultiplier
        private set

    override var reset = true

    override var rotationMatrix = matrix4f()
        private set

    inner class RotationVectorSensorListener : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

        }

        override fun onSensorChanged(event: SensorEvent?) {
            when (event?.sensor?.type) {
                Sensor.TYPE_ROTATION_VECTOR -> {
                    rotationVector[0] = kFilter * rotationVector[0] + (1.0f - kFilter) * event.values[0]
                    rotationVector[1] = kFilter * rotationVector[0] + (1.0f - kFilter) * event.values[1]
                    rotationVector[2] = kFilter * rotationVector[0] + (1.0f - kFilter) * event.values[2]
                    rotationVector[3] = kFilter * rotationVector[0] + (1.0f - kFilter) * event.values[3]
                }
            }
        }
    }

    private val rotationVectorSensorListener = RotationVectorSensorListener()

    override fun connect(sensorManager: SensorManager) {
        parallaxEffectScale = SettingsProvider.parallaxEffectMultiplier
        rotationMatrix.loadIdentity()
        val rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val s = sensorManager.registerListener(rotationVectorSensorListener, rotationVectorSensor, SensorManager.SENSOR_DELAY_GAME)
        check(s)
    }

    override fun disconnect(sensorManager: SensorManager) {
        sensorManager.unregisterListener(rotationVectorSensorListener)
    }

    override fun onOffsetChanged(xOffset: Float, yOffset: Float, xOffsetStep: Float, yOffsetStep: Float, xPixelOffset: Int, yPixelOffset: Int) {

    }

    override fun onTick() {
        val dg : vector2f
        if(reset) {
            backgroundOffset = vector2f(0.0f, 0.0f)
            rotationMatrix.loadIdentity()
            dg = vector2f(0.0f, 0.0f)
            reset = false
        } else {
            val v = vector3f()
            val q = quaternion(rotationVector[0], rotationVector[1], rotationVector[2], rotationVector[3])
            val rm = q.getRotationMatrix()
            SensorManager.remapCoordinateSystem(rm.toFloatArray(), SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, rotationMatrix.toFloatArray())
            SensorManager.getOrientation(rotationMatrix.toFloatArray(), v.toFloatArray())
            if(v[2].isNaN()) {
                v[2] = 0.0f
            }
            if(v[1].isNaN()) {
                v[1] = 0.0f
            }
            dg = vector2f(parallaxEffectScale * v[2], parallaxEffectScale * v[1])
        }
        val dxOffset = 0.0f
        backgroundOffset = dg
//        backgroundOffset.plusAssign(vector2f(dxOffset, 0.0f))
        offset = vector3f(-dg.y,dg.x + dxOffset, 0.0f)
//        dxOffset = precessionSpeed
    }
}

class StarfieldParallaxEffect3D : StarfieldParallaxEffect {
    var gravityVector = vector3f(0.0f, 0.0f, 0.0f)
    var magVector = vector3f(0.0f, 0.0f, 0.0f)

    override var rotationMatrix = matrix4f()
        private set

    override var reset = true

    override var offset = vector3f(0.0f, 0.0f, 0.0f)
        private set

    override var backgroundOffset = vector2f(0.0f, 0.0f)
        private set

    override var parallaxEffectScale = SettingsProvider.parallaxEffectMultiplier
        private set

    inner class SensorsEventListener : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { }

        override fun onSensorChanged(event: SensorEvent?) {
            when(event?.sensor?.type) {
                Sensor.TYPE_GRAVITY -> {
                    gravityVector[0] = gravityVector[0] * gravityFilter + (1.0f - gravityFilter) * event.values[0]
                    gravityVector[1] = gravityVector[1] * gravityFilter + (1.0f - gravityFilter) * event.values[1]
                    gravityVector[2] = gravityVector[2] * gravityFilter + (1.0f - gravityFilter) * event.values[2]
                }
                Sensor.TYPE_MAGNETIC_FIELD -> {
                    magVector[0] = magVector[0] * magFilter + (1.0f - magFilter) * event.values[0]
                    magVector[1] = magVector[1] * magFilter + (1.0f - magFilter) * event.values[1]
                    magVector[2] = magVector[2] * magFilter + (1.0f - magFilter) * event.values[2]
                }
            }
        }
    }

    private val sensorsEventListener = SensorsEventListener()

    override fun connect(sensorManager: SensorManager) {
        parallaxEffectScale = SettingsProvider.parallaxEffectMultiplier
        rotationMatrix.loadIdentity()
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
        val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        val acc = sensorManager.registerListener(sensorsEventListener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        check(acc)
        val mag = sensorManager.registerListener(sensorsEventListener, magnetometer, SensorManager.SENSOR_DELAY_GAME)
        check(mag)
    }

    override fun disconnect(sensorManager: SensorManager) {
        sensorManager.unregisterListener(sensorsEventListener)
    }

    override fun onOffsetChanged(xOffset: Float, yOffset: Float, xOffsetStep: Float, yOffsetStep: Float, xPixelOffset: Int, yPixelOffset: Int) {

    }

    override fun onTick() {
//        var dg = vector2f(0.0f, 0.0f)
        if(reset) {
            rotationMatrix.loadIdentity()
            backgroundOffset = vector2f(0.0f, 0.0f)
            offset = vector3f(0.0f, 0.0f, 0.0f)
            reset = false
        } else {
            val rm = matrix4f()
            val im = matrix4f()
            SensorManager.getRotationMatrix(rm.toFloatArray(), im.toFloatArray(), gravityVector.toFloatArray(), magVector.toFloatArray())
//            SensorManager.remapCoordinateSystem(rm.toFloatArray(), SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, rotationMatrix.toFloatArray())

            val v = vector3f(0.0f, 0.0f ,0.0f)
            SensorManager.getOrientation(rm.toFloatArray(), v.toFloatArray())

            val azimuth = v[0]
            val pitch = v[1]
            val roll = v[2]
            backgroundOffset = vector2f(roll, pitch)
            offset = vector3f(-pitch, roll, 0.0f)
//            rotationMatrix.setEulerRotation(-pitch, roll, 0.0f)

            rotationMatrix = rm

            // Orientation isn't as useful as a rotation matrix, but
            // we'll show it here anyway.
//            val v = vector3f(0.0f ,0.0f ,0.0f)
//            SensorManager.getOrientation(rotationMatrix.toFloatArray(), v.toFloatArray())
//            dg = vector2f(parallaxEffectScale * v[2], parallaxEffectScale * v[1])
        }
//        offset = vector3f(dg.x, 0.0f, 0.0f)
//        backgroundOffset = vector2f(-dg.y, dg.x)
    }
}

class StarfieldParallaxEffectSimple : StarfieldParallaxEffect {

    private val gravityVector = vector3f(0.0f, 0.0f, 0.0f)
    private var lastGravity = vector3f(0.0f,0.0f, 0.0f)
    private var lastXOffset = 0.0f
    private var dxOffset = 0.0f
    private var precessionSpeed = 0.0f

    override var reset = true

    override var offset = vector3f(0.0f, 0.0f, 0.0f)
        private set

    override var backgroundOffset = vector2f(0.0f, 0.0f)
        private set

    override var parallaxEffectScale = SettingsProvider.parallaxEffectMultiplier
        private set

    override var rotationMatrix = matrix4f()
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
        rotationMatrix.loadIdentity()
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
        val s = sensorManager.registerListener(accelerometerSensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        check(s)
    }

    override fun disconnect(sensorManager: SensorManager) {
        sensorManager.unregisterListener(accelerometerSensorEventListener)
    }

    override fun onTick() {
        val g = gravityVector.normalized()
        val dg : vector2f
        if(reset) {
            backgroundOffset = vector2f(0.0f, 0.0f)
            rotationMatrix.loadIdentity()
            dg = vector2f(0.0f, 0.0f)
            reset = false
        } else {
            val v = vector3f()
            rotationMatrix.setAxisRotation(g, lastGravity)
            SensorManager.getOrientation(rotationMatrix.toFloatArray(), v.toFloatArray())
            dg = vector2f(parallaxEffectScale * v[2], parallaxEffectScale * v[1])
        }
        backgroundOffset.plusAssign(dg)
        backgroundOffset.plusAssign(vector2f(dxOffset, 0.0f))
        offset = vector3f(-dg.y,dg.x + dxOffset, 0.0f)
        dxOffset = precessionSpeed
        lastGravity = g
    }

    override fun onOffsetChanged(xOffset: Float, yOffset: Float, xOffsetStep: Float, yOffsetStep: Float, xPixelOffset: Int, yPixelOffset: Int) {
        dxOffset = 0.05f * parallaxEffectScale * (xOffset - lastXOffset)
        lastXOffset = xOffset
        precessionSpeed = sign(dxOffset) * SettingsProvider.precessionSpeed
    }
}

class StarfieldParallaxEffectS3D : StarfieldParallaxEffect {

    private val gravityVector = vector3f(0.0f, 0.0f, 0.0f)
    private val magVector = vector3f(0.0f, 0.0f ,0.0f)
    private var lastRotation = vector3f(0.0f,0.0f, 0.0f)
    private var lastXOffset = 0.0f
    private var dxOffset = 0.0f
    private var precessionSpeed = 0.0f

    override var reset = true

    override var offset = vector3f(0.0f, 0.0f, 0.0f)
        private set

    override var backgroundOffset = vector2f(0.0f, 0.0f)
        private set

    override var parallaxEffectScale = SettingsProvider.parallaxEffectMultiplier
        private set

    override var rotationMatrix = matrix4f()
        private set

    inner class SensorsEventListener : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { }

        override fun onSensorChanged(event: SensorEvent?) {
            when(event?.sensor?.type) {
                Sensor.TYPE_GRAVITY -> {
                    gravityVector[0] = gravityVector[0] * gravityFilter + (1.0f - gravityFilter) * event.values[0]
                    gravityVector[1] = gravityVector[1] * gravityFilter + (1.0f - gravityFilter) * event.values[1]
                    gravityVector[2] = gravityVector[2] * gravityFilter + (1.0f - gravityFilter) * event.values[2]
                }
                Sensor.TYPE_MAGNETIC_FIELD -> {
                    magVector[0] = magVector[0] * magFilter + (1.0f - magFilter) * event.values[0]
                    magVector[1] = magVector[1] * magFilter + (1.0f - magFilter) * event.values[1]
                    magVector[2] = magVector[2] * magFilter + (1.0f - magFilter) * event.values[2]
                }
            }
        }
    }

    private val sensorsEventListener = SensorsEventListener()

    override fun connect(sensorManager: SensorManager) {
        parallaxEffectScale = SettingsProvider.parallaxEffectMultiplier
        rotationMatrix.loadIdentity()
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
        val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        val acc = sensorManager.registerListener(sensorsEventListener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        check(acc)
        val mag = sensorManager.registerListener(sensorsEventListener, magnetometer, SensorManager.SENSOR_DELAY_GAME)
        check(mag)
    }

    override fun disconnect(sensorManager: SensorManager) {
        sensorManager.unregisterListener(sensorsEventListener)
    }

    override fun onTick() {
        val g = gravityVector.normalized()
        val m = magVector.normalized()
        val r = vector3f.cross(g, m)
        val dr : vector2f
        if(reset) {
            backgroundOffset = vector2f(0.0f, 0.0f)
            rotationMatrix.loadIdentity()
            dr = vector2f(0.0f, 0.0f)
            reset = false
        } else {
            val v = vector3f()
            rotationMatrix.setAxisRotation(r, lastRotation)
            SensorManager.getOrientation(rotationMatrix.toFloatArray(), v.toFloatArray())
            dr = vector2f(parallaxEffectScale * v[2], parallaxEffectScale * v[1])
        }
        backgroundOffset.plusAssign(dr)
        backgroundOffset.plusAssign(vector2f(dxOffset, 0.0f))
        offset = vector3f(-dr.y,dr.x + dxOffset, 0.0f)
        dxOffset = precessionSpeed
        lastRotation = r
    }

    override fun onOffsetChanged(xOffset: Float, yOffset: Float, xOffsetStep: Float, yOffsetStep: Float, xPixelOffset: Int, yPixelOffset: Int) {
        dxOffset = 0.05f * parallaxEffectScale * (xOffset - lastXOffset)
        lastXOffset = xOffset
        precessionSpeed = sign(dxOffset) * SettingsProvider.precessionSpeed
    }
}