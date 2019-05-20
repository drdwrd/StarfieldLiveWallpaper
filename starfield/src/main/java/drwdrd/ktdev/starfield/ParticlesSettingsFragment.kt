package drwdrd.ktdev.starfield

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import androidx.fragment.app.Fragment

class ParticlesSettingsFragment : Fragment() {

    private lateinit var particleSpeedSlider : Slider
    private lateinit var starsSpawnTimeSlider : Slider
    private lateinit var cloudsSpawnTimeSlider : Slider
    private lateinit var adaptiveFPSSwitch : Switch


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.particles_settings_fragment, container, false)
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
        starsSpawnTimeSlider.value = starsSpawnTimeSlider.maxValue + starsSpawnTimeSlider.minValue - SettingsProvider.starsSpawnTimeMultiplier.toFloat()
        starsSpawnTimeSlider.onValueChangedListener = object : Slider.OnValueChangedListener {
            override fun onValueChanged(value: Float) {
                SettingsProvider.starsSpawnTimeMultiplier = starsSpawnTimeSlider.maxValue + starsSpawnTimeSlider.minValue - value.toDouble()
            }
        }

        cloudsSpawnTimeSlider = view.findViewById(R.id.cloudsSpawnTimeSlider)
        cloudsSpawnTimeSlider.isEnabled = !SettingsProvider.adaptiveFPS
        cloudsSpawnTimeSlider.value = cloudsSpawnTimeSlider.maxValue + cloudsSpawnTimeSlider.minValue - SettingsProvider.cloudsSpawnTimeMultiplier.toFloat()
        cloudsSpawnTimeSlider.onValueChangedListener = object : Slider.OnValueChangedListener {
            override fun onValueChanged(value: Float) {
                SettingsProvider.cloudsSpawnTimeMultiplier = cloudsSpawnTimeSlider.maxValue + cloudsSpawnTimeSlider.minValue - value.toDouble()
            }
        }

        adaptiveFPSSwitch = view.findViewById(R.id.adaptiveFPSSwitch)
        adaptiveFPSSwitch.isChecked = SettingsProvider.adaptiveFPS
        adaptiveFPSSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            starsSpawnTimeSlider.isEnabled = !isChecked
            cloudsSpawnTimeSlider.isEnabled = !isChecked
            SettingsProvider.adaptiveFPS = isChecked
        }


    }

    fun resetSettings() {
        particleSpeedSlider.value = SettingsProvider.particleSpeed
        adaptiveFPSSwitch.isChecked = SettingsProvider.adaptiveFPS
        starsSpawnTimeSlider.value = starsSpawnTimeSlider.maxValue + starsSpawnTimeSlider.minValue - SettingsProvider.starsSpawnTimeMultiplier.toFloat()
        cloudsSpawnTimeSlider.value = cloudsSpawnTimeSlider.maxValue + cloudsSpawnTimeSlider.minValue - SettingsProvider.cloudsSpawnTimeMultiplier.toFloat()
    }

}