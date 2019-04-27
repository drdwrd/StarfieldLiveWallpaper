package drwdrd.ktdev.starfield

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import java.io.*
import java.lang.Exception
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream



//TODO: split data from view
data class ThemeInfo(val name : String, val resId : Int, var isActive : Boolean)


class ThemeMenuFragment : MenuFragment() {

    inner class ThemeInfoAdapter(private val data : ArrayList<ThemeInfo>, context : Context) : RecyclerView.Adapter<ThemeInfoAdapter.ViewHolder>() {

        private val inflater = LayoutInflater.from(context)
        private var currentItem : ThemeInfo = data[0]

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val downloadButton = inflater.inflate(R.layout.theme_gallery_item, parent, false)
            return ViewHolder(downloadButton)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val themeInfo = data[position]
            if(position == 0) {
                setupDefaultThemeButton(holder.downloadButton, themeInfo)
            } else {
                setupThemeButton(holder.downloadButton, themeInfo)
            }
        }

        override fun getItemCount(): Int {
            return  data.size
        }

        inner class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
            val downloadButton : DownloadButton = itemView as DownloadButton
        }

        private fun setupDefaultThemeButton(themeButton: DownloadButton, themeInfo : ThemeInfo) {
            themeButton.themeInfo = themeInfo
            themeButton.isDownloaded = true
            themeButton.setImageResource(themeInfo.resId)
            themeButton.setOnClickListener {
                StarfieldRenderer.theme = DefaultTheme()
                setCurrentItem(themeInfo)
            }

        }

        private fun setupThemeButton(themeButton : DownloadButton, themeInfo : ThemeInfo) {
            themeButton.themeInfo = themeInfo
            themeButton.isDownloaded = hasTheme(themeInfo.name)
            themeButton.setImageResource(themeInfo.resId)
            themeButton.setOnClickListener {
                if(!hasTheme(themeInfo.name)) {
                    themeButton.isEnabled = false
                    val storage = FirebaseStorage.getInstance("gs://starfield-23195.appspot.com/")
                    val fileRef = storage.getReference(getPackageName(themeInfo.name))
                    val localFile = File.createTempFile("theme", "zip")
                    fileRef.getFile(localFile).addOnSuccessListener {
                        Toast.makeText(context, "Installing...", Toast.LENGTH_SHORT).show()
                        if(installTheme(localFile, themeInfo.name)) {
                            localFile.delete()
                            themeButton.isDownloaded = true
                            setCurrentTheme(context, themeInfo)
                        } else {
                            uninstallTheme(themeInfo)
                            Toast.makeText(context, "Cannot install theme!", Toast.LENGTH_SHORT).show()
                        }
                        themeButton.isEnabled = true
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
                    setCurrentTheme(context, themeInfo)
                }
            }
            themeButton.setOnLongClickListener {
                if(uninstallTheme(themeInfo)) {
                    Toast.makeText(context, "Uninstalling...", Toast.LENGTH_SHORT).show()
                    themeButton.isDownloaded = false
                }
                StarfieldRenderer.theme = DefaultTheme()
                setCurrentItem(data[0])
                true
            }
        }

        private fun uninstallTheme(themeInfo: ThemeInfo) : Boolean {
            val location = File(context?.getExternalFilesDir(null), themeInfo.name)
            if(location.exists() && location.isDirectory) {
                location.deleteRecursively()
            }
            return true
        }

        private fun setCurrentTheme(context: Context?, themeInfo : ThemeInfo) {
            if(context != null) {
                val theme = ThemePackage(themeInfo.name)
                if (!theme.loadTheme(context)) {
                    Toast.makeText(context, "Cannot load theme!", Toast.LENGTH_SHORT).show()
                } else {
                    StarfieldRenderer.theme = theme
                    setCurrentItem(themeInfo)
                }
            } else {
                Toast.makeText(context, "Cannot load theme!", Toast.LENGTH_SHORT).show()
            }
        }

        private fun setCurrentItem(themeInfo : ThemeInfo) {
            currentItem.isActive = false
            currentItem = themeInfo
            themeInfo.isActive = true
            notifyDataSetChanged()
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

        private fun installTheme(cacheFile : File, theme : String) : Boolean {
            val cacheDir = context?.getExternalFilesDir(null) ?: return false
            try {
                val location = File(cacheDir, theme)
                if (!location.exists()) {
                    location.mkdir()
                }
                ZipInputStream(FileInputStream(cacheFile)).use { zipInputStream ->
                    var zipEntry: ZipEntry? = zipInputStream.nextEntry
                    while (zipEntry != null) {
                        if (zipEntry.isDirectory) {
                            val dir = File(location, zipEntry.name)
                            dir.mkdir()
                        } else {
                            BufferedOutputStream(FileOutputStream(File(location, zipEntry.name))).use { bufferedOutputStream ->
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
            } catch (e : Exception) {
                return false
            }
            return true
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.theme_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val backButton = view.findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            onMenuFragmentInteraction("browser", "back")
        }

        val data = arrayListOf(
            ThemeInfo("default", R.drawable.default_preview, true),
            ThemeInfo("classic", android.R.color.black, false),
            ThemeInfo("classic_color", android.R.color.black, false),
            ThemeInfo("starfield2", R.drawable.starfield2_preview, false))

        val themeGallery = view.findViewById<RecyclerView>(R.id.themeGallery)
        themeGallery.setHasFixedSize(true)
        themeGallery.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        themeGallery.adapter = ThemeInfoAdapter(data, context!!)
    }
}
