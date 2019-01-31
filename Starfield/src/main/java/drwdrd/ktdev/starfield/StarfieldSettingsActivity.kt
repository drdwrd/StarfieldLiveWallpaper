package drwdrd.ktdev.starfield

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.CheckBox
import android.widget.SeekBar
import android.widget.Toast

class StarfieldSettingsActivity : AppCompatActivity() {

    private lateinit var timeScaleSlider : SeekBar
    private lateinit var starsSpawnTimeSlider : SeekBar
    private lateinit var cloudsSpawnTimeSlider : SeekBar
    private lateinit var parallaxEffectMultiplierSlider : SeekBar
    private lateinit var parallaxEffectEnabledCheckBox : CheckBox
    private  lateinit var highQualityTexturesCheckBox : CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupActionBar()
        setContentView(R.layout.settings_activity)

        timeScaleSlider = findViewById(R.id.timeScaleSlider)
        timeScaleSlider.progress = (SettingsProvider.timeScale * 10.0).toInt()
        timeScaleSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                SettingsProvider.timeScale = progress.toDouble() / 10.0
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })

        starsSpawnTimeSlider = findViewById(R.id.starsSpawnTimeSlider)
        starsSpawnTimeSlider.progress = (SettingsProvider.starParticlesSpawnTime * 1000.0).toInt()
        starsSpawnTimeSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                SettingsProvider.starParticlesSpawnTime = progress.toDouble() / 1000.0
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })

        cloudsSpawnTimeSlider = findViewById(R.id.cloudsSpawnTimeSlider)
        cloudsSpawnTimeSlider.progress = (SettingsProvider.cloudParticleSpawnTime * 100.0).toInt()
        cloudsSpawnTimeSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                SettingsProvider.cloudParticleSpawnTime = progress.toDouble() / 100.0
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

        parallaxEffectMultiplierSlider.progress = (SettingsProvider.parallaxEffectMultiplier * 10.0f).toInt()
        parallaxEffectMultiplierSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                SettingsProvider.parallaxEffectMultiplier = progress.toFloat() / 10.0f
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
            timeScaleSlider.progress = (SettingsProvider.timeScale * 10.0).toInt()
            starsSpawnTimeSlider.progress = (SettingsProvider.starParticlesSpawnTime * 1000.0).toInt()
            cloudsSpawnTimeSlider.progress = (SettingsProvider.cloudParticleSpawnTime * 100.0).toInt()
            parallaxEffectEnabledCheckBox.isChecked = SettingsProvider.enableParallaxEffect
            parallaxEffectMultiplierSlider.isEnabled = SettingsProvider.enableParallaxEffect
            parallaxEffectMultiplierSlider.progress = (SettingsProvider.parallaxEffectMultiplier * 10.0f).toInt()
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
