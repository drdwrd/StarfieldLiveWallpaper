package drwdrd.ktdev.starfield

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import drwdrd.ktdev.engine.Log

class SettingsFragment : Fragment() {

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
            val transaction = fragmentManager.beginTransaction()
            transaction.add(R.id.settingsContentFrame, particlesSettingsFragment)
            transaction.add(R.id.settingsContentFrame, cameraSettingsFragment)
            transaction.add(R.id.settingsContentFrame, systemSettingsFragment)
            transaction.commit()
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

            val transaction3 = fragmentManager.beginTransaction()
            transaction3.add(R.id.tabFrame, settingsListFragment)
            transaction3.commit()

            particlesSettingsFragment = ParticlesSettingsFragment()
            cameraSettingsFragment = CameraSettingsFragment()
            systemSettingsFragment = SystemSettingsFragment()

            val transaction2 = fragmentManager.beginTransaction()
            transaction2.add(R.id.settingsContentFrame, particlesSettingsFragment)
            transaction2.add(R.id.settingsContentFrame, cameraSettingsFragment)
            transaction2.hide(cameraSettingsFragment)
            transaction2.add(R.id.settingsContentFrame, systemSettingsFragment)
            transaction2.hide(systemSettingsFragment)
            transaction2.commit()


            settingsListFragment.onSettingSelectedListener = object : SettingsListFragment.OnSettingSelectedListener {
                override fun onSettingSelected(pos: Int) {
                    when(pos) {
                        0 -> {
                            val transaction = fragmentManager.beginTransaction()
                            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                            transaction.show(particlesSettingsFragment)
                            transaction.hide(cameraSettingsFragment)
                            transaction.hide(systemSettingsFragment)
                            transaction.commit()
                        }
                        1 -> {
                            val transaction = fragmentManager.beginTransaction()
                            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                            transaction.hide(particlesSettingsFragment)
                            transaction.show(cameraSettingsFragment)
                            transaction.hide(systemSettingsFragment)
                            transaction.commit()
                        }
                        2 -> {
                            val transaction = fragmentManager.beginTransaction()
                            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                            transaction.hide(particlesSettingsFragment)
                            transaction.hide(cameraSettingsFragment)
                            transaction.show(systemSettingsFragment)
                            transaction.commit()
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


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.settings_fragment, container, false)
        Log.debug("SettingsFragment", "layout tag: ${view.tag}")
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
