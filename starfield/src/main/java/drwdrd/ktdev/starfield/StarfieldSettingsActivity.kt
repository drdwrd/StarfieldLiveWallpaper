package drwdrd.ktdev.starfield

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import drwdrd.ktdev.engine.Log
import java.lang.Exception


private const val TAG = "drwdrd.ktdev.starfield.StarfieldSettingsActivity"

class StarfieldSettingsActivity : AppCompatActivity() {

    private val settingsFragment = SettingsFragment()
    private val themesFragment = ThemesFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupActionBar()
        setContentView(R.layout.settings_activity)

        val viewPager = findViewById<ViewPager>(R.id.settingsViewPager)
        viewPager.adapter = SettingsPagerAdapter(supportFragmentManager)

        val tabLayout = findViewById<TabLayout>(R.id.settingsTabLayout)
        tabLayout.setupWithViewPager(viewPager)

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
        //clear cache
        cacheDir.walkBottomUp().forEach {
            if(it.name.contains("theme")) {
                it.delete()
            }
        }
        SettingsProvider.save(applicationContext,"starfield.ini")
        super.onStop()
    }

    inner class SettingsPagerAdapter(fragmentManager: FragmentManager) : FragmentStatePagerAdapter(fragmentManager) {

        override fun getPageTitle(position: Int): CharSequence? {
            return when(position) {
                0 -> "Settings"
                1 -> "Themes"
                else -> null
            }
        }

        override fun getItem(position: Int): Fragment {
            return when(position) {
                0 -> settingsFragment
                1 -> themesFragment
                else -> throw Exception("Invalid fragment!")
            }
        }

        override fun getCount(): Int {
            return 2
        }
    }

}
