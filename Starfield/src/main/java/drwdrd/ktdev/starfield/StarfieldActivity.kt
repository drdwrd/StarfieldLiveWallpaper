package drwdrd.ktdev.starfield

import android.app.Activity
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.provider.Settings
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import drwdrd.ktdev.engine.GLWallpaperService
import drwdrd.ktdev.engine.Log

class StarfieldActivity : Activity() {

    init {
        Log.tag = "StarfieldWallpaperActivity"
    }

    private lateinit var glSurfaceView : GLSurfaceView
    private lateinit var liveCycleListener: GLWallpaperService.WallpaperLiveCycleListener


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.starfield_activity)

        glSurfaceView = findViewById(R.id.glSurfaceView)
        glSurfaceView.setEGLContextClientVersion(2)
        glSurfaceView.preserveEGLContextOnPause = true
        val renderer = StarfieldRenderer.createRenderer(this)
        liveCycleListener = renderer
        glSurfaceView.setRenderer(renderer)
        glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY

        val setWallpaperButton = findViewById<Button>(R.id.setWallpaperButton)
        setWallpaperButton.setOnClickListener {
            val intent = Intent()
            intent.action = WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER
            val packageName = StarfieldWallpaperService::class.java.`package`?.name
            val canonicalName = StarfieldWallpaperService::class.java.canonicalName
            intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, ComponentName(packageName!!, canonicalName!!))
            startActivity(intent)
            finish()
        }

        val setDreamButton = findViewById<Button>(R.id.setDreamButton)
        setDreamButton.setOnClickListener {
            startActivity(Intent(Settings.ACTION_DREAM_SETTINGS))
        }

        val editSettingsButton = findViewById<Button>(R.id.editSettingsButton)
        editSettingsButton.setOnClickListener {
            val intent = Intent(this, StarfieldSettingsActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        glSurfaceView.onResume()
        liveCycleListener.onResume()
        super.onResume()
    }

    override fun onPause() {
        glSurfaceView.onPause()
        liveCycleListener.onPause()
        super.onPause()
    }
}