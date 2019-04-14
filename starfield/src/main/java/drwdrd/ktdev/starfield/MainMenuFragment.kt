package drwdrd.ktdev.starfield

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton

class MainMenuFragment : MenuFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.main_menu, container, false)

        val setWallpaperButton = view.findViewById<ImageButton>(R.id.menuSetWallpaperButton)
        setWallpaperButton.setOnClickListener {
            val intent = Intent()
            intent.action = WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER
            val packageName = StarfieldWallpaperService::class.java.`package`?.name
            val canonicalName = StarfieldWallpaperService::class.java.canonicalName
            intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, ComponentName(packageName!!, canonicalName!!))
            startActivity(intent)
        }

        val setDreamButton = view.findViewById<ImageButton>(R.id.menuSetDreamButton)
        setDreamButton.setOnClickListener {
            startActivity(Intent(Settings.ACTION_DREAM_SETTINGS))
        }

        val browseThemesButton = view.findViewById<ImageButton>(R.id.menuBrowseThemes)
        browseThemesButton.setOnClickListener {
            onMenuFragmentInteraction("main", "browse")
        }

        val showSettingsButton = view.findViewById<ImageButton>(R.id.showSettingsButton)
        showSettingsButton.setOnClickListener {
            onMenuFragmentInteraction("main", "settings")
        }

        return view
    }

}
