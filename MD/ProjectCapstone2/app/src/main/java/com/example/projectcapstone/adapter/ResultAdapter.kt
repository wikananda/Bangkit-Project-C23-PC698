package com.example.projectcapstone.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projectcapstone.R
import com.example.projectcapstone.ResultModel

class ResultAdapter (private val listResult: ArrayList<ResultModel>) : RecyclerView.Adapter<ResultAdapter.ListViewHolder>() {
    class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgPhotoResult: ImageView = itemView.findViewById(R.id.iv_item_photo)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.image_item, parent, false)
        return ListViewHolder(view)
    }

    override fun getItemCount(): Int = listResult.size

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val image = listResult[position]
        Glide.with(holder.itemView.context).load(listResult[position].photoResult).into(holder.imgPhotoResult)
    }
}