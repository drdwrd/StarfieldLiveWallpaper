package drwdrd.ktdev.starfield

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import java.net.MalformedURLException
import java.net.URL
import com.google.ads.mediation.admob.AdMobAdapter
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import com.google.ads.consent.*
import com.google.android.gms.ads.AdView
import drwdrd.ktdev.engine.logd
import drwdrd.ktdev.engine.loge

class AdProvider(val context: Context) {

    var userPrefersAdFree = false
        private set

    var consentStatus = ConsentStatus.UNKNOWN
        private set

    private lateinit var consentForm : ConsentForm

    fun requestConsent() {
        initialize(context, false)
    }

    fun requestMainBannerAd() {
        MobileAds.initialize(context, context.getString(R.string.admob_app_id))
        val adRequest = when(consentStatus) {
            ConsentStatus.PERSONALIZED -> AdRequest.Builder().build()
            ConsentStatus.UNKNOWN -> AdRequest.Builder().build()
            ConsentStatus.NON_PERSONALIZED -> {
                val extras = Bundle()
                extras.putString("npa", "1")
                AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter::class.java, extras).build()
            }
        }
        val adView = (context as AppCompatActivity).findViewById<AdView>(R.id.mainAdBanner)
        adView.loadAd(adRequest)
    }

    fun requestSettingsBannerAd(container : View) {
        MobileAds.initialize(context, context.getString(R.string.admob_app_id))
        val adRequest = when(consentStatus) {
            ConsentStatus.PERSONALIZED -> AdRequest.Builder().build()
            ConsentStatus.UNKNOWN -> AdRequest.Builder().build()
            ConsentStatus.NON_PERSONALIZED -> {
                val extras = Bundle()
                extras.putString("npa", "1")
                AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter::class.java, extras).build()
            }
        }
        val adView = container.findViewById<AdView>(R.id.settingsAdBanner)
        adView.loadAd(adRequest)
    }

    fun requestPrivacyDialog() {
        if(isConsentRequired(context)) {
            initialize(context, true)
        } else {
            // create a WebView with the current stats
            val webView = WebView(context)
            webView.loadUrl(BuildConfig.urlPrivacyPolicy)

            // display the WebView in an AlertDialog
            val builder = AlertDialog.Builder(context)
            builder.setTitle(R.string.dlg_show_privacy_title).setView(webView).setNeutralButton(R.string.btn_ok, null).show()
        }
    }

    private fun initialize(context: Context, reset : Boolean) {
        val consentInformation = ConsentInformation.getInstance(context)
        if(reset) {
            consentInformation.reset()
        }
        //TODO: remove debug code
        if(BuildConfig.DEBUG) {
            consentInformation.addTestDevice("117D43E6FBDD7EC3C5A7E7E4D3381427")
            //consentInformation.debugGeography = DebugGeography.DEBUG_GEOGRAPHY_NOT_EEA
        }
        val publisherIds = arrayOf(context.getString(R.string.admob_id))
        consentInformation.requestConsentInfoUpdate(publisherIds, object : ConsentInfoUpdateListener {
            override fun onConsentInfoUpdated(status: ConsentStatus) {
                logd("AdProvider consentStatus = $status", enclosingClass = AdProvider::class)
                consentStatus = status
                if(status == ConsentStatus.UNKNOWN) {
                    if(ConsentInformation.getInstance(context).isRequestLocationInEeaOrUnknown) {
                        loadConsentForm(context)
                    }
                }
            }

            override fun onFailedToUpdateConsentInfo(errorDescription: String) {
                loge("Cannot update consent status : $errorDescription", enclosingClass = AdProvider::class)
            }
        })
    }

    private fun isConsentRequired(context: Context) : Boolean {
        return ConsentInformation.getInstance(context).isRequestLocationInEeaOrUnknown
    }

    private fun loadConsentForm(context: Context) : Boolean {
        val privacyUrl: URL?
        try {
            privacyUrl = URL(BuildConfig.urlPrivacyPolicy)
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            return false
        }

        consentForm = ConsentForm.Builder(context, privacyUrl)
            .withListener(object : ConsentFormListener() {
                override fun onConsentFormLoaded() {
                    consentForm.show()
                }

                override fun onConsentFormOpened() {

                }

                override fun onConsentFormClosed(status: ConsentStatus?, adFree: Boolean?) {
                    userPrefersAdFree = adFree ?: false
                    consentStatus = status ?: ConsentStatus.UNKNOWN
                    logd("AdProvider consentStatus = $status, userPrefersAdFree = $adFree", enclosingClass = AdProvider::class)
                    if(userPrefersAdFree) {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("https://play.google.com/store/apps/details?id=drwdrd.ktdev.starfield")
                            setPackage("com.android.vending")
                        }
                        context.startActivity(intent)
                    }
                }

                override fun onConsentFormError(errorDescription: String?) {
                    loge("Cannot load consent form : $errorDescription")
                }
            })
            .withPersonalizedAdsOption()
            .withNonPersonalizedAdsOption()
            .withAdFreeOption()
            .build()
        consentForm.load()
        return true
    }

}
