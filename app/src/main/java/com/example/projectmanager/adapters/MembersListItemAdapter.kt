package com.example.projectmanager.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projectmanager.R
import com.example.projectmanager.databinding.ItemMembersBinding
import com.example.projectmanager.models.User
import com.example.projectmanager.utils.Constants
import org.checkerframework.checker.units.qual.C

class MembersListItemAdapter(
    val context: Context,
    val list: ArrayList<User>,
    val listener: itemClickListenerForMembers
) : RecyclerView.Adapter<MembersListItemAdapter.MyViewHolder>() {
    class MyViewHolder(val binding: ItemMembersBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        MyViewHolder(ItemMembersBinding.inflate(LayoutInflater.from(context), parent, false))

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = list[position]

       holder.binding.apply {
           Glide
               .with(context)
               .load(model.image)
               .centerCrop()
               .placeholder(R.drawable.ic_user_place_holder)
               .into(ivMemberImage)

           tvMemberName.text = model.name
           tvMemberEmail.text = model.email
           if(model.selected){
               holder.binding.ivSelectedMember.visibility= View.VISIBLE
           }else{
               holder.binding.ivSelectedMember.visibility=View.GONE
           }

           holder.binding.root.setOnClickListener {
               if(model.selected){
                   listener.onItemClicked(position,model, Constants.UNSELECT)
               }else{
                   listener.onItemClicked(position,model,Constants.SELECT)
               }
           }

       }

    }
}

interface itemClickListenerForMembers {
    fun onItemClicked(position: Int, user: User, action: String)
}
