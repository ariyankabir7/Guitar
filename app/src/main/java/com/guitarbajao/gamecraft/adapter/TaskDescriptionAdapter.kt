package com.guitarbajao.gamecraft.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.guitarbajao.gamecraft.R

class TaskDescriptionAdapter(
    private val context: Context,
    private val jsonArray: ArrayList<String>

) :
    RecyclerView.Adapter<TaskDescriptionAdapter.ViewHolder>() {


    private val requestQueue: RequestQueue = Volley.newRequestQueue(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.item_task_description, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {


        holder.tv_description.text = jsonArray[position]
        holder.tv_slno.text = (position+1).toString()


        }


        override fun getItemCount(): Int {
            return jsonArray.size
        }

        fun updateTrans(updateTrans: ArrayList<String>) {
            jsonArray.clear()
            jsonArray.addAll(updateTrans)
            notifyDataSetChanged()
        }



        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tv_description: TextView = itemView.findViewById(R.id.tv_description)
            val tv_slno: TextView = itemView.findViewById(R.id.tv_slno)

        }

    }
