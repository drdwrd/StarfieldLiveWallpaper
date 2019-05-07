package drwdrd.ktdev.starfield

import android.app.AlertDialog
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



class ThemeInfo(val name : String, val resId : Int, val isDefaultTheme : Boolean = false) {

    interface OnDownloadListener {
        fun onSucces()
        fun onFailure()
        fun onProgress(bytesTransferred : Long, totalByteCount : Long)
    }

    var isActive = false
        private set

    var isDownloaded = false
        private set

    var shouldTryDownload = true

    val isInstalled : Boolean
        get() {
            return isDownloaded || isDefaultTheme
        }

    fun uninstall(context: Context) : Boolean {
        val location = File(context.getExternalFilesDir(null), name)
        if(location.exists() && location.isDirectory) {
            location.deleteRecursively()
        }
        shouldTryDownload = false
        isDownloaded = false
        isActive = false
        return true
    }

    fun checkInstallation(context: Context) {
        val location = File(context.getExternalFilesDir(null), name)
        isDownloaded = (location.exists() && location.isDirectory)
    }

    fun download(context: Context, onDownloadListener: OnDownloadListener?) {
        val storage = FirebaseStorage.getInstance("gs://starfield-23195.appspot.com/")
        val fileRef = storage.getReference(getPackageName())
        val localFile = File.createTempFile("theme", "zip")
        fileRef.getFile(localFile).addOnSuccessListener {
            Toast.makeText(context, "Installing...", Toast.LENGTH_SHORT).show()
            if(install(context, localFile)) {
                localFile.delete()
                isDownloaded = true
                onDownloadListener?.onSucces()
            } else {
                uninstall(context)
                Toast.makeText(context, "Cannot install theme!", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            localFile.delete()
            Toast.makeText(context, "File download failed!", Toast.LENGTH_SHORT).show()
            onDownloadListener?.onFailure()
        }.addOnProgressListener {
            onDownloadListener?.onProgress(it.bytesTransferred, it.totalByteCount)
        }
    }

    private fun install(context: Context, cacheFile : File) : Boolean {
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

    fun setActive(context: Context, activeTheme : ThemeInfo?) {
        val theme = if(isDefaultTheme && !isDownloaded) DefaultTheme() else ThemePackage(name)
        if (!theme.loadTheme(context)) {
            Toast.makeText(context, "Cannot load theme!", Toast.LENGTH_SHORT).show()
        } else {
            StarfieldRenderer.theme = theme
//            StarfieldRenderer.theme = TestTheme()
            activeTheme?.isActive = false
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

    companion object {

        val themes = arrayOf(
            ThemeInfo("default", R.drawable.default_preview, true),
            ThemeInfo("classic", android.R.color.black),
            ThemeInfo("classic_color", android.R.color.black),
            ThemeInfo("starfield2", R.drawable.starfield2_preview)
        )
    }

}


class ThemeMenuFragment : MenuFragment() {

    inner class ThemeInfoAdapter(private val data : Array<ThemeInfo>, context : Context) : RecyclerView.Adapter<ThemeInfoAdapter.ViewHolder>() {

        private val inflater = LayoutInflater.from(context)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val downloadButton = inflater.inflate(R.layout.theme_gallery_item, parent, false)
            return ViewHolder(downloadButton)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val themeInfo = data[position]
            holder.setupThemeButton(context!!, themeInfo)
            if(position == 0) {
                holder.setupDefaultThemeButton(context!!, themeInfo)
            }
        }

        override fun getItemCount(): Int {
            return  data.size
        }

        inner class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
            private val downloadButton : DownloadButton = itemView as DownloadButton

            fun setupDefaultThemeButton(context: Context, themeInfo: ThemeInfo) {
                if(!themeInfo.isDownloaded && themeInfo.shouldTryDownload) {
                    AlertDialog.Builder(context).setTitle("Notification").setMessage("Download optimized textures for default theme?").setIcon(android.R.drawable.ic_dialog_info)
                        .setPositiveButton(android.R.string.yes) { dialog, which ->  data[0].download(context, object : ThemeInfo.OnDownloadListener {

                            override fun onSucces() {
                                setCurrentItem(context, adapterPosition, true)
                                downloadButton.setProgress(0, 0)
                                downloadButton.isEnabled = true
                            }

                            override fun onFailure() {
                                downloadButton.setProgress(0, 0)
                                downloadButton.isEnabled = true
                            }

                            override fun onProgress(bytesTransferred: Long, totalByteCount: Long) {
                                downloadButton.setProgress(bytesTransferred, totalByteCount)
                            }

                        }) }
                        .setNegativeButton(android.R.string.no) {
                            dialog, which -> themeInfo.shouldTryDownload = false
                        }
                        .show()
                }
            }

            fun setupThemeButton(context: Context, themeInfo : ThemeInfo) {
                downloadButton.themeInfo = themeInfo
                downloadButton.setImageResource(themeInfo.resId)
                downloadButton.setOnClickListener {
                    if(!themeInfo.isDownloaded && !themeInfo.isDefaultTheme) {
                        downloadButton.isEnabled = false
                        themeInfo.download(context, object : ThemeInfo.OnDownloadListener {

                            override fun onSucces() {
                                setCurrentItem(context, adapterPosition)
                                downloadButton.setProgress(0, 0)
                                downloadButton.isEnabled = true
                            }

                            override fun onFailure() {
                                downloadButton.setProgress(0, 0)
                                downloadButton.isEnabled = true
                            }

                            override fun onProgress(bytesTransferred: Long, totalByteCount: Long) {
                                downloadButton.bytesTransferred = bytesTransferred
                                downloadButton.totalBytesCount = totalByteCount
                                downloadButton.setProgress(bytesTransferred, totalByteCount)
                            }
                        })
                    } else {
                        setCurrentItem(context, adapterPosition)
                    }
                }
                downloadButton.setOnLongClickListener {
                    if(themeInfo.isDownloaded) {
                        if (themeInfo.uninstall(context)) {
                            Toast.makeText(context, "Uninstalling...", Toast.LENGTH_SHORT).show()
                        }
                        if (SettingsProvider.currentTheme == adapterPosition) {
                            setCurrentItem(context, 0, true)
                        }
                    }
                    true
                }
            }

            private fun setCurrentItem(context: Context, pos : Int, force : Boolean = false) {
                if(force || (SettingsProvider.currentTheme != pos)) {
                    data[pos].setActive(context, data[SettingsProvider.currentTheme])
                    notifyItemChanged(SettingsProvider.currentTheme)
                    notifyItemChanged(pos)
                    SettingsProvider.currentTheme = pos
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

        for (theme in ThemeInfo.themes) {
            theme.checkInstallation(context!!)
        }

        val themeGallery = view.findViewById<RecyclerView>(R.id.themeGallery)
        themeGallery.setHasFixedSize(true)
        themeGallery.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        themeGallery.adapter = ThemeInfoAdapter(ThemeInfo.themes, context!!)
    }
}
