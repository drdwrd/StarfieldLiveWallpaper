package drwdrd.ktdev.starfield

import android.opengl.GLSurfaceView
import drwdrd.ktdev.kengine.FileLogOutput
import drwdrd.ktdev.kengine.GLWallpaperService
import drwdrd.ktdev.kengine.Log

class StarfieldWallpaperService : GLWallpaperService() {

    init {
        touchEventsEnabled = true
    }

    override fun onCreate() {
        if(Log.logOutput == null && BuildConfig.DEBUG) {
            Log.logOutput = FileLogOutput(applicationContext, BuildConfig.logFileName)
        }
        super.onCreate()
    }

    override fun createRenderer() : GLSurfaceView.Renderer {
        val renderer = StarfieldRenderer.rendererInstances.createRenderer(this)
        wallpaperLiveCycleListener = renderer
        onOffsetChangedListener = renderer
        return renderer
    }
}
