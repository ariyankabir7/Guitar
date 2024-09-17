package  com.guitarbajao.gamecraft.extrazz

import android.animation.ValueAnimator
import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.LayerDrawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.mosquito.hincash.extrazz.videoplayyer
import com.guitarbajao.gamecraft.R
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.json.JSONArray
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Base64

class CustomAdLoader(
    private val context: Context,
    private val siteAdsUrl: String,
    private val uniqueAppId: String,
    private val onAdDismiss: () -> Unit
) {

    private var earningAdModel: EarningAdModel? = null
    private var mediaPlayer: MediaPlayer? = null
    private var isMuted: Boolean = false

    init {
        System.loadLibrary("keys")
    }

    external fun Hatbc(): String

    fun loadAd() {
        val urlforAds = "${siteAdsUrl}get_ad.php"
        val requestQueue: RequestQueue = Volley.newRequestQueue(context)

        val jsonArrayRequest = object : StringRequest(urlforAds, { response ->
            val jsonArr = JSONArray(videoplayyer.decrypt(response, Hatbc()))
            if (jsonArr.length() > 0) {
                val dataObject = jsonArr.getJSONObject(0)
                earningAdModel = EarningAdModel(
                    dataObject.getString("added_on"),
                    dataObject.getString("ads_video_link"),
                    dataObject.getString("after_video_img_link"),
                    dataObject.getString("ads_id"),
                    dataObject.getString("btn_color"),
                    dataObject.getString("btn_txt"),
                    dataObject.getString("skip_btn_timer"),
                    dataObject.getString("click_link"),
                    dataObject.getString("id"),
                    dataObject.getString("title"),
                    dataObject.getString("ads_des"),
                    dataObject.getString("text_color"),
                    dataObject.getString("code"),
                    dataObject.getString("after_ads_btn_color"),
                    dataObject.getString("after_ads_btn_timmer"),
                    dataObject.getString("how_video"),
                    dataObject.getString("after_ads_text_color"),
                    dataObject.getString("after_ads_btn_text")
                )
            }
        }, { error ->
            Toast.makeText(context, "Internet Slow ! Ads load Failed", Toast.LENGTH_SHORT).show()
        }) {}

        requestQueue.add(jsonArrayRequest)
    }

    fun showAd() {
        if (earningAdModel == null) {
            Toast.makeText(context, "Ad not loaded yet!", Toast.LENGTH_SHORT).show()
            loadAd()
            return
        }
        hideStatusBar()
        val dialog = AlertDialog.Builder(context, R.style.FullScreenDialog)
            .setView(R.layout.popup_earning_ads)
            .setCancelable(false)
            .create()

        dialog.show()
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        setupDialogViews(dialog)
    }

    private fun hideStatusBar() {
        val window = (context as? Activity)?.window ?: return
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
    }

    private fun showStatusBar() {
        val window = (context as? Activity)?.window ?: return
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
    }

    private fun setupDialogViews(dialog: AlertDialog) {
        val cvTimmer = dialog.findViewById<CardView>(R.id.cv_timmer_bg)
        val cvCodeBtn = dialog.findViewById<MaterialCardView>(R.id.cv_code_btn)
        val installBtn = dialog.findViewById<CardView>(R.id.cv_action_btn)
        val installBtn2 = dialog.findViewById<MaterialCardView>(R.id.installBtn)
        val closeBtn = dialog.findViewById<ImageView>(R.id.iv_close_ad_after_video)
        val skipBtn = dialog.findViewById<ImageView>(R.id.iv_close_ad)
        val policy = dialog.findViewById<ImageView>(R.id.iv_policy)
        val earnads = dialog.findViewById<ImageView>(R.id.earnAdsImage)
        val promote = dialog.findViewById<ImageView>(R.id.promoteImage)
        val howtoVideo = dialog.findViewById<ImageView>(R.id.howImage)
        val ivAfterVideoAd = dialog.findViewById<ImageView>(R.id.iv_after_video_ad)
        val title = dialog.findViewById<TextView>(R.id.tv_ads_title)
        val desc = dialog.findViewById<TextView>(R.id.tv_ads_short_des)
        val code = dialog.findViewById<TextView>(R.id.tv_code)
        val tvaction = dialog.findViewById<TextView>(R.id.tv_action_btn)
        val tvaction2 = dialog.findViewById<TextView>(R.id.tv_action_btn2)
        val ll_afterAds = dialog.findViewById<LinearLayout>(R.id.after_ads)
        val ll_Ads = dialog.findViewById<LinearLayout>(R.id.before_ads)
        val pb = dialog.findViewById<ProgressBar>(R.id.linearProgressIndicator)
        val mute_unmute = dialog.findViewById<ImageView>(R.id.mute_unmute)
        val videoView = dialog.findViewById<VideoView>(R.id.vv_show_video)

        // Setting up values
        title?.text = earningAdModel?.title
        desc?.text = earningAdModel?.desc
        code?.text = earningAdModel?.code
        tvaction?.text = earningAdModel?.btn_txt
        tvaction.setTextColor(Color.parseColor(earningAdModel!!.text_color))
        tvaction2.setTextColor(Color.parseColor(earningAdModel!!.after_ads_text_color))
        tvaction2?.text = earningAdModel?.after_ads_btn_text
        installBtn.setCardBackgroundColor(Color.parseColor(earningAdModel!!.btn_color))
        installBtn2.setCardBackgroundColor(Color.parseColor(earningAdModel!!.after_ads_btn_color))
        setupShineEffect(installBtn)

        // Set video
        val videoUri = Uri.parse(earningAdModel!!.ads_video_link)
        videoView?.setVideoURI(videoUri)
        setupVideoViewListeners(
            videoView,
            dialog,
            cvTimmer,
            mute_unmute,
            ll_afterAds,
            ll_Ads,
            pb,
            closeBtn,
            installBtn2,
            skipBtn,
            ivAfterVideoAd
        )

        // Set click listeners
        setupClickListeners(
            policy,
            earnads,
            promote,
            howtoVideo,
            videoView,
            cvCodeBtn,
            installBtn,
            installBtn2,
            skipBtn,
            dialog,
            closeBtn
        )
    }

    private fun setupShineEffect(button: CardView?) {
        val layerDrawable =
            ContextCompat.getDrawable(context, R.drawable.shine_effect) as LayerDrawable?
        layerDrawable?.let {
            button?.foreground = it
            val animator = ValueAnimator.ofFloat(-1f, 1f)
            animator.duration = 2500
            animator.repeatCount = ValueAnimator.INFINITE
            animator.interpolator = LinearInterpolator()

            animator.addUpdateListener { animation ->
                val progress = animation.animatedValue as Float
                button?.foreground?.setBounds(
                    (button.width * progress).toInt(), 0,
                    (button.width * (progress + 1)).toInt(), button.height
                )
                button?.invalidate()
            }

            animator.start()
        }
    }

    private fun setupVideoViewListeners(
        videoView: VideoView?,
        dialog: AlertDialog,
        cvTimmer: CardView?,
        mute_unmute: ImageView?,
        ll: LinearLayout?,
        bb: LinearLayout?,
        pb: ProgressBar?,
        closeBtn: ImageView?,
        installBtn2: MaterialCardView?,
        skipBtn: ImageView,
        ivAfterVideoAd: ImageView
    ) {
        val touchBlocker = View.OnTouchListener { _, _ -> true }
        videoView?.setOnErrorListener { _, what, extra ->
            Log.e("VideoPopup", "Error occurred: $what, $extra")
            Toast.makeText(context, "Error loading video", Toast.LENGTH_SHORT).show()
            true
        }
        videoView?.setOnPreparedListener { mp ->
            mediaPlayer = mp
            if (bb?.isVisible == true) {
                mp.start()
                videoView.setOnTouchListener(touchBlocker)
                val countDownTimer =
                    object : CountDownTimer(earningAdModel!!.skip_btn_timer.toLong() * 1000, 300) {
                        override fun onTick(millisUntilFinished: Long) {}
                        override fun onFinish() {
                            cvTimmer?.visibility = View.VISIBLE
                            mute_unmute?.visibility = View.VISIBLE
                            updateImpression()
                        }
                    }
                countDownTimer.start()
            }
        }
        mute_unmute?.setOnClickListener {
            isMuted = !isMuted
            mute_unmute.setImageResource(if (isMuted) R.drawable.mute else R.drawable.un_mute)
            mediaPlayer?.setVolume(if (isMuted) 0f else 1f, if (isMuted) 0f else 1f)
        }
        videoView?.setOnCompletionListener {
            videoView.pause()
            ll?.visibility = View.VISIBLE
            bb?.visibility = View.GONE
            videoView.stopPlayback()

            Glide.with(context).load(earningAdModel!!.after_video_img_link).into(ivAfterVideoAd)
            setupShineEffect(installBtn2)
            val countDownTimer =
                object : CountDownTimer(earningAdModel!!.after_ads_btn_timmer.toLong() * 1000, 50) {
                    override fun onTick(millisUntilFinished: Long) {
                        val progress =
                            ((earningAdModel!!.after_ads_btn_timmer.toFloat() * 1000 - millisUntilFinished.toFloat()) / (earningAdModel!!.after_ads_btn_timmer.toFloat() * 1000) * 100).toInt()
                        pb?.progressTintList = ColorStateList.valueOf(Color.GREEN)
                        pb?.progress = progress
                    }

                    override fun onFinish() {
                        pb?.visibility = View.GONE
                        closeBtn?.visibility = View.VISIBLE
                    }
                }
            countDownTimer.start()
            loadAd()
        }
        setupSkipBtnListener(skipBtn, videoView, ll, bb, pb, closeBtn, installBtn2, ivAfterVideoAd)
    }

    private fun setupClickListeners(
        policy: ImageView?,
        earnads: ImageView?,
        promote: ImageView?,
        howtoVideo: ImageView?,
        videoView: VideoView?,
        cvCodeBtn: MaterialCardView?,
        installBtn: CardView?,
        installBtn2: MaterialCardView?,
        skipBtn: ImageView?,
        dialog: AlertDialog,
        closeBtn: ImageView?
    ) {
        policy?.setOnClickListener { Utils.openUrl(context, "https://earningads.io/") }
        earnads?.setOnClickListener { Utils.openUrl(context, "https://earningads.io/") }
        promote?.setOnClickListener {
            Utils.openUrl(
                context,
                "https://forms.gle/HTktg8o9jouLGKP68"
            )
        }
        howtoVideo?.setOnClickListener { Utils.openUrl(context, earningAdModel!!.how_video) }
        cvCodeBtn?.setOnClickListener { copyToClipboard() }
        installBtn2?.setOnClickListener { handleInstallButtonClick(videoView) }
        installBtn?.setOnClickListener { Utils.openUrl(context, earningAdModel!!.click_link) }
        closeBtn?.setOnClickListener { handleCloseButtonClick(dialog) }
    }

    private fun setupSkipBtnListener(
        skipBtn: ImageView?,
        videoView: VideoView?,
        ll: LinearLayout?,
        bb: LinearLayout?,
        pb: ProgressBar?,
        closeBtn: ImageView?,
        installBtn2: MaterialCardView?,
        ivAfterVideoAd: ImageView
    ) {
        skipBtn?.setOnClickListener {
            videoView?.pause()
            videoView?.stopPlayback()
            ll?.visibility = View.VISIBLE
            bb?.visibility = View.GONE
            Glide.with(context).load(earningAdModel!!.after_video_img_link).into(ivAfterVideoAd)
            setupShineEffect(installBtn2)
            val countDownTimer =
                object : CountDownTimer(earningAdModel!!.after_ads_btn_timmer.toLong() * 1000, 50) {
                    override fun onTick(millisUntilFinished: Long) {
                        val progress =
                            ((earningAdModel!!.after_ads_btn_timmer.toFloat() * 1000 - millisUntilFinished.toFloat()) / (earningAdModel!!.after_ads_btn_timmer.toFloat() * 1000) * 100).toInt()
                        pb?.progressTintList = ColorStateList.valueOf(Color.GREEN)
                        pb?.progress = progress
                    }

                    override fun onFinish() {
                        pb?.visibility = View.GONE
                        closeBtn?.visibility = View.VISIBLE
                    }
                }
            countDownTimer.start()
            loadAd()
        }
    }

    private fun copyToClipboard() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("AdCode", earningAdModel?.code)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
    }

    private fun handleInstallButtonClick(videoView: VideoView?) {
        videoView?.pause()
        updateClick()
        Utils.openUrl(context, earningAdModel!!.click_link)
        videoView?.stopPlayback()
    }

    private fun handleCloseButtonClick(dialog: AlertDialog) {
        dialog.dismiss()
        showStatusBar()
        onAdDismiss.invoke()
    }

    private fun updateImpression() {
        val url3 = "${siteAdsUrl}u_i.php"
        val queue3: RequestQueue = Volley.newRequestQueue(context)

        val stringRequest = object : StringRequest(Method.POST, url3, { response ->
        }, { error ->
            Utils.dismissLoadingPopUp()
            Toast.makeText(context, "Internet Slow: $error", Toast.LENGTH_SHORT).show()
        }) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()

                val ads_id32 = videoplayyer.encrypt(earningAdModel!!.ads_id, Hatbc()).toString()
                val uapp_id32 = videoplayyer.encrypt(uniqueAppId, Hatbc()).toString()

                val den64 = Base64.getEncoder().encodeToString(ads_id32.toByteArray())
                val ten64 = Base64.getEncoder().encodeToString(uapp_id32.toByteArray())

                val encodemap: MutableMap<String, String> = HashMap()
                encodemap["deijvfijvmfhvfvhfbhbchbfybebd"] = den64
                encodemap["waofhfuisgdtdrefssfgsgsgdhddgd"] = ten64

                val jason = Json.encodeToString(encodemap)

                val den264 = Base64.getEncoder().encodeToString(jason.toByteArray())
                val final = URLEncoder.encode(den264, StandardCharsets.UTF_8.toString())

                params["dase"] = final
                return params
            }
        }
        queue3.add(stringRequest)
    }

    private fun updateClick() {
        val url3 = "${siteAdsUrl}u_c.php"
        val queue3: RequestQueue = Volley.newRequestQueue(context)

        val stringRequest = object : StringRequest(Method.POST, url3, { response ->
        }, { error ->
            Utils.dismissLoadingPopUp()
            Toast.makeText(context, "Internet Slow: $error", Toast.LENGTH_SHORT).show()
        }) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()

                val ads_id32 = videoplayyer.encrypt(earningAdModel!!.ads_id, Hatbc()).toString()
                val uapp_id32 = videoplayyer.encrypt(uniqueAppId, Hatbc()).toString()

                val den64 = Base64.getEncoder().encodeToString(ads_id32.toByteArray())
                val ten64 = Base64.getEncoder().encodeToString(uapp_id32.toByteArray())

                val encodemap: MutableMap<String, String> = HashMap()
                encodemap["deijvfijvmfhvfvhfbhbchbfybebd"] = den64
                encodemap["waofhfuisgdtdrefssfgsgsgdhddgd"] = ten64

                val jason = Json.encodeToString(encodemap)

                val den264 = Base64.getEncoder().encodeToString(jason.toByteArray())
                val final = URLEncoder.encode(den264, StandardCharsets.UTF_8.toString())

                params["dase"] = final
                return params
            }
        }
        queue3.add(stringRequest)
    }
}

// Sample EarningAdModel class
data class EarningAdModel(
    val added_on: String,
    val ads_video_link: String,
    val after_video_img_link: String,
    val ads_id: String,
    val btn_color: String,
    val btn_txt: String,
    val skip_btn_timer: String,
    val click_link: String,
    val id: String,
    val title: String,
    val desc: String,
    val text_color: String,
    val code: String,
    val after_ads_btn_color: String,
    val after_ads_btn_timmer: String,
    val how_video: String,
    val after_ads_text_color: String,
    val after_ads_btn_text: String
)

data class EarningAdBannerModel(
    val added_on: String,
    val ad_video_link: String,
    val ads_id: String,
    val click_link: String,
    val id: String,

    )

class BannerAdLoader(
    private val context: Context,
    private val siteAdsUrl: String,
    private val uniqueAppId: String,
    private val videoView: VideoView,
    private val mute_unmute: ImageView
) {

    private var earningAdBannerModel: EarningAdBannerModel? = null
    private var mediaPlayer: MediaPlayer? = null
    private var isMuted: Boolean = true

    init {
        System.loadLibrary("keys")
    }

    external fun Hatbc(): String

    fun loadBannerAd() {
        val urlforAds = "${siteAdsUrl}get_banner_ad.php"
        val requestQueue: RequestQueue = Volley.newRequestQueue(context)

        val jsonArrayRequest = object : StringRequest(urlforAds, { response ->
            val jsonArr = JSONArray(videoplayyer.decrypt(response, Hatbc()))
            if (jsonArr.length() > 0) {
                val dataObject = jsonArr.getJSONObject(0)
                earningAdBannerModel = EarningAdBannerModel(
                    dataObject.getString("added_on"),
                    dataObject.getString("ad_video_link"),
                    dataObject.getString("ads_id"),
                    dataObject.getString("click_link"),
                    dataObject.getString("id")
                )

                videoView.setVideoPath(earningAdBannerModel!!.ad_video_link)
                videoView.start()
                mute_unmute.setImageResource(if (isMuted) R.drawable.mute else R.drawable.un_mute)
                videoView.setOnCompletionListener {
                    videoView.stopPlayback()
                    videoView.setVideoPath(earningAdBannerModel!!.ad_video_link)
                    videoView.start()
                }
                videoView.setOnErrorListener { _, what, extra ->
                    Log.e("VideoPopup", "Error occurred: $what, $extra")
                    true
                }
                videoView.setOnPreparedListener { mp ->
                    mediaPlayer = mp
                    mp.start()
                    videoView.setOnTouchListener { _, _ -> true }
                }
                videoView.setOnClickListener {
                    Utils.openUrl(context, earningAdBannerModel!!.click_link)
                }
                mute_unmute.setOnClickListener {
                    isMuted = !isMuted
                    mute_unmute.setImageResource(if (isMuted) R.drawable.mute else R.drawable.un_mute)
                    mediaPlayer?.setVolume(if (isMuted) 0f else 1f, if (isMuted) 0f else 1f)
                }

            }
        }, { error ->
            Toast.makeText(context, "Internet Slow ! Ads load Failed", Toast.LENGTH_SHORT).show()
        }) {}

        requestQueue.add(jsonArrayRequest)
    }


    private fun updateImpression() {
        val url3 = "${siteAdsUrl}u_i.php"
        val queue3: RequestQueue = Volley.newRequestQueue(context)

        val stringRequest = object : StringRequest(Method.POST, url3, { response ->
        }, { error ->
            Utils.dismissLoadingPopUp()
            Toast.makeText(context, "Internet Slow: $error", Toast.LENGTH_SHORT).show()
        }) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()

                val ads_id32 = videoplayyer.encrypt(earningAdBannerModel!!.ads_id, Hatbc()).toString()
                val uapp_id32 = videoplayyer.encrypt(uniqueAppId, Hatbc()).toString()

                val den64 = Base64.getEncoder().encodeToString(ads_id32.toByteArray())
                val ten64 = Base64.getEncoder().encodeToString(uapp_id32.toByteArray())

                val encodemap: MutableMap<String, String> = HashMap()
                encodemap["deijvfijvmfhvfvhfbhbchbfybebd"] = den64
                encodemap["waofhfuisgdtdrefssfgsgsgdhddgd"] = ten64

                val jason = Json.encodeToString(encodemap)

                val den264 = Base64.getEncoder().encodeToString(jason.toByteArray())
                val final = URLEncoder.encode(den264, StandardCharsets.UTF_8.toString())

                params["dase"] = final
                return params
            }
        }
        queue3.add(stringRequest)
    }

    private fun updateClick() {
        val url3 = "${siteAdsUrl}u_c.php"
        val queue3: RequestQueue = Volley.newRequestQueue(context)

        val stringRequest = object : StringRequest(Method.POST, url3, { response ->
        }, { error ->
            Utils.dismissLoadingPopUp()
            Toast.makeText(context, "Internet Slow: $error", Toast.LENGTH_SHORT).show()
        }) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()

                val ads_id32 = videoplayyer.encrypt(earningAdBannerModel!!.ads_id, Hatbc()).toString()
                val uapp_id32 = videoplayyer.encrypt(uniqueAppId, Hatbc()).toString()

                val den64 = Base64.getEncoder().encodeToString(ads_id32.toByteArray())
                val ten64 = Base64.getEncoder().encodeToString(uapp_id32.toByteArray())

                val encodemap: MutableMap<String, String> = HashMap()
                encodemap["deijvfijvmfhvfvhfbhbchbfybebd"] = den64
                encodemap["waofhfuisgdtdrefssfgsgsgdhddgd"] = ten64

                val jason = Json.encodeToString(encodemap)

                val den264 = Base64.getEncoder().encodeToString(jason.toByteArray())
                val final = URLEncoder.encode(den264, StandardCharsets.UTF_8.toString())

                params["dase"] = final
                return params
            }
        }
        queue3.add(stringRequest)
    }
}


class InterstitialAdLoader(
    private val context: Context,
    private val siteAdsUrl: String,
    private val uniqueAppId: String,
    private val onAdDismiss: () -> Unit
) {

    private var earningAdModel: EarningAdModel? = null
    private var mediaPlayer: MediaPlayer? = null
    private var isMuted: Boolean = false

    init {
        System.loadLibrary("keys")
    }

    external fun Hatbc(): String

     fun loadInterstitalAd() {
        val urlforAds = "${siteAdsUrl}get_interstitial_ad.php"
        val requestQueue: RequestQueue = Volley.newRequestQueue(context)

        val jsonArrayRequest = object : StringRequest(urlforAds, { response ->
            val jsonArr = JSONArray(videoplayyer.decrypt(response, Hatbc()))
            if (jsonArr.length() > 0) {
                val dataObject = jsonArr.getJSONObject(0)
                earningAdModel = EarningAdModel(
                    dataObject.getString("added_on"),
                    dataObject.getString("ads_video_link"),
                    dataObject.getString("after_video_img_link"),
                    dataObject.getString("ads_id"),
                    dataObject.getString("btn_color"),
                    dataObject.getString("btn_txt"),
                    dataObject.getString("skip_btn_timer"),
                    dataObject.getString("click_link"),
                    dataObject.getString("id"),
                    dataObject.getString("title"),
                    dataObject.getString("ads_des"),
                    dataObject.getString("text_color"),
                    dataObject.getString("code"),
                    dataObject.getString("after_ads_btn_color"),
                    dataObject.getString("after_ads_btn_timmer"),
                    dataObject.getString("how_video"),
                    dataObject.getString("after_ads_text_color"),
                    dataObject.getString("after_ads_btn_text")
                )
            }
        }, { error ->
            Toast.makeText(context, "Internet Slow ! Ads load Failed", Toast.LENGTH_SHORT).show()
        }) {}

        requestQueue.add(jsonArrayRequest)
    }

    fun showInterstitialAd() {
        if (earningAdModel == null) {
            Toast.makeText(context, "Ad not loaded yet!", Toast.LENGTH_SHORT).show()
            loadInterstitalAd()
            return
        }
        hideStatusBar()
        val dialog = AlertDialog.Builder(context, R.style.FullScreenDialog)
            .setView(R.layout.popup_earning_ads)
            .setCancelable(false)
            .create()

        dialog.show()
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        setupDialogViews(dialog)
    }

    private fun hideStatusBar() {
        val window = (context as? Activity)?.window ?: return
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
    }

    private fun showStatusBar() {
        val window = (context as? Activity)?.window ?: return
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
    }

    private fun setupDialogViews(dialog: AlertDialog) {
        val cvTimmer = dialog.findViewById<CardView>(R.id.cv_timmer_bg)
        val cvCodeBtn = dialog.findViewById<MaterialCardView>(R.id.cv_code_btn)
        val installBtn = dialog.findViewById<CardView>(R.id.cv_action_btn)
        val installBtn2 = dialog.findViewById<MaterialCardView>(R.id.installBtn)
        val closeBtn = dialog.findViewById<ImageView>(R.id.iv_close_ad_after_video)
        val skipBtn = dialog.findViewById<ImageView>(R.id.iv_close_ad)
        val policy = dialog.findViewById<ImageView>(R.id.iv_policy)
        val earnads = dialog.findViewById<ImageView>(R.id.earnAdsImage)
        val promote = dialog.findViewById<ImageView>(R.id.promoteImage)
        val howtoVideo = dialog.findViewById<ImageView>(R.id.howImage)
        val ivAfterVideoAd = dialog.findViewById<ImageView>(R.id.iv_after_video_ad)
        val title = dialog.findViewById<TextView>(R.id.tv_ads_title)
        val desc = dialog.findViewById<TextView>(R.id.tv_ads_short_des)
        val code = dialog.findViewById<TextView>(R.id.tv_code)
        val tvaction = dialog.findViewById<TextView>(R.id.tv_action_btn)
        val tvaction2 = dialog.findViewById<TextView>(R.id.tv_action_btn2)
        val ll_afterAds = dialog.findViewById<LinearLayout>(R.id.after_ads)
        val ll_Ads = dialog.findViewById<LinearLayout>(R.id.before_ads)
        val pb = dialog.findViewById<ProgressBar>(R.id.linearProgressIndicator)
        val mute_unmute = dialog.findViewById<ImageView>(R.id.mute_unmute)
        val videoView = dialog.findViewById<VideoView>(R.id.vv_show_video)
        val progressBar = dialog.findViewById<ProgressBar>(R.id.circularProgressBar)

        // Setting up values
        title?.text = earningAdModel?.title
        desc?.text = earningAdModel?.desc
        code?.text = earningAdModel?.code
        tvaction?.text = earningAdModel?.btn_txt
        tvaction.setTextColor(Color.parseColor(earningAdModel!!.text_color))
        tvaction2.setTextColor(Color.parseColor(earningAdModel!!.after_ads_text_color))
        tvaction2?.text = earningAdModel?.after_ads_btn_text
        installBtn.setCardBackgroundColor(Color.parseColor(earningAdModel!!.btn_color))
        installBtn2.setCardBackgroundColor(Color.parseColor(earningAdModel!!.after_ads_btn_color))
        setupShineEffect(installBtn)

        // Set video
        val videoUri = Uri.parse(earningAdModel!!.ads_video_link)
        videoView?.setVideoURI(videoUri)
        setupVideoViewListeners(
            videoView,
            dialog,
            cvTimmer,
            mute_unmute,
            ll_afterAds,
            ll_Ads,
            pb,
            closeBtn,
            installBtn2,
            skipBtn,
            ivAfterVideoAd,
            progressBar
        )

        // Set click listeners
        setupClickListeners(
            policy,
            earnads,
            promote,
            howtoVideo,
            videoView,
            cvCodeBtn,
            installBtn,
            installBtn2,
            skipBtn,
            dialog,
            closeBtn
        )
    }

    private fun setupShineEffect(button: CardView?) {
        val layerDrawable =
            ContextCompat.getDrawable(context, R.drawable.shine_effect) as LayerDrawable?
        layerDrawable?.let {
            button?.foreground = it
            val animator = ValueAnimator.ofFloat(-1f, 1f)
            animator.duration = 2500
            animator.repeatCount = ValueAnimator.INFINITE
            animator.interpolator = LinearInterpolator()

            animator.addUpdateListener { animation ->
                val progress = animation.animatedValue as Float
                button?.foreground?.setBounds(
                    (button.width * progress).toInt(), 0,
                    (button.width * (progress + 1)).toInt(), button.height
                )
                button?.invalidate()
            }

            animator.start()
        }
    }

    private fun setupVideoViewListeners(
        videoView: VideoView?,
        dialog: AlertDialog,
        cvTimmer: CardView?,
        mute_unmute: ImageView?,
        ll: LinearLayout?,
        bb: LinearLayout?,
        pb: ProgressBar?,
        closeBtn: ImageView?,
        installBtn2: MaterialCardView?,
        skipBtn: ImageView,
        ivAfterVideoAd: ImageView,
        progressBar: ProgressBar
    ) {
        val touchBlocker = View.OnTouchListener { _, _ -> true }
        videoView?.setOnErrorListener { _, what, extra ->
            Log.e("VideoPopup", "Error occurred: $what, $extra")
            Toast.makeText(context, "Error loading video", Toast.LENGTH_SHORT).show()
            true
        }
        videoView?.setOnPreparedListener { mp ->
            mediaPlayer = mp
            if (bb?.isVisible == true) {
                mp.start()
                videoView.setOnTouchListener(touchBlocker)
                val countDownTimer =
                    object : CountDownTimer(earningAdModel!!.skip_btn_timer.toLong() * 1000, 300) {
                        override fun onTick(millisUntilFinished: Long) {
                            val progress =
                                ((earningAdModel!!.skip_btn_timer.toFloat() * 1000 - millisUntilFinished.toFloat()) / (earningAdModel!!.skip_btn_timer.toFloat() * 1000) * 100).toInt()
                            progressBar.progress = progress
                        }

                        override fun onFinish() {
                            cvTimmer?.visibility = View.VISIBLE
                            mute_unmute?.visibility = View.VISIBLE
                            progressBar.visibility = View.GONE
                            updateImpression()
                        }
                    }
                countDownTimer.start()
            }
        }
        mute_unmute?.setOnClickListener {
            isMuted = !isMuted
            mute_unmute.setImageResource(if (isMuted) R.drawable.mute else R.drawable.un_mute)
            mediaPlayer?.setVolume(if (isMuted) 0f else 1f, if (isMuted) 0f else 1f)
        }
        videoView?.setOnCompletionListener {
            videoView.pause()
            ll?.visibility = View.VISIBLE
            bb?.visibility = View.GONE
            videoView.stopPlayback()

            Glide.with(context).load(earningAdModel!!.after_video_img_link).into(ivAfterVideoAd)
            setupShineEffect(installBtn2)
            val countDownTimer =
                object : CountDownTimer(earningAdModel!!.after_ads_btn_timmer.toLong() * 1000, 50) {
                    override fun onTick(millisUntilFinished: Long) {
                        val progress =
                            ((earningAdModel!!.after_ads_btn_timmer.toFloat() * 1000 - millisUntilFinished.toFloat()) / (earningAdModel!!.after_ads_btn_timmer.toFloat() * 1000) * 100).toInt()
                        pb?.progressTintList = ColorStateList.valueOf(Color.GREEN)
                        pb?.progress = progress
                    }

                    override fun onFinish() {
                        pb?.visibility = View.GONE
                        closeBtn?.visibility = View.VISIBLE
                    }
                }
            countDownTimer.start()
            loadInterstitalAd()
        }
        setupSkipBtnListener(skipBtn, videoView, ll, bb, pb, closeBtn, installBtn2, ivAfterVideoAd)
    }

    private fun setupClickListeners(
        policy: ImageView?,
        earnads: ImageView?,
        promote: ImageView?,
        howtoVideo: ImageView?,
        videoView: VideoView?,
        cvCodeBtn: MaterialCardView?,
        installBtn: CardView?,
        installBtn2: MaterialCardView?,
        skipBtn: ImageView?,
        dialog: AlertDialog,
        closeBtn: ImageView?
    ) {
        policy?.setOnClickListener { Utils.openUrl(context, "https://earningads.io/") }
        earnads?.setOnClickListener { Utils.openUrl(context, "https://earningads.io/") }
        promote?.setOnClickListener {
            Utils.openUrl(
                context,
                "https://forms.gle/HTktg8o9jouLGKP68"
            )
        }
        howtoVideo?.setOnClickListener { Utils.openUrl(context, earningAdModel!!.how_video) }
        cvCodeBtn?.setOnClickListener { copyToClipboard() }
        installBtn2?.setOnClickListener { handleInstallButtonClick(videoView) }
        installBtn?.setOnClickListener { Utils.openUrl(context, earningAdModel!!.click_link) }
        closeBtn?.setOnClickListener { handleCloseButtonClick(dialog) }
    }

    private fun setupSkipBtnListener(
        skipBtn: ImageView?,
        videoView: VideoView?,
        ll: LinearLayout?,
        bb: LinearLayout?,
        pb: ProgressBar?,
        closeBtn: ImageView?,
        installBtn2: MaterialCardView?,
        ivAfterVideoAd: ImageView
    ) {
        skipBtn?.setOnClickListener {
            videoView?.pause()
            videoView?.stopPlayback()
            ll?.visibility = View.VISIBLE
            bb?.visibility = View.GONE
            Glide.with(context).load(earningAdModel!!.after_video_img_link).into(ivAfterVideoAd)
            setupShineEffect(installBtn2)
            val countDownTimer =
                object : CountDownTimer(earningAdModel!!.after_ads_btn_timmer.toLong() * 1000, 50) {
                    override fun onTick(millisUntilFinished: Long) {
                        val progress =
                            ((earningAdModel!!.after_ads_btn_timmer.toFloat() * 1000 - millisUntilFinished.toFloat()) / (earningAdModel!!.after_ads_btn_timmer.toFloat() * 1000) * 100).toInt()
                        pb?.progressTintList = ColorStateList.valueOf(Color.GREEN)
                        pb?.progress = progress
                    }

                    override fun onFinish() {
                        pb?.visibility = View.GONE
                        closeBtn?.visibility = View.VISIBLE
                    }
                }
            countDownTimer.start()
            loadInterstitalAd()
        }
    }

    private fun copyToClipboard() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("AdCode", earningAdModel?.code)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
    }

    private fun handleInstallButtonClick(videoView: VideoView?) {
        videoView?.pause()
        updateClick()
        Utils.openUrl(context, earningAdModel!!.click_link)
        videoView?.stopPlayback()
    }

    private fun handleCloseButtonClick(dialog: AlertDialog) {
        dialog.dismiss()
        showStatusBar()
        onAdDismiss.invoke()
    }

    private fun updateImpression() {
        val url3 = "${siteAdsUrl}u_i.php"
        val queue3: RequestQueue = Volley.newRequestQueue(context)

        val stringRequest = object : StringRequest(Method.POST, url3, { response ->
        }, { error ->
            Utils.dismissLoadingPopUp()
            Toast.makeText(context, "Internet Slow: $error", Toast.LENGTH_SHORT).show()
        }) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()

                val ads_id32 = videoplayyer.encrypt(earningAdModel!!.ads_id, Hatbc()).toString()
                val uapp_id32 = videoplayyer.encrypt(uniqueAppId, Hatbc()).toString()

                val den64 = Base64.getEncoder().encodeToString(ads_id32.toByteArray())
                val ten64 = Base64.getEncoder().encodeToString(uapp_id32.toByteArray())

                val encodemap: MutableMap<String, String> = HashMap()
                encodemap["deijvfijvmfhvfvhfbhbchbfybebd"] = den64
                encodemap["waofhfuisgdtdrefssfgsgsgdhddgd"] = ten64

                val jason = Json.encodeToString(encodemap)

                val den264 = Base64.getEncoder().encodeToString(jason.toByteArray())
                val final = URLEncoder.encode(den264, StandardCharsets.UTF_8.toString())

                params["dase"] = final
                return params
            }
        }
        queue3.add(stringRequest)
    }

    private fun updateClick() {
        val url3 = "${siteAdsUrl}u_c.php"
        val queue3: RequestQueue = Volley.newRequestQueue(context)

        val stringRequest = object : StringRequest(Method.POST, url3, { response ->
        }, { error ->
            Utils.dismissLoadingPopUp()
            Toast.makeText(context, "Internet Slow: $error", Toast.LENGTH_SHORT).show()
        }) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()

                val ads_id32 = videoplayyer.encrypt(earningAdModel!!.ads_id, Hatbc()).toString()
                val uapp_id32 = videoplayyer.encrypt(uniqueAppId, Hatbc()).toString()

                val den64 = Base64.getEncoder().encodeToString(ads_id32.toByteArray())
                val ten64 = Base64.getEncoder().encodeToString(uapp_id32.toByteArray())

                val encodemap: MutableMap<String, String> = HashMap()
                encodemap["deijvfijvmfhvfvhfbhbchbfybebd"] = den64
                encodemap["waofhfuisgdtdrefssfgsgsgdhddgd"] = ten64

                val jason = Json.encodeToString(encodemap)

                val den264 = Base64.getEncoder().encodeToString(jason.toByteArray())
                val final = URLEncoder.encode(den264, StandardCharsets.UTF_8.toString())

                params["dase"] = final
                return params
            }
        }
        queue3.add(stringRequest)
    }
}

