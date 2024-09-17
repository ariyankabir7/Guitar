package com.guitarbajao.gamecraft

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.mosquito.hincash.extrazz.videoplayyer
import com.pubscale.sdkone.offerwall.OfferWall
import com.pubscale.sdkone.offerwall.OfferWallConfig
import com.pubscale.sdkone.offerwall.models.OfferWallInitListener
import com.pubscale.sdkone.offerwall.models.OfferWallListener
import com.pubscale.sdkone.offerwall.models.Reward
import com.pubscale.sdkone.offerwall.models.errors.InitError
import com.guitarbajao.gamecraft.databinding.ActivityMoreBinding
import com.guitarbajao.gamecraft.extrazz.CustomAdLoader
import com.guitarbajao.gamecraft.extrazz.TinyDB
import com.guitarbajao.gamecraft.extrazz.Utils
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Base64

class MoreActivity : AppCompatActivity() {
    lateinit var binding: ActivityMoreBinding
    lateinit var adloder: CustomAdLoader

    init {
        System.loadLibrary("keys")
    }

    external fun Hatbc(): String

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMoreBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        adloder = CustomAdLoader(
            context = this,
            siteAdsUrl = com.guitarbajao.gamecraft.extrazz.Companions.siteAdsUrl,
            uniqueAppId = com.guitarbajao.gamecraft.extrazz.Companions.ADS_UNIQUE_APP_ID,
            onAdDismiss = {
                claimCoin()
            }
        )
        adloder.loadAd()
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
        binding.tvTotalAdsLimit.text=TinyDB.getString(this, "earning_ads_limit", "")
        binding.tvEarningdsLimit.text=TinyDB.getString(this, "earning_ad_limit", "")

        binding.tvBalance.text = TinyDB.getString(this, "balance", "")
        binding.cvPubscal.setOnClickListener {
            OfferWall.launch(this, offerWallListener)
        }
        binding.cvWatchNow.setOnClickListener {
            adloder.showAd()
        }
        binding.cvBalance.setOnClickListener {
            startActivity(Intent(this, WalletActivity::class.java))
        }
        binding.cvMore.setOnClickListener {
            startActivity(Intent(this, MoreOfferActivity::class.java))
        }
    }

private val offerWallListener = object : OfferWallListener {

    override fun onOfferWallShowed() {

    }

    override fun onOfferWallClosed() {

    }

    override fun onRewardClaimed(reward: Reward) {

    }

    override fun onFailed(message: String) {
        Toast.makeText(this@MoreActivity, "No Offers Available", Toast.LENGTH_SHORT).show()
    }
}

private fun claimCoin() {
    val deviceid: String =
        Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
    val time = System.currentTimeMillis()

    val url2 = "${com.guitarbajao.gamecraft.extrazz.Companions.siteUrl}earning_ads_point.php"
    val emails = Base64.getEncoder()
        .encodeToString(TinyDB.getString(this, "phone", "")!!.toByteArray())

    val queue1: RequestQueue = Volley.newRequestQueue(this)

    val stringRequest = object : StringRequest(Method.POST, url2, { response ->
        val ytes = Base64.getDecoder().decode(response)
        val res = String(ytes, Charsets.UTF_8)


        if (res.contains(",")) {
            val alldata = res.trim().split(",")
            Toast.makeText(this, alldata[0], Toast.LENGTH_SHORT).show()

            TinyDB.saveString(this, "earning_ad_limit", alldata[2])
            TinyDB.saveString(this, "balance", alldata[1])
            // binding.tvRemain.text = TinyDB.getString(this, "earning_ad_limit", "")
            binding.tvBalance.text = TinyDB.getString(this, "balance", "")
            binding.tvEarningdsLimit.text = TinyDB.getString(this, "earning_ad_limit", "")
        } else {
            Toast.makeText(this, res, Toast.LENGTH_SHORT).show()
        }
        adloder.loadAd()
    }, Response.ErrorListener { error ->
        Utils.dismissLoadingPopUp()
        Toast.makeText(this, "Internet Slow: $error", Toast.LENGTH_SHORT).show()
        // requireActivity().finish()
    }) {
        override fun getParams(): Map<String, String> {
            val params: MutableMap<String, String> = HashMap()

            val dbit32 = videoplayyer.encrypt(deviceid, Hatbc()).toString()
            val tbit32 = videoplayyer.encrypt(time.toString(), Hatbc()).toString()
            val email = videoplayyer.encrypt(emails.toString(), Hatbc()).toString()


            val den64 = Base64.getEncoder().encodeToString(dbit32.toByteArray())
            val ten64 = Base64.getEncoder().encodeToString(tbit32.toByteArray())
            val email64 = Base64.getEncoder().encodeToString(email.toByteArray())

            val encodemap: MutableMap<String, String> = HashMap()
            encodemap["deijvfijvmfhvfvhfbhbchbfybebd"] = den64
            encodemap["waofhfuisgdtdrefssfgsgsgdhddgd"] = ten64
            encodemap["fdvbdfbhbrthyjsafewwt5yt5"] = email64

            val jason = Json.encodeToString(encodemap)

            val den264 = Base64.getEncoder().encodeToString(jason.toByteArray())

            val final = URLEncoder.encode(den264, StandardCharsets.UTF_8.toString())

            params["dase"] = final

            val encodedAppID =
                Base64.getEncoder().encodeToString(
                    com.guitarbajao.gamecraft.extrazz.Companions.APP_ID.toString().toByteArray()
                )
            params["app_id"] = encodedAppID

            return params
        }
    }

    queue1.add(stringRequest)
}

override fun onResume() {
    super.onResume()
    binding.tvBalance.text = TinyDB.getString(this, "balance", "")
}
}