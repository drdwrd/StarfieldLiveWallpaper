package drwdrd.ktdev.starfield_free

import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import java.net.MalformedURLException
import java.net.URL
import com.google.ads.mediation.admob.AdMobAdapter
import android.os.Bundle
import com.google.ads.consent.*
import drwdrd.ktdev.engine.Log


private const val TAG = "drwdrd.ktdev.starfield_free.ConsentProvider"

class ConsentProvider(_onAdFreeVersionRequested: OnAdFreeVersionRequested?) {

    interface OnAdFreeVersionRequested {
        fun onRequest()
    }

    var userPrefersAdFree = false
        private set

    var consentStatus = ConsentStatus.UNKNOWN
        private set

    private lateinit var consentForm : ConsentForm
    private var onAdFreeVersionRequested : OnAdFreeVersionRequested? = _onAdFreeVersionRequested

    fun initialize(context: Context, reset : Boolean = false) {
        val consentInformation = ConsentInformation.getInstance(context)
        if(reset) {
            consentInformation.reset()
        }
        //TODO: remove debug code
//        consentInformation.addTestDevice("117D43E6FBDD7EC3C5A7E7E4D3381427")
//        consentInformation.debugGeography = DebugGeography.DEBUG_GEOGRAPHY_EEA
        val publisherIds = arrayOf(context.getString(R.string.admob_id))
        consentInformation.requestConsentInfoUpdate(publisherIds, object : ConsentInfoUpdateListener {
            override fun onConsentInfoUpdated(status: ConsentStatus) {
                Log.debug(TAG, "ConsentProvider consentStatus = $status")
                consentStatus = status
                if(status == ConsentStatus.UNKNOWN) {
                    if(ConsentInformation.getInstance(context).isRequestLocationInEeaOrUnknown) {
                        loadConsentForm(context)
                    }
                }
            }

            override fun onFailedToUpdateConsentInfo(errorDescription: String) {
                Log.error(TAG, "Cannot update consent status : $errorDescription")
            }
        })
    }

    fun requestBannerAd(context: Context) : AdRequest {
        MobileAds.initialize(context, context.getString(R.string.admob_app_id))
        return when(consentStatus) {
            ConsentStatus.PERSONALIZED -> AdRequest.Builder().build()
            ConsentStatus.UNKNOWN -> AdRequest.Builder().build()
            ConsentStatus.NON_PERSONALIZED -> {
                val extras = Bundle()
                extras.putString("npa", "1")
                AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter::class.java, extras).build()
            }
        }
    }

    private fun loadConsentForm(context: Context) : Boolean {
        val privacyUrl: URL?
        try {
            privacyUrl = URL("https://drdwrd.github.io/starfield_privacy.html")
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
                    Log.debug(TAG, "ConsentProvider consentStatus = $status, userPrefersAdFree = $adFree")
                    if(userPrefersAdFree) {
                        onAdFreeVersionRequested?.onRequest()
                    }
                }

                override fun onConsentFormError(errorDescription: String?) {
                    Log.error(TAG, "Cannot load consent form : $errorDescription")
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