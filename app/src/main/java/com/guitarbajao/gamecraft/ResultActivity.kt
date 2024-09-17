package com.guitarbajao.gamecraft

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.card.MaterialCardView
import com.mosquito.hincash.extrazz.videoplayyer
import com.guitarbajao.gamecraft.extrazz.Companions
import com.guitarbajao.gamecraft.extrazz.TinyDB
import com.guitarbajao.gamecraft.extrazz.Utils
import com.guitarbajao.gamecraft.databinding.ActivityResultBinding
import com.guitarbajao.gamecraft.extrazz.CustomAdLoader
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Base64

class ResultActivity : AppCompatActivity() {
    lateinit var binding: ActivityResultBinding
    var isApiCallable = true
    lateinit var adloder: CustomAdLoader

    init {
        System.loadLibrary("keys")
    }
    private external fun HatBc(): String
    override fun onCreate(savedInstanceState: Bundle?) {
        binding= ActivityResultBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val insertController = ViewCompat.getWindowInsetsController(v)
            insertController?.isAppearanceLightStatusBars = true
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        adloder = CustomAdLoader(
            context = this,
            siteAdsUrl = com.guitarbajao.gamecraft.extrazz.Companions.siteAdsUrl,
            uniqueAppId = com.guitarbajao.gamecraft.extrazz.Companions.ADS_UNIQUE_APP_ID,
            onAdDismiss = {
                finish()
            }
        )
        adloder.loadAd()
        val result = intent.getStringExtra("coins")
        val tvBalance = findViewById<TextView>(R.id.tv_balance)
        tvBalance.text = result

        findViewById<MaterialCardView>(R.id.cv_CLOSE).setOnClickListener {
          adloder.showAd()
        }
    }

    private fun addPoint(coin: String) {
        Utils.showLoadingPopUp(this)
        if (TinyDB.getString(this, "play_limit", "0") == "0") {
            Toast.makeText(this, "Today's Limit End, Come Back Tomorrow !", Toast.LENGTH_SHORT)
                .show()
            finish()
        } else {
            val deviceid: String = Settings.Secure.getString(
                contentResolver, Settings.Secure.ANDROID_ID
            )
            val time = System.currentTimeMillis()

            val url3 = "${Companions.siteUrl}play_point_mos.php"
            val email = TinyDB.getString(this, "email", "")

            val queue3: RequestQueue = Volley.newRequestQueue(this)
            val stringRequest =
                object : StringRequest(Method.POST, url3, { response ->

                    val yes = Base64.getDecoder().decode(response)
                    val res = String(yes, Charsets.UTF_8)

                    if (res.contains(",")) {
                        Utils.dismissLoadingPopUp()
                        val alldata = res.trim().split(",")
                        TinyDB.saveString(this, "play_limit", alldata[2])
                        TinyDB.saveString(this, "balance", alldata[1])
                        isApiCallable = true
                        Handler(Looper.getMainLooper()).postDelayed({
                            Utils.dismissLoadingPopUp()

                        }, 500)

                    } else {
                        Toast.makeText(this, res, Toast.LENGTH_SHORT).show()
                        finish()
                    }

                }, { error ->
                    Utils.dismissLoadingPopUp()
                    Toast.makeText(this, "Internet Slow", Toast.LENGTH_SHORT).show()
                    // requireActivity().finish()
                }) {
                    override fun getParams(): Map<String, String> {
                        val params: MutableMap<String, String> = HashMap()

                        val dbit32 = videoplayyer.encrypt(deviceid, HatBc()).toString()
                        val tbit32 = videoplayyer.encrypt(time.toString(), HatBc()).toString()
                        val email = videoplayyer.encrypt(coin.toString(), HatBc()).toString()

                        val den64 = Base64.getEncoder().encodeToString(dbit32.toByteArray())
                        val ten64 = Base64.getEncoder().encodeToString(tbit32.toByteArray())
                        val email64 = Base64.getEncoder().encodeToString(email.toByteArray())

                        val encodemap: MutableMap<String, String> = HashMap()
                        encodemap["deijvfijvmfhvfvhfbhbchbfybebd"] = den64
                        encodemap["waofhfuisgdtdrefssfgsgsgdhddgd"] = ten64
                        encodemap["rgerghthrthrthrthhrjryjrtjrtjherbyweyyy"] = email64

                        val jason = Json.encodeToString(encodemap)

                        val den264 = Base64.getEncoder().encodeToString(jason.toByteArray())

                        val final = URLEncoder.encode(den264, StandardCharsets.UTF_8.toString())

                        params["dase"] = final

                        val encodedAppID = Base64.getEncoder()
                            .encodeToString(
                                Companions.APP_ID.toString().toByteArray()
                            )
                        params["app_id"] = encodedAppID

                        return params
                    }
                }

            queue3.add(stringRequest)
        }


    }
}