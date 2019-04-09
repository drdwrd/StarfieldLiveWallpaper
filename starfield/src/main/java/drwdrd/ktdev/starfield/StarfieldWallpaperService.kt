package drwdrd.ktdev.starfield

import android.opengl.GLSurfaceView
import drwdrd.ktdev.engine.GLWallpaperService

class StarfieldWallpaperService : GLWallpaperService() {

    init {
        touchEventsEnabled = true
    }

    override fun createRenderer() : GLSurfaceView.Renderer {
        val renderer = StarfieldRenderer.createRenderer(this)
        renderer.theme = ThemePackage(this, "starfield2")
        wallpaperLiveCycleListener = renderer
        onOffsetChangedListener = renderer
        return renderer
    }
}
