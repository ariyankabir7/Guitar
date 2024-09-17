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
import com.guitarbajao.gamecraft.adapter.MoreOfferAdapter
import com.guitarbajao.gamecraft.databinding.ActivityMoreOfferBinding
import com.guitarbajao.gamecraft.extrazz.Companions
import com.guitarbajao.gamecraft.models.MoreTaskModel
import java.util.Base64

class MoreOfferActivity : AppCompatActivity() {
    lateinit var binding: ActivityMoreOfferBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding= ActivityMoreOfferBinding.inflate(layoutInflater)
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
        getOCROffers()
        binding.rvMore.layoutManager = LinearLayoutManager(this) // or GridLayoutManager, StaggeredGridLayoutManager, etc.

    }
    fun getOCROffers() {
        val deviceid: String = Base64.getEncoder(). encodeToString (
            Settings.Secure.getString(
                this.contentResolver, Settings.Secure.ANDROID_ID
            ).toByteArray())
        val appId = Base64.getEncoder()
            .encodeToString(Companions.APP_ID.toString().toByteArray())

        val url =
            "${Companions.siteUrl}get_more_offers_task.php?email=$deviceid&app_id=$appId"
        val requestQueue: RequestQueue = Volley.newRequestQueue(this)
        val jsonArrayRequest =
            object : JsonArrayRequest(Method.GET, url, null, Response.Listener { response ->
                val temp = ArrayList<MoreTaskModel>()

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
                        val MoreModel = MoreTaskModel(
                            how_to_link,
                            offer_description,
                            offer_id,
                            offer_link,
                            offer_title,
                            offer_coin,
                            offer_icon
                        )
                        temp.add(MoreModel)
                    }

                    val adapter = MoreOfferAdapter(this, temp)
                    binding.rvMore.adapter = adapter
                } else {
                   binding.tvNoMoreOffers.visibility= View.VISIBLE

                }
            }, Response.ErrorListener { _ ->
                Toast.makeText(this, "Internet Slow !", Toast.LENGTH_SHORT).show()
            }) {}

        requestQueue.add(jsonArrayRequest)
    }
}