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

        val defaultThemeButton = view.findViewById<DownloadButton>(R.id.defaultThemeButton)
        defaultThemeButton.isDownloaded = true
        defaultThemeButton.setOnClickListener {
            StarfieldRenderer.theme = DefaultTheme()
            defaultThemeButton.isCurrent = true
        }

        val classicThemeButton = view.findViewById<DownloadButton>(R.id.classicThemeButton)
        classicThemeButton.isDownloaded = hasTheme("classic")
        classicThemeButton.setOnClickListener {
            if(!hasTheme("classic")) {
                classicThemeButton.isEnabled = false
                val storage = FirebaseStorage.getInstance("gs://starfield-23195.appspot.com/")
                val fileRef = storage.getReference(getPackageName("classic"))
                val localFile = File.createTempFile("theme", "zip")
                fileRef.getFile(localFile).addOnSuccessListener {
                    Toast.makeText(context, "Installing...", Toast.LENGTH_SHORT).show()
                    installTheme(localFile, "classic")
                    localFile.delete()
                    classicThemeButton.isEnabled = true
                    classicThemeButton.isDownloaded = true
                    classicThemeButton.isCurrent = true
                    StarfieldRenderer.theme = ThemePackage(context!!, "classic")
                    classicThemeButton.progress = 0.0f
                }.addOnFailureListener {
                    Toast.makeText(context, "File download failed!", Toast.LENGTH_SHORT).show()
                    localFile.delete()
                    classicThemeButton.isEnabled = true
                    classicThemeButton.progress = 0.0f
                }.addOnProgressListener {
                    classicThemeButton.progress = 100.0f * it.bytesTransferred / it.totalByteCount
                }
            } else {
                StarfieldRenderer.theme = ThemePackage(context!!, "classic")
                classicThemeButton.isCurrent = true
            }
        }
        classicThemeButton.setOnLongClickListener {
            val location = File(context?.getExternalFilesDir(null), "classic")
            if(location.exists() && location.isDirectory) {
                location.deleteRecursively()
                Toast.makeText(context, "Uninstalling...", Toast.LENGTH_SHORT).show()
                classicThemeButton.isDownloaded = false
            }
            StarfieldRenderer.theme = DefaultTheme()
            true
        }

        val starfieldThemeButton = view.findViewById<DownloadButton>(R.id.starfieldThemeButton)
        starfieldThemeButton.isDownloaded = hasTheme("starfield2")
        starfieldThemeButton.setOnClickListener {
            if(!hasTheme("starfield2")) {
                starfieldThemeButton.isEnabled = false
                val storage = FirebaseStorage.getInstance("gs://starfield-23195.appspot.com/")
                val fileRef = storage.getReference(getPackageName("starfield2"))
                val localFile = File.createTempFile("theme", "zip")
                fileRef.getFile(localFile).addOnSuccessListener {
                    Toast.makeText(context, "Installing...", Toast.LENGTH_SHORT).show()
                    installTheme(localFile, "starfield2")
                    localFile.delete()
                    starfieldThemeButton.isEnabled = true
                    starfieldThemeButton.isDownloaded = true
                    starfieldThemeButton.isCurrent = true
                    StarfieldRenderer.theme = ThemePackage(context!!, "starfield2")
                    starfieldThemeButton.progress = 0.0f
                }.addOnFailureListener {
                    Toast.makeText(context, "File download failed!", Toast.LENGTH_SHORT).show()
                    localFile.delete()
                    starfieldThemeButton.isEnabled = true
                    starfieldThemeButton.progress = 0.0f
                }.addOnProgressListener {
                    starfieldThemeButton.progress = 100.0f * it.bytesTransferred / it.totalByteCount
                }
            } else {
                StarfieldRenderer.theme = ThemePackage(context!!, "starfield2")
                starfieldThemeButton.isCurrent = true
            }
        }
        starfieldThemeButton.setOnLongClickListener {
            val location = File(context?.getExternalFilesDir(null), "starfield2")
            if(location.exists() && location.isDirectory) {
                location.deleteRecursively()
                Toast.makeText(context, "Uninstalling...", Toast.LENGTH_SHORT).show()
                starfieldThemeButton.isDownloaded = false
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
