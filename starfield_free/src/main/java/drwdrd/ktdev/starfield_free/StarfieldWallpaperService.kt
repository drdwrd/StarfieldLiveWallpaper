package drwdrd.ktdev.starfield_free

import android.opengl.GLSurfaceView
import drwdrd.ktdev.engine.GLWallpaperService

class StarfieldWallpaperService : GLWallpaperService() {

    init {
        touchEventsEnabled = true
    }

    override fun createRenderer() : GLSurfaceView.Renderer {
        val renderer = StarfieldRenderer.createRenderer(this)
        wallpaperLiveCycleListener = renderer
        onOffsetChangedListener = renderer
        return renderer
    }
}
