package drwdrd.ktdev.starfield

import android.content.Intent
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import com.google.firebase.auth.FirebaseAuth
import drwdrd.ktdev.engine.Log

private const val TAG = "drwdrd.ktdev.starfield.StarfieldActivity"


class StarfieldActivity : AppCompatActivity(), MenuFragment.OnMenuFragmentInteractionListener {

    private lateinit var glSurfaceView : GLSurfaceView
    private lateinit var renderer: StarfieldRenderer
    private lateinit var mainMenuFragment : MenuFragment
    private lateinit var themeMenuFragment: ThemeMenuFragment


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(null)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.starfield_activity)

        val firebaseAuth = FirebaseAuth.getInstance()
        if(firebaseAuth.currentUser == null) {
            firebaseAuth.signInAnonymously().addOnCompleteListener {
                if(!it.isSuccessful) {
                    Log.error(TAG, "User authentication failed!")
                } else {
                    Log.info(TAG, "User authenticated!")
                }
            }
        } else {
            Log.info(TAG, "User already logged!")
        }


        glSurfaceView = findViewById(R.id.glSurfaceView)
        glSurfaceView.setEGLContextClientVersion(2)
        glSurfaceView.preserveEGLContextOnPause = true
        renderer = StarfieldRenderer.createRenderer(this)
        glSurfaceView.setRenderer(renderer)
        glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY

        mainMenuFragment = MainMenuFragment()
        themeMenuFragment = ThemeMenuFragment()

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
                    fragmentTransaction.add(R.id.menuFrame, themeMenuFragment)
                    fragmentTransaction.commit()
                }
            }
            "browser" -> when(item) {
                "back" -> {
                    val fragmentTransaction = supportFragmentManager.beginTransaction()
                    fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    fragmentTransaction.remove(themeMenuFragment)
                    fragmentTransaction.add(R.id.menuFrame, mainMenuFragment)
                    fragmentTransaction.commit()
                }
            }
        }
    }
}