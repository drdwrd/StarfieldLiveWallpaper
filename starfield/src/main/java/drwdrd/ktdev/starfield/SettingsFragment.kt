package drwdrd.ktdev.starfield

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import drwdrd.ktdev.kengine.logd

class SettingsFragment(context : Context) : Fragment() {

    interface FragmentHandler {
        fun onViewCreated(view: View, fragmentManager : FragmentManager)
        fun onResetSettings()
    }

    class SinglePaneFragmentHandler : FragmentHandler {

        private lateinit var particlesSettingsFragment: ParticlesSettingsFragment
        private lateinit var cameraSettingsFragment: CameraSettingsFragment
        private lateinit var systemSettingsFragment: SystemSettingsFragment

        override fun onViewCreated(view: View, fragmentManager : FragmentManager) {
            particlesSettingsFragment = ParticlesSettingsFragment()
            cameraSettingsFragment = CameraSettingsFragment()
            systemSettingsFragment = SystemSettingsFragment()
            fragmentManager.beginTransaction().add(R.id.settingsContentFrame, particlesSettingsFragment).commit()
            fragmentManager.beginTransaction().add(R.id.settingsContentFrame, cameraSettingsFragment).commit()
            fragmentManager.beginTransaction().add(R.id.settingsContentFrame, systemSettingsFragment).commit()
        }

        override fun onResetSettings() {
            particlesSettingsFragment.resetSettings()
            cameraSettingsFragment.resetSettings()
            systemSettingsFragment.resetSettings()
        }
    }

    class MultiPaneFragmentHandler : FragmentHandler {

        private lateinit var settingsListFragment: SettingsListFragment
        private lateinit var particlesSettingsFragment: ParticlesSettingsFragment
        private lateinit var cameraSettingsFragment: CameraSettingsFragment
        private lateinit var systemSettingsFragment: SystemSettingsFragment

        override fun onViewCreated(view: View, fragmentManager: FragmentManager) {

            settingsListFragment = SettingsListFragment()
            fragmentManager.beginTransaction().add(R.id.tabFrame, settingsListFragment).commit()

            particlesSettingsFragment = ParticlesSettingsFragment()
            fragmentManager.beginTransaction().add(R.id.settingsContentFrame, particlesSettingsFragment).commit()

            cameraSettingsFragment = CameraSettingsFragment()
            fragmentManager.beginTransaction().add(R.id.settingsContentFrame, cameraSettingsFragment).hide(cameraSettingsFragment).commit()

            systemSettingsFragment = SystemSettingsFragment()
            fragmentManager.beginTransaction().add(R.id.settingsContentFrame, systemSettingsFragment).hide(systemSettingsFragment).commit()


            settingsListFragment.onSettingSelectedListener = object : SettingsListFragment.OnSettingSelectedListener {
                override fun onSettingSelected(pos: Int) {
                    when(pos) {
                        0 -> {
                            fragmentManager.beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).hide(cameraSettingsFragment).commit()
                            fragmentManager.beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).hide(systemSettingsFragment).commit()
                            fragmentManager.beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).show(particlesSettingsFragment).commit()
                        }
                        1 -> {
                            fragmentManager.beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).hide(particlesSettingsFragment).commit()
                            fragmentManager.beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).hide(systemSettingsFragment).commit()
                            fragmentManager.beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).show(cameraSettingsFragment).commit()
                        }
                        2 -> {
                            fragmentManager.beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).hide(particlesSettingsFragment).commit()
                            fragmentManager.beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).hide(cameraSettingsFragment).commit()
                            fragmentManager.beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).show(systemSettingsFragment).commit()
                        }
                    }
                }
            }
        }

        override fun onResetSettings() {
            particlesSettingsFragment.resetSettings()
            cameraSettingsFragment.resetSettings()
            systemSettingsFragment.resetSettings()
        }
    }

    private lateinit var fragmentHandler : FragmentHandler
    private val adProvider = AdProvider(context)


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.settings_fragment, container, false)
        logd("layout tag: ${view.tag}")

        adProvider.requestConsent()
        adProvider.requestSettingsBannerAd(view)


        fragmentHandler = when(view.tag) {
            "layout_sw600dp_land" -> MultiPaneFragmentHandler()
            "layout_sw720dp_land" -> MultiPaneFragmentHandler()
            else -> SinglePaneFragmentHandler()
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fragmentHandler.onViewCreated(view, childFragmentManager)
    }

    fun resetSettings() {
        SettingsProvider.resetSettings()
        fragmentHandler.onResetSettings()
    }

}
