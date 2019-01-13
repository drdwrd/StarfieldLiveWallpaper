package drwdrd.ktdev.starfield

import android.content.Context
import android.opengl.GLSurfaceView
import android.view.GestureDetector
import android.view.MotionEvent
import drwdrd.ktdev.engine.GLWallpaperService
import drwdrd.ktdev.engine.Log
import java.lang.ref.WeakReference

class StarfieldWallpaperService : GLWallpaperService() {

    init {
        Log.tag = "StarfieldWallpaperService"
        touchEventsEnabled = true
    }

    private lateinit var gestureDetector : GestureDetector

    override fun onTouchEvent(event: MotionEvent?) {
        super.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)
    }

    override fun createRenderer() : GLSurfaceView.Renderer {
        var renderer = StarfieldWallpaperService.rendererFactory(this)
        Settings.onSettingsChangedListener = WeakReference(renderer)
        var gestureListener = renderer.createGestureListener()
        gestureDetector = GestureDetector(this, gestureListener)
        wallpaperLiveCycleListener = renderer
        onOffsetChangedListener = renderer
        return renderer
    }

    companion object {

        fun rendererFactory(context: Context) = StarfieldRenderer(context)

    }

}
