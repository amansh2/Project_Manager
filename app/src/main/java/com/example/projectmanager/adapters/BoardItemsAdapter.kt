package com.example.projectmanager.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projectmanager.R
import com.example.projectmanager.databinding.ItemBoardBinding
import com.example.projectmanager.models.Board

class BoardItemsAdapter(val boardList:ArrayList<Board>,val context:Context,val listener:itemClickListener):RecyclerView.Adapter<BoardItemsAdapter.BoardsViewHolder>(){

    class BoardsViewHolder(val binding: ItemBoardBinding):RecyclerView.ViewHolder(binding.root) {

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoardsViewHolder {
        return BoardsViewHolder(ItemBoardBinding.inflate(LayoutInflater.from(context),parent,false))
    }

    override fun getItemCount()=boardList.size

    override fun onBindViewHolder(holder: BoardsViewHolder, position: Int) {
        val board=boardList[position]
        if(holder is BoardsViewHolder){
            Glide.with(context).load(board.image).centerCrop()
                .placeholder(R.drawable.ic_user_place_holder)
                .into(holder.binding.ivBoardImage)
        }
        holder.binding.tvName.text=board.name
        holder.binding.tvCreatedBy.text="created by "+board.createdBy
        holder.binding.root.setOnClickListener {
            listener.onItemClicked(board)
        }
    }
}
interface itemClickListener {
    fun onItemClicked(board:Board)
}