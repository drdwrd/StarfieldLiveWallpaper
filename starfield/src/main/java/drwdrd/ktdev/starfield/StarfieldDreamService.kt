package drwdrd.ktdev.starfield

import android.opengl.GLSurfaceView
import android.service.dreams.DreamService
import drwdrd.ktdev.engine.FileLogOutput
import drwdrd.ktdev.engine.GLWallpaperService
import drwdrd.ktdev.engine.Log

class StarfieldDreamService : DreamService() {

    private lateinit var glSurfaceView : GLSurfaceView
    private lateinit var liveCycleListener: GLWallpaperService.WallpaperLiveCycleListener

    override fun onCreate() {
        if(Log.logOutput == null) {
            Log.logOutput = FileLogOutput(applicationContext, "starfield.log")
        }
        glSurfaceView = GLSurfaceView(this)
        super.onCreate()
    }

    override fun onAttachedToWindow() {
        isInteractive = true
        isFullscreen = true
        glSurfaceView.setEGLContextClientVersion(2)
        glSurfaceView.preserveEGLContextOnPause = true
        val renderer = StarfieldRenderer.rendererInstances.createRenderer(this)
        liveCycleListener = renderer
        glSurfaceView.setRenderer(renderer)
        glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        setContentView(glSurfaceView)
        liveCycleListener.onStart()
        super.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        liveCycleListener.onStop()
        super.onDetachedFromWindow()
    }

    override fun onDreamingStarted() {
        glSurfaceView.onResume()
        liveCycleListener.onResume()
        super.onDreamingStarted()
    }

    override fun onDreamingStopped() {
        glSurfaceView.onPause()
        liveCycleListener.onPause()
        super.onDreamingStopped()
    }

}
