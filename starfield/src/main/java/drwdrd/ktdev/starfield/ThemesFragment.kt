package drwdrd.ktdev.starfield

import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.FirebaseStorage
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.Exception
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream


class ThemesFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.themes_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)

        val starfield2ThemeButton = view.findViewById<Button>(R.id.starfield2ThemeButton)
        starfield2ThemeButton.setOnClickListener {
            starfield2ThemeButton.isEnabled = false
            progressBar.visibility = View.VISIBLE
            val storage = FirebaseStorage.getInstance("gs://starfield-23195.appspot.com/")
            val fileRef = storage.getReference("theme/starfield2/png/starfield2.zip")
            val localFile = File.createTempFile("theme", "zip")
            fileRef.getFile(localFile).addOnSuccessListener {
                progressBar.visibility = View.INVISIBLE
                starfield2ThemeButton.isEnabled = true
                Toast.makeText(context, "File downloaded successfully!", Toast.LENGTH_SHORT).show()
                unzipFile(localFile, "starfield2")
            }.addOnFailureListener {
                Toast.makeText(context, "File download failed!", Toast.LENGTH_SHORT).show()
            }.addOnProgressListener {
                val progress = 100.0 * it.bytesTransferred / it.totalByteCount
                progressBar.progress = progress.toInt()
            }
        }
    }

    //unzips temp file into external storage into theme folder
    fun unzipFile(cacheFile : File, themeName : String) {
        val location = context?.getExternalFilesDir(null)
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
}