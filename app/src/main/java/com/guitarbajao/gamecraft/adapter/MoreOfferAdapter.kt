package com.guitarbajao.gamecraft.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.guitarbajao.gamecraft.R
import com.guitarbajao.gamecraft.extrazz.Utils
import com.guitarbajao.gamecraft.models.MoreTaskModel

class MoreOfferAdapter(
    private val context: Context,
    private val jsonArray: ArrayList<MoreTaskModel>
) :
    RecyclerView.Adapter<MoreOfferAdapter.PaymentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.item_more_offers, parent, false)
        return PaymentViewHolder(view)
    }

    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {

        val item = jsonArray[position]
        holder.paymentTitleTextView.text = item.offer_title
        holder.coins.text = item.offer_coin
        Glide.with(context).load(item.offer_icon).into(holder.offer_logo)
       holder.startBtn.setOnClickListener {
           Utils.openUrl(context, item.offer_link)
       }
    }


    override fun getItemCount(): Int {
        return jsonArray.size
    }

    fun updateTrans(updateTrans: ArrayList<MoreTaskModel>) {
        jsonArray.clear()
        jsonArray.addAll(updateTrans)
        notifyDataSetChanged()
    }


    inner class PaymentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val paymentTitleTextView: TextView = itemView.findViewById(R.id.tv_app_name)
        val coins: TextView = itemView.findViewById(R.id.tv_coins)
        val offer_logo: ImageView = itemView.findViewById(R.id.iv_giveaway)
        val startBtn: MaterialCardView = itemView.findViewById(R.id.cv_start)
    }
}
