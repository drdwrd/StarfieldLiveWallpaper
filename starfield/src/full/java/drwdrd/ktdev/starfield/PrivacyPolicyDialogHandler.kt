package drwdrd.ktdev.starfield

import android.content.Context
import android.webkit.WebView
import androidx.appcompat.app.AlertDialog

class PrivacyPolicyDialogHandler(val context: Context) {

    fun onShowPrivacyDialog() {
        // create a WebView with the current stats
        val webView = WebView(context)
        webView.loadUrl(BuildConfig.urlPrivacyPolicy)

        // display the WebView in an AlertDialog
        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.dlg_show_privacy_title).setView(webView).setNeutralButton(R.string.btn_ok, null).show()
    }
}
