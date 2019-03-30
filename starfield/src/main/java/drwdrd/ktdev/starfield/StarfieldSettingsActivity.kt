package drwdrd.ktdev.starfield

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
import java.lang.Exception


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

    override fun onStop() {
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
