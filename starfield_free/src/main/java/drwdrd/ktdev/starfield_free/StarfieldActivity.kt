package drwdrd.ktdev.starfield_free

import android.app.Activity
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import com.google.android.gms.ads.AdView
import drwdrd.ktdev.engine.GLWallpaperService



class StarfieldActivity : Activity() {

    private lateinit var glSurfaceView : GLSurfaceView
    private lateinit var liveCycleListener: GLWallpaperService.WallpaperLiveCycleListener
    private val consentProvider = ConsentProvider(object : ConsentProvider.OnAdFreeVersionRequested {
        override fun onRequest() {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://play.google.com/store/apps/details?id=drwdrd.ktdev.starfield")
                setPackage("com.android.vending")
            }
            finish()
            startActivity(intent)
        }
    })


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.starfield_activity)

        consentProvider.initialize(this)

        val adView = findViewById<AdView>(R.id.adViewBanner)
        adView.loadAd(consentProvider.requestBannerAd(this))

        glSurfaceView = findViewById(R.id.glSurfaceView)
        glSurfaceView.setEGLContextClientVersion(2)
        glSurfaceView.preserveEGLContextOnPause = true
        val renderer = StarfieldRenderer.createRenderer(this)
        liveCycleListener = renderer
        glSurfaceView.setRenderer(renderer)
        glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY

        val getFullVersionButton = findViewById<Button>(R.id.getFullVersionButton)
        getFullVersionButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://play.google.com/store/apps/details?id=drwdrd.ktdev.starfield")
                setPackage("com.android.vending")
            }
            startActivity(intent)
        }

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

    override fun onStart() {
        liveCycleListener.onStart()
        super.onStart()
    }

    override fun onStop() {
        liveCycleListener.onStop()
        super.onStop()
    }
}