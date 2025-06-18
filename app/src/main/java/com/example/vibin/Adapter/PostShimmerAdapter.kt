package com.example.vibin.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.vibin.R

class PostShimmerAdapter(private val itemCount: Int) :
    RecyclerView.Adapter<PostShimmerAdapter.ShimmerViewHolder>() {

    inner class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShimmerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post_shimmer, parent, false)
        return ShimmerViewHolder(view)
    }

    override fun onBindViewHolder(holder: ShimmerViewHolder, position: Int) {
        // No binding required as it's a static shimmer layout
    }

    override fun getItemCount(): Int = itemCount
}
