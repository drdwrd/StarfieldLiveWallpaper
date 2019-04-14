package drwdrd.ktdev.starfield

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import drwdrd.ktdev.engine.Log


private const val TAG = "drwdrd.ktdev.starfield.StarfieldSettingsActivity"

class StarfieldSettingsActivity : AppCompatActivity() {

    private val settingsFragment = SettingsFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupActionBar()
        if(savedInstanceState == null) {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.add(android.R.id.content, settingsFragment)
            transaction.commit()
        }

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
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.settings_menu, menu)
        return true
    }

    private fun setupActionBar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if(item?.itemId == android.R.id.home) {
            finish()
        }
        if(item?.itemId == R.id.menuResetSettings) {
            settingsFragment.resetSettings()
            Toast.makeText(this, "Resetting settings...", Toast.LENGTH_LONG).show()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        SettingsProvider.save(applicationContext,"starfield.ini")
        super.onStop()
    }
}
