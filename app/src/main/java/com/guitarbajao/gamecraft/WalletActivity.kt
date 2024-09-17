package com.guitarbajao.gamecraft

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.InputType
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
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.mosquito.hincash.extrazz.videoplayyer
import com.guitarbajao.hincash.adapter.TotalHistoryAdapter
import com.guitarbajao.hincash.models.TotalTransctionModal
import com.guitarbajao.gamecraft.databinding.ActivityWalletBinding
import com.guitarbajao.gamecraft.extrazz.Companions
import com.guitarbajao.gamecraft.extrazz.TinyDB
import com.guitarbajao.gamecraft.extrazz.Utils
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Base64


class WalletActivity : AppCompatActivity() {
    lateinit var binding: ActivityWalletBinding
     var id=0
     var coin=0

    init {
        System.loadLibrary("keys")
    }

    external fun Hatbc(): String

    override fun onCreate(savedInstanceState: Bundle?) {
        binding= ActivityWalletBinding.inflate(layoutInflater)
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
binding.tvBalance.text = TinyDB.getString(this, "balance", "")

        binding.ivUpiCheck.visibility=View.VISIBLE
        binding.ivHindCheck.visibility=View.INVISIBLE
        binding.etUpi.hint = "Enter UPI  ID"
        val input = binding.etUpi.text.toString()
        if (input.matches("^(?=.*@)[a-zA-Z0-9@._-]+$".toRegex())) {
            // Set input type for UPI (text, email-like behavior)
            binding.etUpi.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS

        }

        binding.tvBalance.text = TinyDB.getString(this, "balance", "")
        binding.cvUpiCard.setOnClickListener {
            binding.ivUpiCheck.visibility=View.VISIBLE
            binding.ivHindCheck.visibility=View.INVISIBLE
            binding.etUpi.hint = "Enter UPI  ID"
            val input = binding.etUpi.text.toString()
            if (input.matches("^(?=.*@)[a-zA-Z0-9@._-]+$".toRegex())) {
                // Set input type for UPI (text, email-like behavior)
                binding.etUpi.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                id=1
                coin=1000
            }
            binding.ll.visibility=View.GONE
        }
        binding.ll.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.wallet.hindcash&hl=en_IN"))
            startActivity(browserIntent)
        }

// Set click listener for HC Wallet Card
        binding.cvHindcash.setOnClickListener {
            binding.ivUpiCheck.visibility=View.INVISIBLE
            binding.ivHindCheck.visibility=View.VISIBLE
            binding.etUpi.hint = "Enter Hindcash Id"
            binding.etUpi.inputType = InputType.TYPE_CLASS_PHONE
            id=14
            coin=500

            binding.ll.visibility=View.VISIBLE
        }
        binding.cvRedeem.setOnClickListener {
            if(TinyDB.getString(this, "balance", "")!!.toInt() >=coin) {
                redeemPoint(binding.etUpi.text.toString(), id)
            }else{
                Toast.makeText(this, "Insuffient Balance !", Toast.LENGTH_SHORT).show()
            }

        }


        getAllHistory()
    }

    fun getAllHistory() {

        binding.rvTotalHistory.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        val deviceId: String = Settings.Secure.getString(
            contentResolver, Settings.Secure.ANDROID_ID
        )
        val app = Base64.getEncoder()
            .encodeToString(Companions.APP_ID.toString().toByteArray())

        val emails = Base64.getEncoder()
            .encodeToString(deviceId.toByteArray())

        val url = "${Companions.siteUrl}get_redeem_history.php?email=$emails&app=${app}"
        val requestQueue: RequestQueue = Volley.newRequestQueue(this)

        val jsonArrayRequest =
            object : JsonArrayRequest(Method.GET, url, null, Response.Listener { response ->

                if (response.length() > 0) {
                    val totalHistory = ArrayList<TotalTransctionModal>()

                    for (i in 0 until response.length()) {
                        val dataObject = response.getJSONObject(i)

                        val title = dataObject.getString("title")
                        val amount = dataObject.getString("amount")
                        val date = dataObject.getString("date")
                        val status = dataObject.getString("status")
                        val historyRedeemModal = TotalTransctionModal(title, amount, date, status)
                        totalHistory.add(historyRedeemModal)
                    }
                    Utils.dismissLoadingPopUp()
                    val adapter = TotalHistoryAdapter(this, totalHistory)
                    binding.rvTotalHistory.adapter = adapter
                } else {
                    binding.rvTotalHistory.visibility = View.GONE
                }
            }, Response.ErrorListener { error ->
                Toast.makeText(this, "Internet Slow !", Toast.LENGTH_SHORT).show()
            }) {}

        requestQueue.add(jsonArrayRequest)
    }

    private fun redeemPoint(upi: String, id: Int) {

        val deviceid: String = Settings.Secure.getString(
            contentResolver, Settings.Secure.ANDROID_ID
        )
        val time = System.currentTimeMillis()

        val url3 = "${Companions.siteUrl}redeem_point_multi.php"
        val email = TinyDB.getString(this, "email", "")

        val queue3: RequestQueue = Volley.newRequestQueue(this)
        val stringRequest = object : StringRequest(Method.POST, url3, { response ->

            val yes = Base64.getDecoder().decode(response)
            val res = String(yes, Charsets.UTF_8)

            if (res.contains(",")) {
                Utils.dismissLoadingPopUp()
                val alldata = res.trim().split(",")

                TinyDB.saveString(this, "balance", alldata[1])
                Toast.makeText(this@WalletActivity, alldata[0], Toast.LENGTH_SHORT).show()
                Handler(Looper.getMainLooper()).postDelayed({
                    Utils.dismissLoadingPopUp()
                    finish()

                }, 1000)

            } else {
                Toast.makeText(this, res, Toast.LENGTH_LONG).show()
                finish()
            }

        }, { error ->
            Utils.dismissLoadingPopUp()
            Toast.makeText(this, "Internet Slow", Toast.LENGTH_SHORT).show()
            // requireActivity().finish()
        }) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()

                val dbit32 = videoplayyer.encrypt(deviceid, Hatbc()).toString()
                val tbit32 = videoplayyer.encrypt(time.toString(), Hatbc()).toString()
                val email = videoplayyer.encrypt(email.toString(), Hatbc()).toString()
                val upi32 = videoplayyer.encrypt(upi.toString(), Hatbc()).toString()
                val di32 = videoplayyer.encrypt(id.toString(), Hatbc()).toString()

                val den64 = Base64.getEncoder().encodeToString(dbit32.toByteArray())
                val ten64 = Base64.getEncoder().encodeToString(tbit32.toByteArray())
                val email64 = Base64.getEncoder().encodeToString(email.toByteArray())
                val upi64 = Base64.getEncoder().encodeToString(upi32.toByteArray())
                val id64 = Base64.getEncoder().encodeToString(di32.toByteArray())

                val encodemap: MutableMap<String, String> = HashMap()
                encodemap["deijvfijvmfhvfvhfbhbchbfybebd"] = den64
                encodemap["waofhfuisgdtdrefssfgsgsgdhddgd"] = ten64
                encodemap["fdvbdfbhbrthyjsafewwt5yt5"] = email64
                encodemap["defsdfefsefwefwefewfwefvfvdfbdbd"] = upi64
                encodemap["idfsdfefsefwefwefewfwefvfvdfbdbd"] = id64

                val jason = Json.encodeToString(encodemap)

                val den264 = Base64.getEncoder().encodeToString(jason.toByteArray())

                val final = URLEncoder.encode(den264, StandardCharsets.UTF_8.toString())

                params["dase"] = final

                val encodedAppID = Base64.getEncoder().encodeToString(
                    Companions.APP_ID.toString().toByteArray()
                )
                params["app_id"] = encodedAppID

                return params
            }
        }

        queue3.add(stringRequest)


    }
}