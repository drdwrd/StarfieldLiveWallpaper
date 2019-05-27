package drwdrd.ktdev.starfield

import android.opengl.GLSurfaceView
import drwdrd.ktdev.engine.FileLogOutput
import drwdrd.ktdev.engine.GLWallpaperService
import drwdrd.ktdev.engine.Log

class StarfieldWallpaperService : GLWallpaperService() {

    init {
        touchEventsEnabled = true
    }

    override fun onCreate() {
        if(Log.logOutput == null && BuildConfig.DEBUG) {
            Log.logOutput = FileLogOutput(applicationContext, "starfield.log")
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
