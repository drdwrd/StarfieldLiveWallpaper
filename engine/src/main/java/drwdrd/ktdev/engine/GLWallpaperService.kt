package drwdrd.ktdev.engine

import android.content.Context
import android.opengl.GLSurfaceView
import android.service.wallpaper.WallpaperService
import android.view.MotionEvent
import android.view.SurfaceHolder


abstract class GLWallpaperService : WallpaperService() {

    init {
        Log.tag = "GLWallpaperService"
    }

    var touchEventsEnabled = false

    inner class GLWallaperServiceEngine : Engine() {

        inner class GLWallpaperSurfaceView(context: Context) : GLSurfaceView(context) {

            init {
                setEGLContextClientVersion(2)
                preserveEGLContextOnPause = true
            }

            override fun getHolder(): SurfaceHolder {
                return surfaceHolder
            }

            fun onDestroy() {
                super.onDetachedFromWindow()
            }
        }

        private lateinit var glSurfaceView : GLWallaperServiceEngine.GLWallpaperSurfaceView
        private var rendererHasBeenSet : Boolean = false

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            setTouchEventsEnabled(touchEventsEnabled)
            glSurfaceView = GLWallpaperSurfaceView(this@GLWallpaperService)
            val renderer = this@GLWallpaperService.createRenderer()!!
            setRenderer(renderer)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            if(rendererHasBeenSet) {
                if(visible) {
                    glSurfaceView.onResume()
                } else {
                    glSurfaceView.onPause()
                }
            }
        }

        override fun onDestroy() {
            super.onDestroy()
            glSurfaceView.onDestroy()
        }

        override fun onTouchEvent(event: MotionEvent?) {
            super.onTouchEvent(event)
            this@GLWallpaperService.onTouchEvent(event)
        }

        private fun setRenderer(renderer : GLSurfaceView.Renderer) {
            glSurfaceView.setRenderer(renderer)
            glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
            rendererHasBeenSet = true
        }
    }

    override fun onCreateEngine(): Engine {
        return GLWallaperServiceEngine()
    }

    open fun onTouchEvent(event: MotionEvent?) { }

    abstract fun createRenderer() : GLSurfaceView.Renderer
}