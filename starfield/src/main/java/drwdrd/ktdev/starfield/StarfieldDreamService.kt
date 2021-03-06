package drwdrd.ktdev.starfield

import android.opengl.GLSurfaceView
import android.service.dreams.DreamService
import drwdrd.ktdev.kengine.FileLogOutput
import drwdrd.ktdev.kengine.GLWallpaperService
import drwdrd.ktdev.kengine.Log

class StarfieldDreamService : DreamService() {

    private lateinit var glSurfaceView : GLSurfaceView
    private lateinit var liveCycleListener: GLWallpaperService.WallpaperLiveCycleListener

    override fun onCreate() {
        if(Log.logOutput == null && BuildConfig.DEBUG) {
            Log.logOutput = FileLogOutput(applicationContext, BuildConfig.logFileName)
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
