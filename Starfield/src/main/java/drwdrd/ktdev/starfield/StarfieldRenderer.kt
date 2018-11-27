package drwdrd.ktdev.starfield

import android.content.Context
import android.hardware.Sensor
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.SystemClock
import android.view.GestureDetector
import android.view.MotionEvent
import drwdrd.ktdev.engine.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import android.hardware.SensorManager
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import kotlin.math.atan2
import kotlin.math.sqrt


const val gravityFilter = 0.8f

class StarfieldRenderer(_context: Context) : GLSurfaceView.Renderer {

    inner class StarfieldSensorEventListener : SensorEventListener {

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

        }

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

    inner class StarfieldGestureListener : GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {
        override fun onDown(p0: MotionEvent?): Boolean {
//            var x = p0?.rawX!!.toFloat() / context.resources.displayMetrics.widthPixels.toFloat()
//            var y = p0?.rawY!!.toFloat() / context.resources.displayMetrics.heightPixels.toFloat()
//            Log.debug("onDown() ... dropCenter = ($x, $y)")
            return false
        }

        override fun onFling(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean {
            return false
        }

        override fun onLongPress(p0: MotionEvent?) {

        }

        override fun onScroll(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean {
            return false
        }

        override fun onShowPress(p0: MotionEvent?) {

        }

        override fun onSingleTapUp(p0: MotionEvent?): Boolean {
            return false
        }

        override fun onDoubleTap(p0: MotionEvent?): Boolean {
            Log.debug("onDoubleTap()")
            return true
        }

        override fun onDoubleTapEvent(p0: MotionEvent?): Boolean {
            return false
        }

        override fun onSingleTapConfirmed(p0: MotionEvent?): Boolean {
            return false
        }
    }

    private val context : Context = _context
    private val simplePlane = SimplePlane()
    private val sensorEventListener = StarfieldSensorEventListener()
    private val gravityVector = vector3f(0.0f, 0.0f, 0.0f)
    private val lastGravity = vector2f(0.0f, 0.0f)
    private var aspect = vector2f(1.0f, 1.0f)
    private lateinit var shader : ProgramObject
    private var layers = Array(3) { Texture() }
    private var startTime : Long = 0
    private var dropStartTime : Long = 0
    private var dropCenter = vector2f(0.5f, 0.5f)


    fun calculateGyroEffect() : vector2f {

        var g = gravityVector.normalized()
        var roll : Float = 0.0f
        var pitch : Float

        if(g.z != 0.0f) {
            roll = atan2(g.x, g.z) * 180.0f / Math.PI.toFloat()
        }

        pitch = sqrt(g.x * g.x + g.z * g.z)

        if(pitch != 0.0f) {
            pitch = atan2(g.y, pitch) * 180.0f / Math.PI.toFloat()
        }

        var dg = vector2f()

        dg[0] = (roll - lastGravity[0])

        dg[1] = (pitch - lastGravity[1])

// if device orientation is close to vertical – rotation around x is almost undefined – skip!

        if(g.y > 0.99) dg[0] = 0.0f

// if rotation was too intensive – more than 180 degrees – skip it

        if(dg[0] > 180.0f) dg[0] = 0.0f

        if(dg[0] < -180.0f) dg[0] = 0.0f

        if(dg[1] > 180.0f) dg[1] = 0.0f

        if(dg[1] < -180.0f) dg[1] = 0.0f

        /*
        if (!screen->isPortrait())

        {

            // Its landscape mode – swap dgX and dgY

            double temp = dgY;

            dgY = dgX;

            dgX = temp;

        }
*/
        lastGravity[0] = roll
        lastGravity[1] = pitch

        return dg
    }

    fun createGestureListener() = StarfieldGestureListener()

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        Log.debug("onSurfaceCreated()")

        val version = GLES20.glGetString(GLES20.GL_VERSION)
        val vendor = GLES20.glGetString(GLES20.GL_VENDOR)
        val renderer = GLES20.glGetString(GLES20.GL_RENDERER)

        Log.info("OpenGL version: $version")
        Log.info("OpenGL vendor: $vendor")
        Log.info("OpenGL renderer: $renderer")

        simplePlane.create()
        shader = ProgramObject.loadFromAssets(context, "shaders/starfield.vert", "shaders/starfield.frag", simplePlane.vertexFormat)

        layers[0] = Texture.loadFromAssets(context, "images/stars_layer0.png", Texture.WrapMode.Repeat, Texture.WrapMode.Repeat, Texture.Filtering.LinearMipmapLinear, Texture.Filtering.Linear)
        layers[1] = Texture.loadFromAssets(context, "images/stars_layer1.png", Texture.WrapMode.Repeat, Texture.WrapMode.Repeat, Texture.Filtering.LinearMipmapLinear, Texture.Filtering.Linear)
        layers[2] = Texture.loadFromAssets(context, "images/stars_layer2.png", Texture.WrapMode.Repeat, Texture.WrapMode.Repeat, Texture.Filtering.LinearMipmapLinear, Texture.Filtering.Linear)

        var sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        var gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)

        sensorManager.registerListener(sensorEventListener, gravitySensor, SensorManager.SENSOR_DELAY_FASTEST)

        startTime = SystemClock.uptimeMillis()
    }


    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        if(width < height) {
            aspect = vector2f(width.toFloat() / height.toFloat(), 1.0f)
        } else {
            aspect = vector2f(1.0f, height.toFloat() / width.toFloat())
        }
        Log.debug("onSurfaceChanged(width = $width, height = $height)")
    }

    override fun onDrawFrame(p0: GL10?) {
        //var dg = calculateGyroEffect()

        var currentTime = SystemClock.uptimeMillis()

        val deltaTime = 0.0001f * (currentTime - startTime).toFloat()

        var dropTime = 0.0001f * (currentTime - dropStartTime).toFloat()

        if(dropTime > 2.0f) {
            dropStartTime = currentTime
            dropCenter = RandomGenerator.rand2f(0.0f, 1.0f)
        }

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)


        shader.bind()
        layers[0].bind(0)
        layers[1].bind(1)
        layers[2].bind(2)
        shader.setSampler("u_Layer0", 0)
        shader.setSampler("u_Layer1", 1)
        shader.setSampler("u_Layer2", 2)
        shader.setUniformValue("u_Time", deltaTime)
        shader.setUniformValue("u_Aspect", aspect)
        shader.setUniformValue("u_DropTime", dropTime)
        shader.setUniformValue("u_DropCenter", dropCenter)
        simplePlane.draw()
        layers[0].release(0)
        layers[1].release(1)
        layers[2].release(2)
        shader.release()
    }
}
