package drwdrd.ktdev.starfield

import android.content.Intent
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction


class StarfieldActivity : FragmentActivity(), MenuFragment.OnMenuFragmentInteractionListener {

    private lateinit var glSurfaceView : GLSurfaceView
    private lateinit var renderer: StarfieldRenderer
    private lateinit var mainMenuFragment : MenuFragment
    private lateinit var themesFragment: ThemesFragment


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(null)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.starfield_activity)

        glSurfaceView = findViewById(R.id.glSurfaceView)
        glSurfaceView.setEGLContextClientVersion(2)
        glSurfaceView.preserveEGLContextOnPause = true
        renderer = StarfieldRenderer.createRenderer(this)
        glSurfaceView.setRenderer(renderer)
        glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY

        mainMenuFragment = MainMenuFragment()
        themesFragment = ThemesFragment()

        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        fragmentTransaction.add(R.id.menuFrame, mainMenuFragment)
        fragmentTransaction.commit()
    }

    override fun onResume() {
        glSurfaceView.onResume()
        renderer.onResume()
        super.onResume()
    }

    override fun onPause() {
        glSurfaceView.onPause()
        renderer.onPause()
        super.onPause()
    }

    override fun onStart() {
        renderer.onStart()
        super.onStart()
    }

    override fun onStop() {
        renderer.onStop()
        super.onStop()
    }

    override fun onMenuFragmentInteraction(menu: String, item: String) {
        when(menu) {
            "main" -> when(item) {
                "settings" -> {
                    val intent = Intent(this, StarfieldSettingsActivity::class.java)
                    startActivity(intent)
                }
                "browse" -> {
                    val fragmentTransaction = supportFragmentManager.beginTransaction()
                    fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    fragmentTransaction.remove(mainMenuFragment)
                    fragmentTransaction.add(R.id.menuFrame, themesFragment)
                    fragmentTransaction.commit()
                }
            }
            "browser" -> when(item) {
                "back" -> {
                    val fragmentTransaction = supportFragmentManager.beginTransaction()
                    fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    fragmentTransaction.remove(themesFragment)
                    fragmentTransaction.add(R.id.menuFrame, mainMenuFragment)
                    fragmentTransaction.commit()
                }
            }
        }
    }
}