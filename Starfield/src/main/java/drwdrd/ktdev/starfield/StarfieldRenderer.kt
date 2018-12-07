package drwdrd.ktdev.starfield

import android.content.Context
import android.content.res.Configuration
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
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


const val gravityFilter = 0.8f
const val maxParticlesCount = 500

class StarfieldRenderer(_context: Context) : GLSurfaceView.Renderer, GLWallpaperService.WallpaperLiveCycleListener {

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
    private val simplePlane = Plane3D()
    private val sensorEventListener = StarfieldSensorEventListener()
    private val gravityVector = vector3f(0.0f, 0.0f, 0.0f)
    private val lastGravity = vector2f(0.0f, 0.0f)
    private var aspect = vector2f(1.0f, 1.0f)
    private lateinit var shader : ProgramObject
    private val layers = Array(3) { Texture() }
    private var noise = Texture()
    private val uvOffset = vector2f(0.0f, 0.0f)
    private val timer = Timer(0.001)
    private lateinit var sprites : MutableList<StarParticle>
    private var eye = Eye()
    private var resetGyro  = true

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

        if(context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            var tmp = dg[1]
            dg[1] = dg[0]
            dg[0] = tmp
        }

        lastGravity[0] = roll
        lastGravity[1] = pitch

        return dg
    }

    fun createGestureListener() = StarfieldGestureListener()

    init {
        RandomGenerator.seed(RandomGenerator.createSeed())

        sprites = MutableList(maxParticlesCount) {
            return@MutableList StarParticle.createRandom(RandomGenerator.randf(1.0f, 10.0f))
        }

        fun selector(sprite: StarParticle) : Float = sprite.position.z

        sprites.sortByDescending { selector(it)  }
    }


    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        Log.debug("onSurfaceCreated()")

        val version = GLES20.glGetString(GLES20.GL_VERSION)
        val vendor = GLES20.glGetString(GLES20.GL_VENDOR)
        val renderer = GLES20.glGetString(GLES20.GL_RENDERER)

        Log.info("OpenGL version: $version")
        Log.info("OpenGL vendor: $vendor")
        Log.info("OpenGL renderer: $renderer")

        simplePlane.create()
        shader = ProgramObject.loadFromAssets(context, "shaders/sprite.vert", "shaders/sprite.frag", simplePlane.vertexFormat)

        layers[0] = Texture.loadFromAssets(context, "images/star.png", Texture.WrapMode.Repeat, Texture.WrapMode.Repeat, Texture.Filtering.LinearMipmapLinear, Texture.Filtering.Linear)
        layers[1] = Texture.loadFromAssets(context, "images/stars_tex.png", Texture.WrapMode.Repeat, Texture.WrapMode.Repeat, Texture.Filtering.LinearMipmapLinear, Texture.Filtering.Linear)
        layers[2] = Texture.loadFromAssets(context, "images/stars_tex.png", Texture.WrapMode.Repeat, Texture.WrapMode.Repeat, Texture.Filtering.LinearMipmapLinear, Texture.Filtering.Linear)

        noise = Texture.loadFromAssets(context, "images/noise.png", Texture.WrapMode.Repeat, Texture.WrapMode.Repeat, Texture.Filtering.LinearMipmapLinear, Texture.Filtering.Linear)

        var sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        var gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)

        sensorManager.registerListener(sensorEventListener, gravitySensor, SensorManager.SENSOR_DELAY_FASTEST)

        eye.setPerspective(50.0f, 0.0f, 100.0f)

        //TODO: z of camera position as Camera Distance settings in customization
        eye.setLookAt(vector3f(0.0f, 0.0f, -1.0f), vector3f(0.0f, 0.0f, 0.0f), vector3f(0.0f, 1.0f, 0.0f))

        timer.reset()
    }


    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        resetGyro = true
        eye.setViewport(vector2f(width.toFloat(), height.toFloat()))
        if(width < height) {
            aspect = vector2f(width.toFloat() / height.toFloat(), 1.0f)
        } else {
            aspect = vector2f(1.0f, height.toFloat() / width.toFloat())
        }
        Log.debug("onSurfaceChanged(width = $width, height = $height)")
    }

    override fun onDrawFrame(p0: GL10?) {
        if(resetGyro) {
            calculateGyroEffect()
            resetGyro = false
        }
        var dg = 0.002f * calculateGyroEffect()

        uvOffset += vector2f(-dg.x, dg.y)

        timer.tick()

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        GLES20.glClearDepthf(1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GL10.GL_DEPTH_BUFFER_BIT)
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE)
        GLES20.glBlendEquation(GLES20.GL_FUNC_ADD)

        val viewProjectionMatrix = eye.viewProjectionMatrix

        sprites.removeAll { it.position.z < -1.0f }

        for( i in sprites.size .. maxParticlesCount) {
            sprites.add(StarParticle.createRandom())
        }

        shader.bind()
        simplePlane.bind()
        layers[0].bind(0)
        noise.bind(1)
        shader.setSampler("u_Layer0", 0)
//        shader.setSampler("u_Noise", 1)
        var it = sprites.iterator()
        while(it.hasNext()) {

            var sprite = it.next()

            //face dir
            var dir = eye.position - sprite.position
            dir.normalize()

            //normal
            var normal = vector3f(0.0f, 0.0f , -1.0f)

            var rotMatrix = matrix4f()
            rotMatrix.setAxisRotation(dir, normal)




            var fadeIn = smoothstep(0.0f, 1.0f, sprite.age)
            var fadeOut = smoothstep(0.0f, 1.0f, sprite.position.z)


            shader.setUniformValue("u_ModelViewProjectionMatrix", viewProjectionMatrix * sprite.modelMatrix * rotMatrix)
            shader.setUniformValue("u_uvRoI", vector4f(sprite.uvRoI.left, sprite.uvRoI.top, sprite.uvRoI.width, sprite.uvRoI.height))
            shader.setUniformValue("u_Fade", fadeIn)
            simplePlane.draw()

            sprite.tick(timer.deltaTime.toFloat())
        }
        layers[0].release(0)
        noise.release(1)
        simplePlane.release()
        shader.release()

        GLES20.glDisable(GLES20.GL_BLEND)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
    }

    override fun onResume() {
        timer.reset()
    }

    override fun onPause() {
    }
}
