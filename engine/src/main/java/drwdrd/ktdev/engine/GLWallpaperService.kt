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

    interface OnOffsetChangedListener {
        fun onOffsetChanged(xOffset: Float, yOffset: Float, xOffsetStep: Float, yOffsetStep: Float, xPixelOffset: Int, yPixelOffset: Int)
    }

    interface WallpaperLiveCycleListener {
        fun onPause()
        fun onResume()
    }

    var touchEventsEnabled = false
    var wallpaperLiveCycleListener : WallpaperLiveCycleListener? = null
    var onOffsetChangedListener : OnOffsetChangedListener? = null

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

            override fun onPause() {
                wallpaperLiveCycleListener?.onPause()
                Log.debug("GLWallpaperSurfaceView.onPause()")
                super.onPause()
            }

            override fun onResume() {
                wallpaperLiveCycleListener?.onResume()
                Log.debug("GLWallpaperSurfaceView.onResume()")
                super.onResume()
            }
        }

        private lateinit var glSurfaceView : GLWallaperServiceEngine.GLWallpaperSurfaceView
        private var rendererHasBeenSet : Boolean = false

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            setTouchEventsEnabled(touchEventsEnabled)
            glSurfaceView = GLWallpaperSurfaceView(this@GLWallpaperService)
            val renderer = this@GLWallpaperService.createRenderer()
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
            glSurfaceView.onDestroy()
            super.onDestroy()
        }

        override fun onTouchEvent(event: MotionEvent?) {
            this@GLWallpaperService.onTouchEvent(event)
            super.onTouchEvent(event)
        }

        override fun onOffsetsChanged(xOffset: Float, yOffset: Float, xOffsetStep: Float, yOffsetStep: Float, xPixelOffset: Int, yPixelOffset: Int) {
            onOffsetChangedListener?.onOffsetChanged(xOffset, yOffset, xOffsetStep, yOffsetStep, xPixelOffset, yPixelOffset)
            super.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep, xPixelOffset, yPixelOffset)
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