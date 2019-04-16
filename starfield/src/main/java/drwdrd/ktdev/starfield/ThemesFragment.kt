package drwdrd.ktdev.starfield

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.themes_fragment.view.*
import java.io.*
import java.lang.Exception
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream


class ThemesFragment : MenuFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.themes_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val backButton = view.findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            onMenuFragmentInteraction("browser", "back")
        }

        val defaultThemeButton = view.findViewById<DownloadButton>(R.id.defaultThemeButton)
        defaultThemeButton.isDownloaded = true
        defaultThemeButton.setOnClickListener {
            StarfieldRenderer.theme = DefaultTheme()
            defaultThemeButton.isCurrent = true
        }

        setupThemeButton(view, R.id.classicThemeButton, "classic")
        setupThemeButton(view, R.id.classicColorThemeButton, "classic_color")
        setupThemeButton(view, R.id.starfieldThemeButton, "starfield2")

    }

    private fun getPackageName(theme : String) : String {
        return when {
            SettingsProvider.textureCompressionMode.hasFlag(SettingsProvider.TextureCompressionMode.ASTC) -> String.format("themes/$theme/${theme}_astc.zip")
            SettingsProvider.textureCompressionMode.hasFlag(SettingsProvider.TextureCompressionMode.ETC2) -> String.format("themes/$theme/${theme}_etc2.zip")
            SettingsProvider.textureCompressionMode.hasFlag(SettingsProvider.TextureCompressionMode.ETC1) -> String.format("themes/$theme/${theme}_etc.zip")
            else -> String.format("themes/$theme/${theme}_png.zip")
        }
    }

    private fun hasTheme(theme : String) : Boolean {
        val location = File(context?.getExternalFilesDir(null), theme)
        if(location.exists() && location.isDirectory) {
            return true
        }
        return false
    }

    private fun setupThemeButton(view : View, buttonId : Int, themeName : String) {
        val themeButton = view.findViewById<DownloadButton>(buttonId)
        themeButton.isDownloaded = hasTheme(themeName)
        themeButton.setOnClickListener {
            if(!hasTheme(themeName)) {
                themeButton.isEnabled = false
                val storage = FirebaseStorage.getInstance("gs://starfield-23195.appspot.com/")
                val fileRef = storage.getReference(getPackageName(themeName))
                val localFile = File.createTempFile("theme", "zip")
                fileRef.getFile(localFile).addOnSuccessListener {
                    Toast.makeText(context, "Installing...", Toast.LENGTH_SHORT).show()
                    installTheme(localFile, themeName)
                    localFile.delete()
                    themeButton.isEnabled = true
                    themeButton.isDownloaded = true
                    themeButton.isCurrent = true
                    StarfieldRenderer.theme = ThemePackage(context!!, themeName)
                    themeButton.progress = 0.0f
                }.addOnFailureListener {
                    Toast.makeText(context, "File download failed!", Toast.LENGTH_SHORT).show()
                    localFile.delete()
                    themeButton.isEnabled = true
                    themeButton.progress = 0.0f
                }.addOnProgressListener {
                    themeButton.progress = 100.0f * it.bytesTransferred / it.totalByteCount
                }
            } else {
                StarfieldRenderer.theme = ThemePackage(context!!, themeName)
                themeButton.isCurrent = true
            }
        }
        themeButton.setOnLongClickListener {
            val location = File(context?.getExternalFilesDir(null), themeName)
            if(location.exists() && location.isDirectory) {
                location.deleteRecursively()
                Toast.makeText(context, "Uninstalling...", Toast.LENGTH_SHORT).show()
                themeButton.isDownloaded = false
            }
            StarfieldRenderer.theme = DefaultTheme()
            true
        }
    }

    private fun installTheme(cacheFile : File, theme : String) {
        val location = File(context?.getExternalFilesDir(null), theme)
        if(!location.exists()) {
            location.mkdir()
        }
        ZipInputStream(FileInputStream(cacheFile)).use { zipInputStream ->
            var zipEntry: ZipEntry? = zipInputStream.nextEntry
            while (zipEntry != null) {
                if (zipEntry.isDirectory) {
                    val dir = File(location, zipEntry.name)
                    dir.mkdir()
                } else {
                    BufferedOutputStream(FileOutputStream(File(location, zipEntry.name))).use { bufferedOutputStream  ->
                        val buffer = ByteArray(1024)
                        var read = zipInputStream.read(buffer)
                        while (read > 0) {
                            bufferedOutputStream.write(buffer, 0, read)
                            read = zipInputStream.read(buffer)
                        }
                        bufferedOutputStream.close()
                        zipInputStream.closeEntry()
                    }
                }
                zipEntry = zipInputStream.nextEntry
            }
        }
    }

}
