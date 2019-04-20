package drwdrd.ktdev.starfield

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.leanback.widget.HorizontalGridView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import drwdrd.ktdev.engine.Log
import java.io.*
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
                        installTheme(localFile, themeInfo.name)
                        localFile.delete()
                        themeButton.isEnabled = true
                        themeButton.isDownloaded = true
                        setCurrentItem(themeInfo)
                        StarfieldRenderer.theme = ThemePackage(context!!, themeInfo.name)
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
                    StarfieldRenderer.theme = ThemePackage(context!!, themeInfo.name)
                    setCurrentItem(themeInfo)
                }
            }
            themeButton.setOnLongClickListener {
                val location = File(context?.getExternalFilesDir(null), themeInfo.name)
                if(location.exists() && location.isDirectory) {
                    location.deleteRecursively()
                    Toast.makeText(context, "Uninstalling...", Toast.LENGTH_SHORT).show()
                    themeButton.isDownloaded = false
                }
                StarfieldRenderer.theme = DefaultTheme()
                setCurrentItem(data[0])
                true
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

        val themeGallery = view.findViewById<HorizontalGridView>(R.id.themeGallery)
        themeGallery.layoutManager = GridLayoutManager(context, 1, GridLayoutManager.HORIZONTAL, false)
        themeGallery.adapter = ThemeInfoAdapter(data, context!!)
    }
}
