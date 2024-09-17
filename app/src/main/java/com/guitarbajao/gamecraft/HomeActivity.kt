package com.guitarbajao.gamecraft

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.card.MaterialCardView
import com.mosquito.hincash.extrazz.videoplayyer
import com.pubscale.sdkone.offerwall.OfferWall
import com.pubscale.sdkone.offerwall.OfferWallConfig
import com.pubscale.sdkone.offerwall.models.OfferWallInitListener
import com.pubscale.sdkone.offerwall.models.OfferWallListener
import com.pubscale.sdkone.offerwall.models.Reward
import com.pubscale.sdkone.offerwall.models.errors.InitError
import com.guitarbajao.gamecraft.extrazz.Companions
import com.guitarbajao.gamecraft.extrazz.TinyDB
import com.guitarbajao.gamecraft.extrazz.Utils
import com.guitarbajao.gamecraft.databinding.ActivityHomeBinding
import com.guitarbajao.gamecraft.extrazz.BannerAdLoader
import com.guitarbajao.gamecraft.extrazz.CustomAdLoader
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Base64

class HomeActivity : AppCompatActivity() {
    lateinit var binding: ActivityHomeBinding
    lateinit var adLoader: CustomAdLoader
    var isApiCallable = true

    init {
        System.loadLibrary("keys")
    }

    private external fun HatBc(): String

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityHomeBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.cvPlay.setOnClickListener {
            startActivity(Intent(this, PlayActivity::class.java))
        }
        binding.cvBalance.setOnClickListener {
            startActivity(Intent(this, WalletActivity::class.java))
        }
        val bannerAdLoader = BannerAdLoader(
            this,
            Companions.siteAdsUrl,
            Companions.ADS_UNIQUE_APP_ID,
            binding.vvShowVideo,
            binding.ivMuteUnmute
        )
        bannerAdLoader.loadBannerAd()

        binding.cvMoreCoins.setOnClickListener {
            startActivity(Intent(this, MoreActivity::class.java))
        }

        val offerWallConfig =
            OfferWallConfig.Builder(this, com.guitarbajao.gamecraft.extrazz.Companions.PUBSCALE_ID)
                .setUniqueId(
                    TinyDB.getString(
                        this,
                        "phone",
                        ""
                    )!! + "-" + com.guitarbajao.gamecraft.extrazz.Companions.APP_ID
                )
                .setFullscreenEnabled(true) //optional
                .build()
        OfferWall.init(offerWallConfig, object : OfferWallInitListener {
            override fun onInitSuccess() {
            }

            override fun onInitFailed(error: InitError) {
            }
        })

        binding.llGift.playAnimation()

        binding.llGift.addAnimatorUpdateListener {
            if (it.animatedFraction == 1f) {
                // Restart the animation
                binding.llGift.cancelAnimation()
                binding.llGift.playAnimation()
            }
        }


        binding.llGift.setOnClickListener {
            OfferWall.launch(this, offerWallListener)
        }

    }

    fun getUserValue() {
        Utils.showLoadingPopUp(this)
        val deviceid: String = Settings.Secure.getString(
            this.contentResolver, Settings.Secure.ANDROID_ID
        )
        val time = System.currentTimeMillis()

        val url2 = "${Companions.siteUrl}getuservalue.php"
        val email = TinyDB.getString(this, "email", "")

        val queue1: RequestQueue = Volley.newRequestQueue(this)
        val stringRequest =
            object : StringRequest(Method.POST, url2, { response ->
                Log.d("response", response)
                val ytes = Base64.getDecoder().decode(response)
                val res = String(ytes, Charsets.UTF_8)

                if (res.contains(",")) {
                    val alldata = res.trim().split(",")
                    TinyDB.saveString(this, "phone", alldata[0])
                    TinyDB.saveString(this, "maintenance", alldata[1])
                    TinyDB.saveString(this, "version", alldata[2])
                    TinyDB.saveString(this, "balance", alldata[3])
                    TinyDB.saveString(this, "earning_ad_limit", alldata[4])
                    TinyDB.saveString(this, "telegram_link", alldata[5])
                    TinyDB.saveString(this, "refer_code", alldata[6])
                    TinyDB.saveString(this, "sponsor_link", alldata[7])
                    TinyDB.saveString(this, "app_link", alldata[8])
                    TinyDB.saveString(this, "play_limit", alldata[9])
                    TinyDB.saveString(this, "earning_ads_limit", alldata[10])

                    //setBalanceText()
                    binding.tvBalance.text = TinyDB.getString(this, "balance", "")
                    binding.tvPlayLimit.text = TinyDB.getString(this, "play_limit", "")
                    if (alldata[2].toInt() > Companions.APP_VERSION) {
                        showUpdatePopup()
                    } else if (alldata[1] == "1") {
                        showMaintaincePopup()
                    }

                    Handler(Looper.getMainLooper()).postDelayed({
                        Utils.dismissLoadingPopUp()
                    }, 1000)

                } else {
                    Utils.dismissLoadingPopUp()

                    Toast.makeText(this, res, Toast.LENGTH_LONG).show()
                    finish()
                }


            }, Response.ErrorListener { error ->
                Utils.dismissLoadingPopUp()
                Toast.makeText(this, "Internet Slow", Toast.LENGTH_SHORT).show()
                finish()
            }) {
                override fun getParams(): Map<String, String> {
                    val params: MutableMap<String, String> = HashMap()

                    val dbit32 = videoplayyer.encrypt(deviceid, HatBc()).toString()
                    val tbit32 = videoplayyer.encrypt(time.toString(), HatBc()).toString()
                    val email = videoplayyer.encrypt(email.toString(), HatBc()).toString()

                    val den64 = Base64.getEncoder().encodeToString(dbit32.toByteArray())
                    val ten64 = Base64.getEncoder().encodeToString(tbit32.toByteArray())
                    val email64 = Base64.getEncoder().encodeToString(email.toByteArray())

                    val encodemap: MutableMap<String, String> = HashMap()
                    encodemap["deijvfijvmfhvfvhfbhbchbfybebd"] = den64
                    encodemap["waofhfuisgdtdrefssfgsgsgdhddgd"] = ten64

                    val jason = Json.encodeToString(encodemap)

                    val den264 = Base64.getEncoder().encodeToString(jason.toByteArray())

                    val final = URLEncoder.encode(den264, StandardCharsets.UTF_8.toString())

                    params["dase"] = final

                    val encodedAppID = Base64.getEncoder()
                        .encodeToString(Companions.APP_ID.toString().toByteArray())
                    params["app_id"] = encodedAppID

                    return params
                }
            }

        queue1.add(stringRequest)
    }

    private fun showMaintaincePopup() {
        AlertDialog.Builder(this, R.style.updateDialogTheme).setView(R.layout.popup_maintaince)
            .setCancelable(false).create().apply {
                show()
                findViewById<MaterialCardView>(R.id.cv_okay)?.setOnClickListener {
                    Utils.openUrl(
                        this@HomeActivity,
                        TinyDB.getString(this@HomeActivity, "telegram_link", "0")!!
                    )
                }
            }
    }

    private fun showUpdatePopup() {
        AlertDialog.Builder(this, R.style.updateDialogTheme).setView(R.layout.popup_newupdate)
            .setCancelable(false).create().apply {
                show()
                findViewById<MaterialCardView>(R.id.cv_okay)?.setOnClickListener {
                    Utils.openUrl(
                        this@HomeActivity,
                        TinyDB.getString(this@HomeActivity, "app_link", "")!!
                    )
                }
            }

    }

    override fun onResume() {
        super.onResume()
        getUserValue()
    }

    val offerWallListener = object : OfferWallListener {

        override fun onOfferWallShowed() {

        }

        override fun onOfferWallClosed() {
        }

        override fun onRewardClaimed(reward: Reward) {
        }

        override fun onFailed(message: String) {
            Toast.makeText(this@HomeActivity, "Offers Not Available", Toast.LENGTH_SHORT).show()

        }
    }
}