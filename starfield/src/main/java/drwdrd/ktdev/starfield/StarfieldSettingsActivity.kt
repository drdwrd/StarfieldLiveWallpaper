package drwdrd.ktdev.starfield

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class StarfieldSettingsActivity : AppCompatActivity() {

    private lateinit var settingsFragment : SettingsFragment
    private val privacyPolicyDialogHandler = PrivacyPolicyDialogHandler(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(null)        //ignore saved
        setupActionBar()
        settingsFragment = SettingsFragment()

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(android.R.id.content, settingsFragment)
        transaction.commit()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.settings_menu, menu)
        return true
    }

    private fun setupActionBar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            android.R.id.home -> finish()
            R.id.menuPrivacySettings -> privacyPolicyDialogHandler.onShowPrivacyDialog()
            R.id.menuResetSettings -> {
                settingsFragment.resetSettings()
                Toast.makeText(this, R.string.msg_reset_settings, Toast.LENGTH_LONG).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStop() {
        SettingsProvider.save(applicationContext, BuildConfig.configFileName)
        super.onStop()
    }
}
