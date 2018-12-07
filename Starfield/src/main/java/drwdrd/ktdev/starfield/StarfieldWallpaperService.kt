package drwdrd.ktdev.starfield

import android.view.GestureDetector
import android.view.MotionEvent
import drwdrd.ktdev.engine.GLWallpaperService
import drwdrd.ktdev.engine.Log

class StarfieldWallpaperService : GLWallpaperService() {

    init {
        Log.tag = "StarfieldWallpaper"
        touchEventsEnabled = true
    }

    private lateinit var gestureDetector : GestureDetector

    override fun onTouchEvent(event: MotionEvent?) {
        super.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)
    }

    override fun createRenderer() : StarfieldRenderer {
        var renderer = StarfieldRenderer(this)
        var gestureListener = renderer.createGestureListener()
        gestureDetector = GestureDetector(this, gestureListener)
        wallpaperLiveCycleListener = renderer
        return renderer
    }

}
