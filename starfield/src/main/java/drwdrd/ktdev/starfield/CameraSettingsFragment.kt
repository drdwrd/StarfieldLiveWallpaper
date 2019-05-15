package drwdrd.ktdev.starfield

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.fragment.app.Fragment

class CameraSettingsFragment : Fragment() {

    private lateinit var parallaxEffectMultiplierSlider : Slider
    private lateinit var parallaxEffectAccelerationSlider : Slider
    private lateinit var cameraRotationSpeedSlider : Slider
    private lateinit var parallaxEffectEnabledCheckBox : CheckBox


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.camera_settings_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        cameraRotationSpeedSlider = view.findViewById(R.id.cameraRotationSpeedSlider)
        cameraRotationSpeedSlider.value = -SettingsProvider.cameraRotationSpeed
        cameraRotationSpeedSlider.onValueChangedListener = object : Slider.OnValueChangedListener {
            override fun onValueChanged(value: Float) {
                SettingsProvider.cameraRotationSpeed = -value
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
    }

    fun resetSettings() {
        cameraRotationSpeedSlider.value = -SettingsProvider.cameraRotationSpeed
        parallaxEffectEnabledCheckBox.isChecked = SettingsProvider.enableParallaxEffect
        parallaxEffectEnabledCheckBox.isEnabled = (SettingsProvider.parallaxEffectEngineType != SettingsProvider.ParallaxEffectEngineType.None)
        parallaxEffectMultiplierSlider.isEnabled = SettingsProvider.enableParallaxEffect
        parallaxEffectMultiplierSlider.value = SettingsProvider.parallaxEffectMultiplier
    }

}