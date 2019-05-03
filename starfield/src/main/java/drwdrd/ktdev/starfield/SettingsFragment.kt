package drwdrd.ktdev.starfield

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.SeekBar
import androidx.fragment.app.Fragment

class SettingsFragment : Fragment() {

    private lateinit var particleSpeedSlider : Slider
    private lateinit var starsSpawnTimeSlider : Slider
    private lateinit var cloudsSpawnTimeSlider : Slider
    private lateinit var parallaxEffectMultiplierSlider : Slider
    private lateinit var parallaxEffectAccelerationSlider : Slider
    private lateinit var cameraRotationSpeedSlider : Slider
    private lateinit var adaptiveFPS : CheckBox
    private lateinit var parallaxEffectEnabledCheckBox : CheckBox
    private lateinit var scrollingEffectEnableCheckBox : CheckBox
    private lateinit var highQualityTexturesCheckBox : CheckBox


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.settings_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        particleSpeedSlider = view.findViewById(R.id.particleSpeedSlider)
        particleSpeedSlider.value = SettingsProvider.particleSpeed
        particleSpeedSlider.onValueChangedListener = object : Slider.OnValueChangedListener {
            override fun onValueChanged(value: Float) {
                SettingsProvider.particleSpeed = value
            }
        }

        starsSpawnTimeSlider = view.findViewById(R.id.starsSpawnTimeSlider)
        starsSpawnTimeSlider.isEnabled = !SettingsProvider.adaptiveFPS

        cloudsSpawnTimeSlider = view.findViewById(R.id.cloudsSpawnTimeSlider)
        cloudsSpawnTimeSlider.isEnabled = !SettingsProvider.adaptiveFPS

        adaptiveFPS = view.findViewById(R.id.adaptiveFPSCheckbox)
        adaptiveFPS.isChecked = SettingsProvider.adaptiveFPS
        adaptiveFPS.setOnCheckedChangeListener { buttonView, isChecked ->
            starsSpawnTimeSlider.isEnabled = !isChecked
            cloudsSpawnTimeSlider.isEnabled = !isChecked
            SettingsProvider.adaptiveFPS = isChecked
        }

        starsSpawnTimeSlider.value = starsSpawnTimeSlider.maxValue + starsSpawnTimeSlider.minValue - SettingsProvider.starsSpawnTimeMultiplier.toFloat()
        starsSpawnTimeSlider.onValueChangedListener = object : Slider.OnValueChangedListener {
            override fun onValueChanged(value: Float) {
                SettingsProvider.starsSpawnTimeMultiplier = starsSpawnTimeSlider.maxValue + starsSpawnTimeSlider.minValue - value.toDouble()
            }
        }

        cloudsSpawnTimeSlider.value = cloudsSpawnTimeSlider.maxValue + cloudsSpawnTimeSlider.minValue - SettingsProvider.cloudsSpawnTimeMultiplier.toFloat()
        cloudsSpawnTimeSlider.onValueChangedListener = object : Slider.OnValueChangedListener {
            override fun onValueChanged(value: Float) {
                SettingsProvider.cloudsSpawnTimeMultiplier = cloudsSpawnTimeSlider.maxValue + cloudsSpawnTimeSlider.minValue - value.toDouble()
            }
        }

        cameraRotationSpeedSlider = view.findViewById(R.id.cameraRotationSpeedSlider)
        cameraRotationSpeedSlider.value = SettingsProvider.cameraRotationSpeed
        cameraRotationSpeedSlider.onValueChangedListener = object : Slider.OnValueChangedListener {
            override fun onValueChanged(value: Float) {
                SettingsProvider.cameraRotationSpeed = value
            }
        }

        parallaxEffectMultiplierSlider = view.findViewById(R.id.parallaxEffectMultiplierSlider)
        parallaxEffectMultiplierSlider.isEnabled = SettingsProvider.enableParallaxEffect

        parallaxEffectAccelerationSlider = view.findViewById(R.id.parallaxEffectAccelerationSlider)
        parallaxEffectAccelerationSlider.isEnabled = SettingsProvider.enableParallaxEffect

        parallaxEffectEnabledCheckBox = view.findViewById(R.id.parallaxEffectEnabledCheckBox)
        parallaxEffectEnabledCheckBox.isEnabled = (SettingsProvider.parallaxEffectEngineType != SettingsProvider.ParallaxEffectEngineType.None)
        parallaxEffectEnabledCheckBox.isChecked = SettingsProvider.enableParallaxEffect
        parallaxEffectEnabledCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            parallaxEffectMultiplierSlider.isEnabled = isChecked
            parallaxEffectAccelerationSlider.isEnabled = isChecked
            SettingsProvider.enableParallaxEffect = isChecked
        }

        parallaxEffectMultiplierSlider.value = SettingsProvider.parallaxEffectMultiplier
        parallaxEffectMultiplierSlider.onValueChangedListener = object : Slider.OnValueChangedListener {
            override fun onValueChanged(value: Float) {
                SettingsProvider.parallaxEffectMultiplier = value
            }
        }

        parallaxEffectAccelerationSlider.value = SettingsProvider.parallaxEffectAcceleration
        parallaxEffectAccelerationSlider.onValueChangedListener = object : Slider.OnValueChangedListener {
            override fun onValueChanged(value: Float) {
                SettingsProvider.parallaxEffectAcceleration = value
            }
        }

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
        particleSpeedSlider.value = SettingsProvider.particleSpeed
        adaptiveFPS.isChecked = SettingsProvider.adaptiveFPS
        starsSpawnTimeSlider.value = starsSpawnTimeSlider.maxValue + starsSpawnTimeSlider.minValue - SettingsProvider.starsSpawnTimeMultiplier.toFloat()
        cloudsSpawnTimeSlider.value = cloudsSpawnTimeSlider.maxValue + cloudsSpawnTimeSlider.minValue - SettingsProvider.cloudsSpawnTimeMultiplier.toFloat()
        cameraRotationSpeedSlider.value = SettingsProvider.cameraRotationSpeed
        parallaxEffectEnabledCheckBox.isChecked = SettingsProvider.enableParallaxEffect
        parallaxEffectEnabledCheckBox.isEnabled = (SettingsProvider.parallaxEffectEngineType != SettingsProvider.ParallaxEffectEngineType.None)
        parallaxEffectMultiplierSlider.isEnabled = SettingsProvider.enableParallaxEffect
        parallaxEffectMultiplierSlider.value = SettingsProvider.parallaxEffectMultiplier
        scrollingEffectEnableCheckBox.isChecked = SettingsProvider.enableScrollingEffect
        highQualityTexturesCheckBox.isChecked = (SettingsProvider.textureQualityLevel == 0)
    }

}