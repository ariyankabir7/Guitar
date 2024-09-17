package com.guitarbajao.gamecraft.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.guitarbajao.gamecraft.OCRTaskDetailsActivity
import com.guitarbajao.gamecraft.R
import com.guitarbajao.gamecraft.models.OCRJoiningTaskModel

class OCRJoiningAdapter(
    private val context: Context,
    private val jsonArray: ArrayList<OCRJoiningTaskModel>
) :
    RecyclerView.Adapter<OCRJoiningAdapter.PaymentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.item_ocr_joining_task, parent, false)
        return PaymentViewHolder(view)
    }

    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        val item = jsonArray[position]
        holder.paymentTitleTextView.text = item.offer_title
        Glide.with(context).load(item.offer_icon).into(holder.offer_logo)
        holder.itemView.setOnClickListener {
            OCRJoiningTaskModel.selectedOCRJoiningTaskModel = item
            val intent = Intent(context, OCRTaskDetailsActivity::class.java)
            context.startActivity(intent)
        }
        if (item.complete == 1) {
            holder.offer_check.visibility = View.VISIBLE
        } else {
            holder.offer_check.visibility = View.GONE
        }
    }


    override fun getItemCount(): Int {
        return jsonArray.size
    }

    fun updateTrans(updateTrans: ArrayList<OCRJoiningTaskModel>) {
        jsonArray.clear()
        jsonArray.addAll(updateTrans)
        notifyDataSetChanged()
    }


    inner class PaymentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val paymentTitleTextView: TextView = itemView.findViewById(R.id.tv_ocr_title)
        val offer_logo: ImageView = itemView.findViewById(R.id.iv_ocr_icon)
        val offer_check: ImageView = itemView.findViewById(R.id.iv_check)
    }

}
