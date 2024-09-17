package com.guitarbajao.gamecraft

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.card.MaterialCardView
import com.mosquito.hincash.extrazz.videoplayyer
import com.guitarbajao.gamecraft.databinding.ActivityMainBinding
import com.guitarbajao.gamecraft.extrazz.Companions
import com.guitarbajao.gamecraft.extrazz.TinyDB
import com.guitarbajao.gamecraft.extrazz.Utils
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Base64
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    var completed = "0"
    var pending = "0"
    init {
        System.loadLibrary("keys")
    }

    private external fun HatBc(): String

    private val progressBarDuration: Long = Random.nextLong(2500, 3000)
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
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

        binding.outerCardView.post {
            val finalWidth = binding.outerCardView.width
            animateProgress(binding.innerCardView, finalWidth, progressBarDuration)
        }

        getOCROffers()

    }

    private fun animateProgress(cardView: MaterialCardView, finalWidth: Int, duration: Long) {
        val startWidth = 0
        val layoutParams = cardView.layoutParams as ViewGroup.LayoutParams

        val handler = Handler(Looper.getMainLooper())
        val startTime = System.currentTimeMillis()

        handler.post(object : Runnable {
            override fun run() {
                val elapsedTime = System.currentTimeMillis() - startTime
                val progress = elapsedTime.toFloat() / duration
                layoutParams.width = (startWidth + (finalWidth * progress)).toInt()
                cardView.layoutParams = layoutParams

                if (progress < 1.0f) {
                    handler.postDelayed(this, 16)
                } else {
                    if (TinyDB.getString(this@MainActivity, "balance", "") != "") {
                        binding.outerCardView.visibility = View.GONE
                    }

                    getUserValue()
                }
            }
        })
    }



    fun getOCROffers() {
        val deviceid: String = Base64.getEncoder(). encodeToString (Settings.Secure.getString(
            this.contentResolver, Settings.Secure.ANDROID_ID
        ).toByteArray())
        val appId = Base64.getEncoder()
            .encodeToString(
                com.guitarbajao.gamecraft.extrazz.Companions.APP_ID.toString().toByteArray()
            )

        val url =
            "${Companions.siteUrl}get_ocr_joining_task_details.php?email=$deviceid&app_id=$appId"
        val requestQueue: RequestQueue = Volley.newRequestQueue(this)

        val strRequest =
            object : StringRequest(Method.GET, url, Response.Listener { response ->
                if (response.contains(",")) {
                    val alldata = response.trim().split(",")
                    TinyDB.saveString(this, "completed", alldata[0])
                    TinyDB.saveString(this, "pending", alldata[1])
                    completed = alldata[0]
                    pending = alldata[1]

                }

            }, Response.ErrorListener { error ->
                Toast.makeText(this, "Internet Slow !", Toast.LENGTH_SHORT).show()
            }) {}

        requestQueue.add(strRequest)
    }

    fun getUserValue() {
        //Utils.showLoadingPopUp(this)
        val deviceid: String = Settings.Secure.getString(
            this.contentResolver, Settings.Secure.ANDROID_ID
        )
        val time = System.currentTimeMillis()

        val url2 = "${Companions.siteUrl}signup.php"
        val email = TinyDB.getString(this, "email", "")

        val queue1: RequestQueue = Volley.newRequestQueue(this)
        val stringRequest =
            object : StringRequest(Method.POST, url2, { response ->
                Log.d("response", response)
                val ytes = Base64.getDecoder().decode(response)
                val res = String(ytes, Charsets.UTF_8)

                if (res.contains("Successfully")) {
                    val alldata = res
                    TinyDB.saveString(this, "Signup",res)
                    Utils.dismissLoadingPopUp()

                    startActivity(Intent(this@MainActivity, BonusActivity::class.java))
                    finish()
                } else if(res.contains("Already")) {
                    Utils.dismissLoadingPopUp()
                    startActivity(Intent(this@MainActivity, OCRTaskActivity::class.java))
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
}