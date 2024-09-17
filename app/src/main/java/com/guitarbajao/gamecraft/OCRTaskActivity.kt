package com.guitarbajao.gamecraft

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.guitarbajao.gamecraft.adapter.OCRJoiningAdapter
import com.guitarbajao.gamecraft.databinding.ActivityOcrtaskBinding
import com.guitarbajao.gamecraft.extrazz.BannerAdLoader
import com.guitarbajao.gamecraft.extrazz.Companions
import com.guitarbajao.gamecraft.extrazz.Companions.ADS_UNIQUE_APP_ID
import com.guitarbajao.gamecraft.extrazz.Companions.siteAdsUrl
import com.guitarbajao.gamecraft.extrazz.CustomAdLoader
import com.guitarbajao.gamecraft.extrazz.InterstitialAdLoader
import com.guitarbajao.gamecraft.extrazz.TinyDB
import com.guitarbajao.gamecraft.models.OCRJoiningTaskModel
import com.pubscale.sdkone.offerwall.OfferWallConfig
import java.util.Base64

class OCRTaskActivity : AppCompatActivity() {
    lateinit var binding: ActivityOcrtaskBinding
    lateinit var adloder: CustomAdLoader


    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityOcrtaskBinding.inflate(layoutInflater)
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
        binding.rvOcrJoiningTask.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        if (TinyDB.getString(this, "completed", "0")!!.toInt() >= 1) {
            val ocrCoin = TinyDB.getString(this, "joining_ocr_task_total_amount_text", "")!!
            binding.cvSkip.visibility = View.VISIBLE
        }

        val interstitialAd = InterstitialAdLoader(
            this, Companions.siteAdsUrl,
            Companions.ADS_UNIQUE_APP_ID,
            onAdDismiss = {

                val intent=Intent(this,HomeActivity::class.java)
                startActivity(intent)
                finish()
            }
        )
        interstitialAd.loadInterstitalAd()

        binding.cvSkip.setOnClickListener {
            interstitialAd.showInterstitialAd()

        }


    }

    fun getOCROffers() {
        val deviceid: String = Base64.getEncoder().encodeToString(
            Settings.Secure.getString(
                this.contentResolver, Settings.Secure.ANDROID_ID
            ).toByteArray()
        )
        val appId = Base64.getEncoder()
            .encodeToString(Companions.APP_ID.toString().toByteArray())

        val url =
            "${Companions.siteUrl}get_ocr_joining_task.php?email=$deviceid&app_id=$appId"
        val requestQueue: RequestQueue = Volley.newRequestQueue(this)
        val jsonArrayRequest =
            object : JsonArrayRequest(Method.GET, url, null, Response.Listener { response ->
                val temp = ArrayList<OCRJoiningTaskModel>()

                if (response.length() > 0) {

                    for (i in 0 until response.length()) {
                        val dataObject = response.getJSONObject(i)

                        val how_to_link = dataObject.getString("how_to_link")
                        val offer_description = dataObject.getString("offer_description")
                        val offer_id = dataObject.getString("offer_id")
                        val offer_link = dataObject.getString("offer_link")
                        val offer_title = dataObject.getString("offer_title")
                        val offer_coin = dataObject.getString("offer_coin")
                        val offer_icon = dataObject.getString("offer_icon")
                        val complete = dataObject.getInt("complete")
                        val OCRJoiningTaskModel = OCRJoiningTaskModel(
                            how_to_link,
                            offer_description,
                            offer_id,
                            offer_link,
                            offer_title,
                            offer_coin,
                            offer_icon,
                            complete
                        )
                        temp.add(OCRJoiningTaskModel)
                    }

                    val adapter = OCRJoiningAdapter(this, temp)
                    binding.rvOcrJoiningTask.adapter = adapter
                } else {
                    binding.cvSkip.visibility = View.GONE
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }
            }, Response.ErrorListener { _ ->
                Toast.makeText(this, "Internet Slow !", Toast.LENGTH_SHORT).show()
            }) {}

        requestQueue.add(jsonArrayRequest)
    }

    override fun onResume() {
        super.onResume()
        getOCROffers()
        if (TinyDB.getString(this, "comp", "0")!!.toInt() >= 1) {
            binding.cvSkip.visibility = View.VISIBLE
        }

    }
}