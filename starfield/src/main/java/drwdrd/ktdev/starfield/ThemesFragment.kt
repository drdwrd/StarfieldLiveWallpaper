package drwdrd.ktdev.starfield

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.lang.Exception


class ThemesFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.themes_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val starfield2ThemeButton = view.findViewById<Button>(R.id.starfield2ThemeButton)
        starfield2ThemeButton.setOnClickListener {
            val auth = FirebaseAuth.getInstance()

            val storage = FirebaseStorage.getInstance("gs://starfield-23195.appspot.com/")
            val fileRef = storage.getReference("theme/starfield2/png/starfield2.zip")
            val localFile = File.createTempFile("theme", "zip")
            fileRef.getFile(localFile).addOnSuccessListener( object : OnSuccessListener<FileDownloadTask.TaskSnapshot> {
                override fun onSuccess(p0: FileDownloadTask.TaskSnapshot?) {
                    Toast.makeText(context, "File downloaded successfully!", Toast.LENGTH_SHORT).show()
                }
            }).addOnFailureListener(object : OnFailureListener {
                override fun onFailure(p0: Exception) {
                    Toast.makeText(context, "File download filed!", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}