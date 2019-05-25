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
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.FirebaseStorage
import java.io.*
import java.lang.Exception
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream



class ThemeInfo(val name : String, val resId : Int, val themeType : Type = Type.PackageTheme) {

    interface OnDownloadListener {
        fun onSuccess()
        fun onFailure()
        fun onProgress(bytesTransferred : Long, totalByteCount : Long)
    }

    enum class Type {
        DefaultTheme,
        TestTheme,
        PackageTheme
    }

    var onDownloadListener: OnDownloadListener? = null

    var isActive = false
        private set

    var isDownloaded = false
        private set

    var shouldTryDownload = true

    val isInstalled : Boolean
        get() {
            return isDownloaded || themeType != Type.PackageTheme
        }

    private var fileDownloadTask : FileDownloadTask? = null
    private var cacheFile : File? = null

    fun stopAsyncTasks() {
        fileDownloadTask?.cancel()
        fileDownloadTask = null
        cacheFile?.delete()
        cacheFile = null
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

    fun download(context: Context) {
        val storage = FirebaseStorage.getInstance("gs://starfield-23195.appspot.com/")
        val fileRef = storage.getReference(getPackageName())
        val localFile = File.createTempFile("theme", "zip")
        val downloadTask = fileRef.getFile(localFile)
        downloadTask.addOnSuccessListener {
            Toast.makeText(context, R.string.msg_install_in_progress, Toast.LENGTH_SHORT).show()
            if (install(context, localFile)) {
                localFile.delete()
                isDownloaded = true
                onDownloadListener?.onSuccess()
            } else {
                uninstall(context)
                Toast.makeText(context, R.string.msg_install_failed, Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            localFile.delete()
            Toast.makeText(context, R.string.msg_download_failed, Toast.LENGTH_SHORT).show()
            onDownloadListener?.onFailure()
        }.addOnProgressListener {
            onDownloadListener?.onProgress(it.bytesTransferred, it.totalByteCount)
        }
        fileDownloadTask = downloadTask
        cacheFile = localFile
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

    fun setActive(context: Context, activeTheme : ThemeInfo?) : Boolean {
        val theme = when {
            themeType == Type.TestTheme -> TestTheme()
            themeType == Type.DefaultTheme && !isDownloaded -> DefaultTheme()
            else -> ThemePackage(name)
        }
        return if (!theme.loadTheme(context)) {
            Toast.makeText(context, R.string.msg_theme_load_failed, Toast.LENGTH_SHORT).show()
            false
        } else {
            StarfieldRenderer.rendererInstances.requestLoadTheme(theme)
            activeTheme?.isActive = false
            isActive = true
            true
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
            ThemeInfo("default", R.drawable.default_preview, Type.DefaultTheme),
            ThemeInfo("test", android.R.color.black, Type.TestTheme),
            ThemeInfo("classic", R.drawable.classic_preview),
            ThemeInfo("classic_color", R.drawable.classic_color_preview),
            ThemeInfo("starfield2", R.drawable.starfield2_preview)
        )
    }

}


class ThemeMenuFragment : MenuFragment() {

    class ThemeInfoAdapter(private val context : Context, private val data : Array<ThemeInfo>) : RecyclerView.Adapter<ThemeInfoAdapter.ViewHolder>() {

        private val inflater = LayoutInflater.from(context)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val downloadButton = inflater.inflate(R.layout.theme_gallery_item, parent, false)
            return ViewHolder(downloadButton)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val themeInfo = data[position]
            if(position == 0) {
                holder.setupDefaultThemeButton(context, themeInfo)
            } else {
                holder.setupThemeButton(context, themeInfo)
            }
        }

        override fun getItemCount(): Int {
            return  data.size
        }

        inner class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {

            private val downloadButton : DownloadButton = itemView as DownloadButton

            fun setupDefaultThemeButton(context: Context, themeInfo: ThemeInfo) {
                downloadButton.themeInfo = themeInfo
                downloadButton.setImageResource(themeInfo.resId)
                themeInfo.onDownloadListener = object : ThemeInfo.OnDownloadListener {

                    override fun onSuccess() {
                        setCurrentItem(context, 0, true)
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

                }
                if(!themeInfo.isDownloaded && themeInfo.shouldTryDownload && SettingsProvider.askDownloadDefaultTheme) {
                    AlertDialog.Builder(context).setTitle(R.string.dlg_download_texture_title).setMessage(R.string.dlg_download_texture_text)
                        .setPositiveButton(R.string.btn_yes) { dialog, which ->
                            downloadButton.isEnabled = false
                            themeInfo.shouldTryDownload = false
                            themeInfo.download(context)
                        }
                        .setNegativeButton(R.string.btn_no) {
                            dialog, which -> themeInfo.shouldTryDownload = false
                        }
                        .setNeutralButton(R.string.btn_never) {
                            dialog, which ->
                            themeInfo.shouldTryDownload = false
                            SettingsProvider.askDownloadDefaultTheme = false

                        }
                        .show()
                }
                downloadButton.setOnClickListener {
                    setCurrentItem(context, 0)
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

            fun setupThemeButton(context: Context, themeInfo : ThemeInfo) {
                downloadButton.themeInfo = themeInfo
                downloadButton.setImageResource(themeInfo.resId)
                themeInfo.onDownloadListener = object : ThemeInfo.OnDownloadListener {

                    override fun onSuccess() {
                        setCurrentItem(context, adapterPosition)
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
                }
                downloadButton.setOnClickListener {
                    if(!themeInfo.isDownloaded && themeInfo.themeType == ThemeInfo.Type.PackageTheme) {
                        downloadButton.isEnabled = false
                        themeInfo.download(context)
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
                    if(data[pos].setActive(context, data[SettingsProvider.currentTheme])) {
                        notifyItemChanged(SettingsProvider.currentTheme)
                        notifyItemChanged(pos)
                        SettingsProvider.currentTheme = pos
                    }
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
        themeGallery.adapter = ThemeInfoAdapter(context!!, ThemeInfo.themes)
    }

    override fun onDestroyView() {
        for(theme in ThemeInfo.themes) {
            theme.stopAsyncTasks()
        }
        super.onDestroyView()
    }
}
