package drwdrd.ktdev.starfield_free

import android.opengl.GLSurfaceView
import drwdrd.ktdev.engine.GLWallpaperService
import drwdrd.ktdev.engine.Log

class StarfieldWallpaperService : GLWallpaperService() {

    init {
        Log.tag = "StarfieldWallpaperService"
        touchEventsEnabled = true
    }

    override fun createRenderer() : GLSurfaceView.Renderer {
        val renderer = StarfieldRenderer.createRenderer(this)
        wallpaperLiveCycleListener = renderer
        onOffsetChangedListener = renderer
        return renderer
    }
}
