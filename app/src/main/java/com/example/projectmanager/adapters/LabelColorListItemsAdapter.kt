package com.example.projectmanager.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.RecyclerView
import com.example.projectmanager.databinding.ItemLabelColorBinding

class LabelColorListItemsAdapter(
    private val context: Context,
    private var list: ArrayList<String>,
    private val mSelectedColor: String,
    private val listener: ColorClickListener
) : RecyclerView.Adapter<LabelColorListItemsAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            ItemLabelColorBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = list[position]

        if (holder is MyViewHolder) {

            holder.binding.viewMain.setBackgroundColor(Color.parseColor(item))

            if (item == mSelectedColor) {
                holder.binding.ivSelectedColor.visibility = View.VISIBLE
            } else {
                holder.binding.ivSelectedColor.visibility = View.GONE
            }

            holder.binding.root.setOnClickListener {
                listener.onClick(position,item)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class MyViewHolder(val binding: ItemLabelColorBinding) : RecyclerView.ViewHolder(binding.root)


}
interface ColorClickListener {
    fun onClick(position: Int, color: String)
}