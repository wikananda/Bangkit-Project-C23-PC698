package com.example.projectcapstone.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projectcapstone.ClothesModel
import com.example.projectcapstone.R
import com.example.projectcapstone.ui.tryon.TryonFragment

class ImageAdapter (private val listClothes: ArrayList<ClothesModel>) : RecyclerView.Adapter<ImageAdapter.ListViewHolder>() {
    class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgPhotoClothes: ImageView = itemView.findViewById(R.id.iv_item_photo)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.image_item, parent, false)
        return ListViewHolder(view)
    }

    override fun getItemCount(): Int = listClothes.size

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val image = listClothes[position]
        Glide.with(holder.itemView.context).load(listClothes[position].photoClothes).into(holder.imgPhotoClothes)

        holder.itemView.setOnClickListener{
            val toTryon = Intent(it.context, TryonFragment::class.java)
            toTryon.putExtra(TryonFragment.EXTRA_PHOTO, image.photoClothes.toString())
            it.context.startActivity(toTryon)
        }
    }



}