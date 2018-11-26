package drwdrd.ktdev.starfield

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.SystemClock
import android.view.GestureDetector
import android.view.MotionEvent
import drwdrd.ktdev.engine.*
import java.io.InputStream
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class StarfieldRenderer(_context: Context) : GLSurfaceView.Renderer {

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
    private var aspect = vector2f(1.0f, 1.0f)
    private lateinit var shader : ProgramObject
    private var layers = Array(3) { Texture() }
    private var startTime : Long = 0


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

        val deltaTime = 0.0001f * (SystemClock.uptimeMillis() - startTime).toFloat()

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
        simplePlane.draw()
        layers[0].release(0)
        layers[1].release(1)
        layers[2].release(2)
        shader.release()
    }
}
