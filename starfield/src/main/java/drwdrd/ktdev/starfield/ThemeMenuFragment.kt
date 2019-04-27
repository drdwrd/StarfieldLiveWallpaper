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



class ThemeInfo(val name : String, val resId : Int, private val isDefaultTheme : Boolean = false) {

    interface OnDownloadListener {
        fun onSucces()
        fun onFailure()
        fun onProgress(progress : Float)
    }

    var isActive = isDefaultTheme
        private set

    var isDownloaded = isDefaultTheme
        private set

    fun uninstallTheme(context: Context) : Boolean {
        val location = File(context.getExternalFilesDir(null), name)
        if(location.exists() && location.isDirectory) {
            location.deleteRecursively()
        }
        isDownloaded = false
        isActive = false
        return true
    }

    fun checkThemeInstallation(context: Context) {
        val location = File(context.getExternalFilesDir(null), name)
        isDownloaded = isDefaultTheme || (location.exists() && location.isDirectory)
    }

    fun downloadTheme(context: Context, onDownloadListener: OnDownloadListener) {
        val storage = FirebaseStorage.getInstance("gs://starfield-23195.appspot.com/")
        val fileRef = storage.getReference(getPackageName())
        val localFile = File.createTempFile("theme", "zip")
        fileRef.getFile(localFile).addOnSuccessListener {
            Toast.makeText(context, "Installing...", Toast.LENGTH_SHORT).show()
            if(installTheme(context, localFile)) {
                localFile.delete()
                isDownloaded = true
                onDownloadListener.onSucces()
            } else {
                uninstallTheme(context)
                Toast.makeText(context, "Cannot install theme!", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            localFile.delete()
            Toast.makeText(context, "File download failed!", Toast.LENGTH_SHORT).show()
            onDownloadListener.onFailure()
        }.addOnProgressListener {
            onDownloadListener.onProgress(100.0f * it.bytesTransferred / it.totalByteCount)
        }
    }

    private fun installTheme(context: Context, cacheFile : File) : Boolean {
        val cacheDir = context.getExternalFilesDir(null) ?: return false
        try {
            val location = File(cacheDir, name)
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

    fun setCurrentTheme(context: Context, activeTheme : ThemeInfo) {
        val theme = if(isDefaultTheme) DefaultTheme() else ThemePackage(name)
        if (!theme.loadTheme(context)) {
            Toast.makeText(context, "Cannot load theme!", Toast.LENGTH_SHORT).show()
        } else {
            StarfieldRenderer.theme = theme
            activeTheme.isActive = false
            isActive = true
        }
    }

    private fun getPackageName() : String {
        return when {
            SettingsProvider.textureCompressionMode.hasFlag(SettingsProvider.TextureCompressionMode.ASTC) -> String.format("themes/$name/${name}_astc.zip")
            SettingsProvider.textureCompressionMode.hasFlag(SettingsProvider.TextureCompressionMode.ETC2) -> String.format("themes/$name/${name}_etc2.zip")
            SettingsProvider.textureCompressionMode.hasFlag(SettingsProvider.TextureCompressionMode.ETC1) -> String.format("themes/$name/${name}_etc.zip")
            else -> String.format("themes/$name/${name}_png.zip")
        }
    }

}


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
            themeButton.setImageResource(themeInfo.resId)
            themeButton.setOnClickListener {
                setCurrentItem(themeInfo)
            }

        }

        private fun setupThemeButton(themeButton : DownloadButton, themeInfo : ThemeInfo) {
            themeButton.themeInfo = themeInfo
            themeInfo.checkThemeInstallation(context!!)
            themeButton.setImageResource(themeInfo.resId)
            themeButton.setOnClickListener {
                if(!themeInfo.isDownloaded) {
                    themeButton.isEnabled = false
                    themeInfo.downloadTheme(context!!, object : ThemeInfo.OnDownloadListener {

                        override fun onSucces() {
                            setCurrentItem(themeInfo)
                            themeButton.progress = 0.0f
                            themeButton.isEnabled = true
                        }

                        override fun onFailure() {
                            themeButton.progress = 0.0f
                            themeButton.isEnabled = true
                        }

                        override fun onProgress(progress: Float) {
                            themeButton.progress = progress
                        }
                    })
                } else {
                    setCurrentItem(themeInfo)
                }
            }
            themeButton.setOnLongClickListener {
                if(themeInfo.uninstallTheme(context!!)) {
                    Toast.makeText(context, "Uninstalling...", Toast.LENGTH_SHORT).show()
                }
                if(currentItem == themeInfo) {
                    setCurrentItem(data[0])
                }
                true
            }
        }

        private fun setCurrentItem(themeInfo : ThemeInfo) {
            if(currentItem != themeInfo) {
                themeInfo.setCurrentTheme(context!!, currentItem)
                currentItem = themeInfo
                notifyDataSetChanged()
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
            ThemeInfo("classic", android.R.color.black),
            ThemeInfo("classic_color", android.R.color.black),
            ThemeInfo("starfield2", R.drawable.starfield2_preview))

        val themeGallery = view.findViewById<RecyclerView>(R.id.themeGallery)
        themeGallery.setHasFixedSize(true)
        themeGallery.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        themeGallery.adapter = ThemeInfoAdapter(data, context!!)
    }
}
