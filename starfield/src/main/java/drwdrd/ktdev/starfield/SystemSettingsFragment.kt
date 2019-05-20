package drwdrd.ktdev.starfield

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import androidx.fragment.app.Fragment

class SystemSettingsFragment : Fragment() {

    private lateinit var scrollingEffectEnableSwitch : Switch
    private lateinit var highQualityTexturesSwitch : Switch
    private lateinit var overrideSystemFrameRateSwith : Switch
    private lateinit var targetFrameRateSlider: Slider


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.system_settings_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        scrollingEffectEnableSwitch = view.findViewById(R.id.scrollingEffectSwitch)
        scrollingEffectEnableSwitch.isChecked = SettingsProvider.enableScrollingEffect
        scrollingEffectEnableSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            SettingsProvider.enableScrollingEffect = isChecked
        }

        highQualityTexturesSwitch = view.findViewById(R.id.highQualityTexturesSwitch)
        highQualityTexturesSwitch.isChecked = (SettingsProvider.textureQualityLevel == 0)
        highQualityTexturesSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            StarfieldRenderer.rendererInstances.requestRestart()
            SettingsProvider.textureQualityLevel = if(isChecked) 0 else 1
        }

        targetFrameRateSlider = view.findViewById(R.id.targetFrameRateSlider)
        targetFrameRateSlider.isEnabled = SettingsProvider.overrideSystemFrameRate
        targetFrameRateSlider.value = SettingsProvider.targetFrameRate
        targetFrameRateSlider.onValueChangedListener = object : Slider.OnValueChangedListener {
            override fun onValueChanged(value: Float) {
                SettingsProvider.targetFrameRate = value
            }
        }

        overrideSystemFrameRateSwith = view.findViewById(R.id.overrideTargetFrameRateSwitch)
        overrideSystemFrameRateSwith.isChecked = SettingsProvider.overrideSystemFrameRate
        overrideSystemFrameRateSwith.setOnCheckedChangeListener { buttonView, isChecked ->
            targetFrameRateSlider.isEnabled = isChecked
            SettingsProvider.overrideSystemFrameRate = isChecked
        }

    }

    fun resetSettings() {
        scrollingEffectEnableSwitch.isChecked = SettingsProvider.enableScrollingEffect
        highQualityTexturesSwitch.isChecked = (SettingsProvider.textureQualityLevel == 0)
        overrideSystemFrameRateSwith.isChecked = SettingsProvider.overrideSystemFrameRate
        targetFrameRateSlider.value = SettingsProvider.targetFrameRate
    }

}