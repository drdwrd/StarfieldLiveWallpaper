package drwdrd.ktdev.starfield

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.fragment.app.Fragment

class SystemSettingsFragment : Fragment() {

    private lateinit var scrollingEffectEnableCheckBox : CheckBox
    private lateinit var highQualityTexturesCheckBox : CheckBox


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.system_settings_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        scrollingEffectEnableCheckBox = view.findViewById(R.id.scrollingEffectCheckBox)
        scrollingEffectEnableCheckBox.isChecked = SettingsProvider.enableScrollingEffect
        scrollingEffectEnableCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            SettingsProvider.enableScrollingEffect = isChecked
        }

        highQualityTexturesCheckBox = view.findViewById(R.id.highQualityTexturesCheckBox)
        highQualityTexturesCheckBox.isChecked = (SettingsProvider.textureQualityLevel == 0)
        highQualityTexturesCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            StarfieldRenderer.rendererInstances.notifyRestart()
            SettingsProvider.textureQualityLevel = if(isChecked) 0 else 1
        }
    }

    fun resetSettings() {
        scrollingEffectEnableCheckBox.isChecked = SettingsProvider.enableScrollingEffect
        highQualityTexturesCheckBox.isChecked = (SettingsProvider.textureQualityLevel == 0)
    }

}