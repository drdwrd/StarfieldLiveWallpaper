package drwdrd.ktdev.starfield_free

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.CheckBox
import android.widget.SeekBar
import android.widget.Toast
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds

class StarfieldSettingsActivity : AppCompatActivity() {

    private lateinit var particleSpeedSlider : SeekBar
    private lateinit var starsSpawnTimeSlider : SeekBar
    private lateinit var parallaxEffectMultiplierSlider : SeekBar
    private lateinit var adaptiveFPS : CheckBox
    private lateinit var parallaxEffectEnabledCheckBox : CheckBox
    private lateinit var scrollingEffectEnableCheckBox : CheckBox
    private  lateinit var highQualityTexturesCheckBox : CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupActionBar()
        setContentView(R.layout.settings_activity)

        MobileAds.initialize(this, getString(R.string.admob_id))

        val adView = findViewById<AdView>(R.id.adViewBanner2)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        val getFullVersionButton = findViewById<Button>(R.id.getFullVersionButton2)
        getFullVersionButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://play.google.com/store/apps/details?id=drwdrd.ktdev.starfield")
                setPackage("com.android.vending")
            }
            startActivity(intent)
        }


        particleSpeedSlider = findViewById(R.id.particleSpeedSlider)
        particleSpeedSlider.progress = (SettingsProvider.particleSpeed * 10.0f).toInt()
        particleSpeedSlider.isEnabled = false

        starsSpawnTimeSlider = findViewById(R.id.starsSpawnTimeSlider)
        starsSpawnTimeSlider.progress = 26 - (SettingsProvider.particlesSpawnTimeMultiplier * 100.0).toInt()
        starsSpawnTimeSlider.isEnabled = false

        adaptiveFPS = findViewById(R.id.adaptiveFPSCheckbox)
        adaptiveFPS.isChecked = SettingsProvider.adaptiveFPS
        adaptiveFPS.isEnabled = false


        parallaxEffectMultiplierSlider = findViewById(R.id.parallaxEffectMultiplierSlider)
        parallaxEffectMultiplierSlider.isEnabled = false

        parallaxEffectEnabledCheckBox = findViewById(R.id.parallaxEffectEnabledCheckBox)
        parallaxEffectEnabledCheckBox.isEnabled = false
        parallaxEffectEnabledCheckBox.isChecked = false

        scrollingEffectEnableCheckBox = findViewById(R.id.scrollingEffectCheckBox)
        scrollingEffectEnableCheckBox.isChecked = false
        scrollingEffectEnableCheckBox.isEnabled = false

        highQualityTexturesCheckBox = findViewById(R.id.highQualityTexturesCheckBox)
        highQualityTexturesCheckBox.isChecked = (SettingsProvider.textureQualityLevel == 0)
        highQualityTexturesCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            SettingsProvider.textureQualityLevel = if(isChecked) 0 else 1
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
            SettingsProvider.resetSettings()
            highQualityTexturesCheckBox.isChecked = (SettingsProvider.textureQualityLevel == 0)
            Toast.makeText(this, "Resetting settings...", Toast.LENGTH_LONG).show()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStop() {
        SettingsProvider.save(applicationContext,"starfield.ini")
        super.onStop()
    }

}
