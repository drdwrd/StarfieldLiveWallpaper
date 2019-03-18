package drwdrd.ktdev.starfield_free

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.CheckBox
import android.widget.SeekBar
import android.widget.Toast
import com.google.android.gms.ads.AdView
import android.webkit.WebView



class StarfieldSettingsActivity : AppCompatActivity() {

    private lateinit var particleSpeedSlider : SeekBar
    private lateinit var starsSpawnTimeSlider : SeekBar
    private lateinit var parallaxEffectMultiplierSlider : SeekBar
    private lateinit var adaptiveFPS : CheckBox
    private lateinit var parallaxEffectEnabledCheckBox : CheckBox
    private lateinit var scrollingEffectEnableCheckBox : CheckBox
    private  lateinit var highQualityTexturesCheckBox : CheckBox

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
        setupActionBar()
        setContentView(R.layout.settings_activity)

        consentProvider.initialize(this)

        val adView = findViewById<AdView>(R.id.adViewBanner2)
        adView.loadAd(consentProvider.requestBannerAd(this))

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
        parallaxEffectMultiplierSlider.isEnabled = SettingsProvider.enableParallaxEffect

        parallaxEffectEnabledCheckBox = findViewById(R.id.parallaxEffectEnabledCheckBox)
        parallaxEffectEnabledCheckBox.isEnabled = (SettingsProvider.parallaxEffectEngineType != SettingsProvider.ParallaxEffectEngineType.None)
        parallaxEffectEnabledCheckBox.isChecked = SettingsProvider.enableParallaxEffect
        parallaxEffectEnabledCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            parallaxEffectMultiplierSlider.isEnabled = isChecked
            SettingsProvider.enableParallaxEffect = isChecked
        }

        parallaxEffectMultiplierSlider.progress = (SettingsProvider.parallaxEffectMultiplier * 50.0f).toInt()
        parallaxEffectMultiplierSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                SettingsProvider.parallaxEffectMultiplier = progress.toFloat() / 50.0f
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })

        scrollingEffectEnableCheckBox = findViewById(R.id.scrollingEffectCheckBox)
        scrollingEffectEnableCheckBox.isChecked = SettingsProvider.enableScrollingEffect
        scrollingEffectEnableCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            SettingsProvider.enableScrollingEffect = isChecked
        }

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
        when {
            item?.itemId == android.R.id.home -> finish()
            item?.itemId == R.id.menuResetSettings -> {
                SettingsProvider.resetSettings()
                parallaxEffectEnabledCheckBox.isChecked = SettingsProvider.enableParallaxEffect
                parallaxEffectEnabledCheckBox.isEnabled = (SettingsProvider.parallaxEffectEngineType != SettingsProvider.ParallaxEffectEngineType.None)
                parallaxEffectMultiplierSlider.isEnabled = SettingsProvider.enableParallaxEffect
                parallaxEffectMultiplierSlider.progress = (SettingsProvider.parallaxEffectMultiplier * 50.0f).toInt()
                scrollingEffectEnableCheckBox.isChecked = SettingsProvider.enableScrollingEffect
                highQualityTexturesCheckBox.isChecked = (SettingsProvider.textureQualityLevel == 0)
                Toast.makeText(this, "Resetting settings...", Toast.LENGTH_LONG).show()
            }
            item?.itemId == R.id.menuPrivacySettings -> {
                if(consentProvider.isConsentRequired(this)) {
                    consentProvider.initialize(this, true)
                } else {
                    showPrivacyDialog()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStop() {
        SettingsProvider.save(applicationContext,"starfield.ini")
        super.onStop()
    }

    private fun showPrivacyDialog() {
        // create a WebView with the current stats
        val webView = WebView(this)
        webView.loadUrl("https://drdwrd.github.io/starfield_privacy.html")

        // display the WebView in an AlertDialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Privacy")
            .setView(webView)
            .setNeutralButton("OK", null)
            .show()
    }
}
