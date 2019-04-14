package drwdrd.ktdev.starfield

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import com.google.firebase.storage.FirebaseStorage
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

        val starfieldThemeButton = view.findViewById<DownloadButton>(R.id.starfieldThemeButton)
        starfieldThemeButton.isDownloaded = true
        starfieldThemeButton.setOnClickListener {
            StarfieldRenderer.theme = DefaultTheme()
            starfieldThemeButton.isCurrent = true
        }

        val starfield2ThemeButton = view.findViewById<DownloadButton>(R.id.starfield2ThemeButton)
        starfield2ThemeButton.isDownloaded = hasTheme("starfield2")
        starfield2ThemeButton.setOnClickListener {
            if(!hasTheme("starfield2")) {
                starfield2ThemeButton.isEnabled = false
                val storage = FirebaseStorage.getInstance("gs://starfield-23195.appspot.com/")
                val fileRef = storage.getReference(getPackageName("starfield2"))
                val localFile = File.createTempFile("theme", "zip")
                fileRef.getFile(localFile).addOnSuccessListener {
                    Toast.makeText(context, "Installing...", Toast.LENGTH_SHORT).show()
                    installTheme(localFile, "starfield2")
                    starfield2ThemeButton.isEnabled = true
                    starfield2ThemeButton.isDownloaded = true
                    starfield2ThemeButton.isCurrent = true
                    StarfieldRenderer.theme = ThemePackage(context!!, "starfield2")
                    starfield2ThemeButton.progress = 0.0f
                }.addOnFailureListener {
                    Toast.makeText(context, "File download failed!", Toast.LENGTH_SHORT).show()
                    starfield2ThemeButton.isEnabled = true
                    starfield2ThemeButton.progress = 0.0f
                }.addOnProgressListener {
                    starfield2ThemeButton.progress = 100.0f * it.bytesTransferred / it.totalByteCount
                }
            } else {
                StarfieldRenderer.theme = ThemePackage(context!!, "starfield2")
                starfield2ThemeButton.isCurrent = true
            }
        }
        starfield2ThemeButton.setOnLongClickListener {
            val location = File(context?.getExternalFilesDir(null), "starfield2")
            if(location.exists() && location.isDirectory) {
                location.deleteRecursively()
                Toast.makeText(context, "Uninstalling...", Toast.LENGTH_SHORT).show()
                starfield2ThemeButton.isDownloaded = false
            }
            StarfieldRenderer.theme = DefaultTheme()
            true
        }
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

    //unzips temp file into external storage into theme folder
    private fun unzipFile(cacheFile : File, themeName : String) {
        val location = File(context?.getExternalFilesDir(null), themeName)
        if(!location.exists()) {
            location.mkdir()
        }
        try {
            val fileInputStream = FileInputStream(cacheFile)
            val zipInputStream = ZipInputStream(fileInputStream)
            var zipEntry: ZipEntry? = zipInputStream.nextEntry
            while (zipEntry != null) {
                if (zipEntry.isDirectory) {
                    val dir = File(location, zipEntry.name)
                    dir.mkdir()
                } else {
                    val fileOutputStream = FileOutputStream(File(location, zipEntry.name))
                    val bufferedOutputStream = BufferedOutputStream(fileOutputStream)
                    val buffer = ByteArray(1024)
                    var read = zipInputStream.read(buffer)
                    while (read > 0) {
                        bufferedOutputStream.write(buffer, 0, read)
                        read = zipInputStream.read(buffer)
                    }
                    bufferedOutputStream.close()
                    zipInputStream.closeEntry()
                }
                zipEntry = zipInputStream.nextEntry
            }
            zipInputStream.close()
        } catch(e : Exception) {
            e.printStackTrace()
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
