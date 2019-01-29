package drwdrd.ktdev.starfield

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.widget.CheckBox
import android.widget.SeekBar

class StarfieldSettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupActionBar()
        setContentView(R.layout.settings_activity)

        val timeScaleSlider = findViewById<SeekBar>(R.id.timeScaleSlider)
        timeScaleSlider.progress = (Settings.timeScale * 10.0).toInt()
        timeScaleSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                Settings.timeScale = progress.toDouble() / 10.0
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })

        val starsSpawnTimeSlider = findViewById<SeekBar>(R.id.starsSpawnTimeSlider)
        starsSpawnTimeSlider.progress = (Settings.starParticlesSpawnTime * 1000.0).toInt()
        starsSpawnTimeSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                Settings.starParticlesSpawnTime = progress.toDouble() / 1000.0
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })

        val cloudsSpawnTimeSlider = findViewById<SeekBar>(R.id.cloudsSpawnTimeSlider)
        cloudsSpawnTimeSlider.progress = (Settings.cloudParticleSpawnTime * 100.0).toInt()
        cloudsSpawnTimeSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                Settings.cloudParticleSpawnTime = progress.toDouble() / 100.0
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })

        val parallaxEffectMultiplierSlider = findViewById<SeekBar>(R.id.parallaxEffectMultiplierSlider)
        parallaxEffectMultiplierSlider.isEnabled = Settings.enableParallaxEffect

        val parallaxEffectEnabledCheckBox = findViewById<CheckBox>(R.id.parallaxEffectEnabledCheckBox)
        parallaxEffectEnabledCheckBox.isChecked = Settings.enableParallaxEffect
        parallaxEffectEnabledCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            parallaxEffectMultiplierSlider.isEnabled = isChecked
            Settings.enableParallaxEffect = isChecked
        }

        parallaxEffectMultiplierSlider.progress = (Settings.parallaxEffectMultiplier * 10.0f).toInt()
        parallaxEffectMultiplierSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                Settings.parallaxEffectMultiplier = progress.toFloat() / 10.0f
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })

        val highQualityTexturesCheckBox = findViewById<CheckBox>(R.id.highQualityTexturesCheckBox)
        highQualityTexturesCheckBox.isChecked = (Settings.textureQualityLevel == 0)
        highQualityTexturesCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            Settings.textureQualityLevel = if(isChecked) 0 else 1
        }
    }

    private fun setupActionBar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if(item?.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStop() {
        Settings.save(applicationContext,"starfield.ini")
        super.onStop()
    }

}
