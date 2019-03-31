package drwdrd.ktdev.starfield

import android.content.Intent
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import drwdrd.ktdev.engine.GLWallpaperService

class StarfieldActivity : FragmentActivity(), MenuFragment.OnMenuFragmentInteractionListener {

    private lateinit var glSurfaceView : GLSurfaceView
    private lateinit var liveCycleListener: GLWallpaperService.WallpaperLiveCycleListener
    private lateinit var mainMenuFragment : MenuFragment
    private lateinit var settingsFragment : SettingsFragment


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(null)
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

        mainMenuFragment = MainMenuFragment()
        settingsFragment = SettingsFragment()

        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        fragmentTransaction.add(R.id.menuFrame, mainMenuFragment)
        fragmentTransaction.commit()
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

    override fun onMenuFragmentInteraction(menu: String, item: String) {
        when(menu) {
            "main" -> when(item) {
                "settings" -> {
                    val intent = Intent(this, StarfieldSettingsActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }
}