package drwdrd.ktdev.starfield

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import androidx.appcompat.app.AlertDialog

class PrivacyPolicyDialogHandler(val context: Context) {

    private val consentProvider = ConsentProvider(object : ConsentProvider.OnAdFreeVersionRequested {
        override fun onRequest() {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://play.google.com/store/apps/details?id=drwdrd.ktdev.starfield")
                setPackage("com.android.vending")
            }
            context.startActivity(intent)
        }
    })

    fun onShowPrivacyDialog() {
        if(consentProvider.isConsentRequired(context)) {
            consentProvider.initialize(context, true)
        } else {
            // create a WebView with the current stats
            val webView = WebView(context)
            webView.loadUrl(BuildConfig.urlPrivacyPolicy)

            // display the WebView in an AlertDialog
            val builder = AlertDialog.Builder(context)
            builder.setTitle(R.string.dlg_show_privacy_title).setView(webView).setNeutralButton(R.string.btn_ok, null).show()
        }
    }
}
