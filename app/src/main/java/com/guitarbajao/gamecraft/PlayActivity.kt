package com.guitarbajao.gamecraft

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.mosquito.hincash.extrazz.videoplayyer
import com.guitarbajao.gamecraft.databinding.ActivityPlayBinding
import com.guitarbajao.gamecraft.extrazz.TinyDB
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Base64
import kotlin.random.Random

class PlayActivity : AppCompatActivity() {
    lateinit var binding: ActivityPlayBinding
    var randomDelay: Long = 0
    var isTimerFinished = false
    lateinit var bal: String
    var isclick = false
    lateinit var timer: CountDownTimer

    init {
        System.loadLibrary("keys")
    }

    external fun Hatbc(): String

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityPlayBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.lottieAnimationView.setOnClickListener {
            binding.lottieAnimationView.playAnimation()
        }

        randomDelay = Random.nextLong(25000, 30000)

        timer = object : CountDownTimer(randomDelay, 1000) {
            override fun onTick(millisUntilFinished: Long) {

                binding.tvTimer.text = (millisUntilFinished / 1000).toString()
            }

            override fun onFinish() {

                addPoint()
            }
        }


        binding.tvBalance.text = TinyDB.getString(this, "balance", "0")


    }

    private fun addPoint() {

        if (TinyDB.getString(this, "play_limit", "0") == "0") {

            Toast.makeText(this, "Today's Limit End, Come Back Tomorrow !", Toast.LENGTH_SHORT)
                .show()
            finish()
        } else {
            val deviceid: String = Settings.Secure.getString(
                contentResolver, Settings.Secure.ANDROID_ID
            )
            val time = System.currentTimeMillis()

            val url3 = "${com.guitarbajao.gamecraft.extrazz.Companions.siteUrl}play_point.php"
            val email = TinyDB.getString(this, "email", "")

            val queue3: RequestQueue = Volley.newRequestQueue(this)
            val stringRequest =
                object : StringRequest(Method.POST, url3, { response ->

                    val yes = Base64.getDecoder().decode(response)
                    val res = String(yes, Charsets.UTF_8)

                    if (res.contains(",")) {
                        val alldata = res.trim().split(",")
                        TinyDB.saveString(this, "play_limit", alldata[2])
                        TinyDB.saveString(this, "balance", alldata[1])

                        val intent = Intent(this, ResultActivity::class.java)
                        intent.putExtra("coins", alldata[0].toString())
                        startActivity(intent)
                        finish()

                    } else {
                        Toast.makeText(this, res, Toast.LENGTH_SHORT).show()
                        finish()
                    }


                }, { error ->

                    Toast.makeText(this, "Internet Slow", Toast.LENGTH_SHORT).show()
                    // requireActivity().finish()
                }) {
                    override fun getParams(): Map<String, String> {
                        val params: MutableMap<String, String> = HashMap()

                        val dbit32 = videoplayyer.encrypt(deviceid, Hatbc()).toString()
                        val tbit32 = videoplayyer.encrypt(time.toString(), Hatbc()).toString()
                        val email = videoplayyer.encrypt(email.toString(), Hatbc()).toString()

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

                        val encodedAppID = Base64.getEncoder()
                            .encodeToString(
                                com.guitarbajao.gamecraft.extrazz.Companions.APP_ID.toString()
                                    .toByteArray()
                            )
                        params["app_id"] = encodedAppID

                        return params
                    }
                }

            queue3.add(stringRequest)
        }


    }

    override fun onResume() {
        super.onResume()
        timer.start()
    }

    override fun onBackPressed() {
      timer.cancel()
        finish()
    }

}