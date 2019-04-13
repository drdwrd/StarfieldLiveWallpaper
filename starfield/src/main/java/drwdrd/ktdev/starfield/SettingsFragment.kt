package drwdrd.ktdev.starfield

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.SeekBar
import androidx.fragment.app.Fragment

class SettingsFragment : Fragment() {

    private lateinit var particleSpeedSlider : SeekBar
    private lateinit var starsSpawnTimeSlider : SeekBar
    private lateinit var parallaxEffectMultiplierSlider : SeekBar
    private lateinit var adaptiveFPS : CheckBox
    private lateinit var parallaxEffectEnabledCheckBox : CheckBox
    private lateinit var scrollingEffectEnableCheckBox : CheckBox
    private lateinit var highQualityTexturesCheckBox : CheckBox


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.settings_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        particleSpeedSlider = view.findViewById(R.id.particleSpeedSlider)
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

        starsSpawnTimeSlider = view.findViewById(R.id.starsSpawnTimeSlider)
        starsSpawnTimeSlider.isEnabled = !SettingsProvider.adaptiveFPS

        adaptiveFPS = view.findViewById(R.id.adaptiveFPSCheckbox)
        adaptiveFPS.isChecked = SettingsProvider.adaptiveFPS
        adaptiveFPS.setOnCheckedChangeListener { buttonView, isChecked ->
            starsSpawnTimeSlider.isEnabled = !isChecked
            SettingsProvider.adaptiveFPS = isChecked
        }

        starsSpawnTimeSlider.progress = 26 - (SettingsProvider.particlesSpawnTimeMultiplier * 100.0).toInt()
        starsSpawnTimeSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                SettingsProvider.particlesSpawnTimeMultiplier = (26.0 - progress.toDouble()) / 100.0
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })

        parallaxEffectMultiplierSlider = view.findViewById(R.id.parallaxEffectMultiplierSlider)
        parallaxEffectMultiplierSlider.isEnabled = SettingsProvider.enableParallaxEffect

        parallaxEffectEnabledCheckBox = view.findViewById(R.id.parallaxEffectEnabledCheckBox)
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

        scrollingEffectEnableCheckBox = view.findViewById(R.id.scrollingEffectCheckBox)
        scrollingEffectEnableCheckBox.isChecked = SettingsProvider.enableScrollingEffect
        scrollingEffectEnableCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            SettingsProvider.enableScrollingEffect = isChecked
        }

        highQualityTexturesCheckBox = view.findViewById(R.id.highQualityTexturesCheckBox)
        highQualityTexturesCheckBox.isChecked = (SettingsProvider.textureQualityLevel == 0)
        highQualityTexturesCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            StarfieldRenderer.notifyRestart()
            SettingsProvider.textureQualityLevel = if(isChecked) 0 else 1
        }
    }

    fun resetSettings() {
        SettingsProvider.resetSettings()
        particleSpeedSlider.progress = (SettingsProvider.particleSpeed * 10.0f).toInt()
        adaptiveFPS.isChecked = SettingsProvider.adaptiveFPS
        starsSpawnTimeSlider.progress = 26 - (SettingsProvider.particlesSpawnTimeMultiplier * 100.0).toInt()
        parallaxEffectEnabledCheckBox.isChecked = SettingsProvider.enableParallaxEffect
        parallaxEffectEnabledCheckBox.isEnabled = (SettingsProvider.parallaxEffectEngineType != SettingsProvider.ParallaxEffectEngineType.None)
        parallaxEffectMultiplierSlider.isEnabled = SettingsProvider.enableParallaxEffect
        parallaxEffectMultiplierSlider.progress = (SettingsProvider.parallaxEffectMultiplier * 50.0f).toInt()
        scrollingEffectEnableCheckBox.isChecked = SettingsProvider.enableScrollingEffect
        highQualityTexturesCheckBox.isChecked = (SettingsProvider.textureQualityLevel == 0)
    }

}