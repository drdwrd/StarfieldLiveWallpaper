package drwdrd.ktdev.starfield

import android.app.Activity
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.Window
import android.view.WindowManager
import drwdrd.ktdev.engine.Log

class StarfieldActivity : Activity() {

    init {
        Log.tag = "SimplicityWallpaper"
    }

    private lateinit var glSurfaceView : GLSurfaceView
    private lateinit var gestureDetector: GestureDetector
    private lateinit var renderer: StarfieldRenderer


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        glSurfaceView = GLSurfaceView(this)
        glSurfaceView.setEGLContextClientVersion(2)
        glSurfaceView.preserveEGLContextOnPause = true
        renderer = StarfieldRenderer(this)
        glSurfaceView.setRenderer(renderer)
        glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        val gestureListener = renderer.createGestureListener()
        gestureDetector = GestureDetector(this, gestureListener)
        gestureDetector.setOnDoubleTapListener(gestureListener)
        setContentView(glSurfaceView)
    }

    override fun onResume() {
        super.onResume()
        glSurfaceView.onResume()
        renderer.onResume()
    }

    override fun onPause() {
        super.onPause()
        glSurfaceView.onPause()
        renderer.onPause()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)
        return gestureDetector.onTouchEvent(event)
    }
}