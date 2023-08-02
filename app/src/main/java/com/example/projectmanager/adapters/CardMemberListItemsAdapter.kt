package com.example.projectmanager.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projectmanager.R
import com.example.projectmanager.databinding.ItemCardSelectedMemberBinding
import com.example.projectmanager.models.SelectedMembers

class CardMemberListItemsAdapter(
    val context: Context,
    val list: ArrayList<SelectedMembers>,
    val listener:cardMemberItemClickListener,
    var showAdd:Boolean=true
    ) : RecyclerView.Adapter<CardMemberListItemsAdapter.MyViewHolder>() {
    class MyViewHolder(val binding: ItemCardSelectedMemberBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            ItemCardSelectedMemberBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
        )
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val model = list[position]
        if (position == list.size - 1 && showAdd) {
            holder.binding.ivAddMember.visibility = View.VISIBLE
            holder.binding.ivSelectedMemberImage.visibility = View.GONE
        } else {
            holder.binding.ivAddMember.visibility = View.GONE
            holder.binding.ivSelectedMemberImage.visibility = View.VISIBLE

            Glide.with(context).load(model.image).centerCrop()
                .placeholder(R.drawable.ic_board_place_holder)
                .into(holder.binding.ivSelectedMemberImage)
        }

        holder.binding.root.setOnClickListener {
            listener.onClick()
        }
    }

}
interface cardMemberItemClickListener{
    fun onClick()
}