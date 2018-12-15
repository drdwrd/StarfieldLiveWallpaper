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




class Starfield2Renderer(_context: Context) : GLSurfaceView.Renderer, GLWallpaperService.WallpaperLiveCycleListener {

    private val context : Context = _context
    private val simplePlane = Plane3D()
    private var aspect = vector2f(1.0f, 1.0f)
    private lateinit var shader : ProgramObject
    private val layers = Array(3) { Texture() }
    private var noise = Texture()
    private val timer = Timer(0.0002)
    private val sprites : MutableList<StarParticle>
    private var eye = Eye()
    private var resetGyro  = true

    private val maxParticlesCount = 500

    inner class StarfieldGestureListener : GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {
        override fun onDown(p0: MotionEvent?): Boolean {
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
            return false
        }

        override fun onDoubleTapEvent(p0: MotionEvent?): Boolean {
            return false
        }

        override fun onSingleTapConfirmed(p0: MotionEvent?): Boolean {
            return false
        }
    }


    fun createGestureListener() = StarfieldGestureListener()

    init {
        RandomGenerator.seed(RandomGenerator.createSeed())

        sprites = MutableList(maxParticlesCount) {
            return@MutableList StarParticle.createRandom2(RandomGenerator.randf(1.0f, 5.0f))
        }

        fun selector(sprite: StarParticle) : Float = sprite.position.z

        sprites.sortByDescending{ selector(it)  }
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
        shader = ProgramObject.loadFromAssets(context, "shaders/sprite2.vert", "shaders/sprite2.frag", simplePlane.vertexFormat)

        layers[0] = Texture.loadFromAssets(context, "images/star2.png", Texture.WrapMode.Repeat, Texture.WrapMode.Repeat, Texture.Filtering.LinearMipmapLinear, Texture.Filtering.Linear)

        noise = Texture.loadFromAssets(context, "images/noise.png", Texture.WrapMode.Repeat, Texture.WrapMode.Repeat, Texture.Filtering.LinearMipmapLinear, Texture.Filtering.Linear)

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

        timer.tick()

        GLES20.glDisable(GLES20.GL_CULL_FACE)
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        GLES20.glClearDepthf(1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GL10.GL_DEPTH_BUFFER_BIT)
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        GLES20.glBlendEquation(GLES20.GL_FUNC_ADD)

        val viewProjectionMatrix = eye.viewProjectionMatrix

        sprites.removeAll { it.position.z < -1.0f }

        for( i in sprites.size .. maxParticlesCount) {
            sprites.add(0, StarParticle.createRandom2(5.0f))
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
            var fadeOut = smoothstep(-1.0f, 2.5f, sprite.position.z)

            shader.setUniformValue("u_ModelViewProjectionMatrix", viewProjectionMatrix * sprite.modelMatrix)
            shader.setUniformValue("u_uvRoI", vector4f(sprite.uvRoI.left, sprite.uvRoI.top, sprite.uvRoI.width, sprite.uvRoI.height))
            shader.setUniformValue("u_FadeIn", fadeIn)
//            shader.setUniformValue("u_FadeOut", fadeOut)
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
