package drwdrd.ktdev.starfield

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.CheckBox
import android.widget.SeekBar
import android.widget.Toast

//TODO: finish
class StarfieldSettingsActivity : AppCompatActivity() {

    private lateinit var particleSpeedSlider : SeekBar
    private lateinit var starsSpawnTimeSlider : SeekBar
    private lateinit var parallaxEffectMultiplierSlider : SeekBar
    private lateinit var adaptiveFPS : CheckBox
    private lateinit var parallaxEffectEnabledCheckBox : CheckBox
    private  lateinit var highQualityTexturesCheckBox : CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupActionBar()
        setContentView(R.layout.settings_activity)

        particleSpeedSlider = findViewById(R.id.particleSpeedSlider)
        particleSpeedSlider.progress = (SettingsProvider.particleSpeed * 10.0f).toInt()
        particleSpeedSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                SettingsProvider.particleSpeed = progress.toFloat() / 10.0f
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })

        starsSpawnTimeSlider = findViewById(R.id.starsSpawnTimeSlider)
        starsSpawnTimeSlider.isEnabled = !SettingsProvider.adaptiveFPS

        adaptiveFPS = findViewById(R.id.adaptiveFPSCheckbox)
        adaptiveFPS.isChecked = SettingsProvider.adaptiveFPS
        adaptiveFPS.setOnCheckedChangeListener { buttonView, isChecked ->
            starsSpawnTimeSlider.isEnabled = !isChecked
            SettingsProvider.adaptiveFPS = isChecked
        }

        starsSpawnTimeSlider.progress = (SettingsProvider.particlesSpawnTimeMultiplier * 100.0).toInt()
        starsSpawnTimeSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                SettingsProvider.particlesSpawnTimeMultiplier = progress.toDouble() / 100.0
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })

        parallaxEffectMultiplierSlider = findViewById(R.id.parallaxEffectMultiplierSlider)
        parallaxEffectMultiplierSlider.isEnabled = SettingsProvider.enableParallaxEffect

        parallaxEffectEnabledCheckBox = findViewById(R.id.parallaxEffectEnabledCheckBox)
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
            particleSpeedSlider.progress = (SettingsProvider.particleSpeed * 10.0f).toInt()
            adaptiveFPS.isChecked = SettingsProvider.adaptiveFPS
            starsSpawnTimeSlider.progress = (SettingsProvider.particlesSpawnTimeMultiplier * 100.0).toInt()
            parallaxEffectEnabledCheckBox.isChecked = SettingsProvider.enableParallaxEffect
            parallaxEffectMultiplierSlider.isEnabled = SettingsProvider.enableParallaxEffect
            parallaxEffectMultiplierSlider.progress = (SettingsProvider.parallaxEffectMultiplier * 50.0f).toInt()
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
