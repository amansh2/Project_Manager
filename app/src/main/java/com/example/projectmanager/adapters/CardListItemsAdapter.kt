package com.example.projectmanager.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projectmanager.activities.TaskListActivity
import com.example.projectmanager.databinding.ItemCardBinding
import com.example.projectmanager.models.Card
import com.example.projectmanager.models.SelectedMembers
import org.checkerframework.checker.units.qual.s

class CardListItemsAdapter(
    private val context: Context,
    private var list: ArrayList<Card>,
    private val listener: itemClickListenerForCard
) : RecyclerView.Adapter<CardListItemsAdapter.CardListItemsViewHolder>() {

    class CardListItemsViewHolder(val binding: ItemCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardListItemsViewHolder {
        return CardListItemsViewHolder(
            ItemCardBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
        )
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: CardListItemsViewHolder, position: Int) {

        val model=list[position]
        if(holder is CardListItemsViewHolder){
            holder.binding.tvCardName.text=model.name
            holder.binding.root.setOnClickListener {
                listener.onItemClicked(position)
            }
            if(model.labelColor.isNotEmpty()){
                holder.binding.viewLabelColor.visibility=View.VISIBLE
                holder.binding.viewLabelColor.setBackgroundColor(Color.parseColor(model.labelColor))
            }else{
                holder.binding.viewLabelColor.visibility=View.GONE
            }
            if((context as TaskListActivity).mAssignedMemberDetailList.size>0) {
                val selectedMembersList = ArrayList<SelectedMembers>()

                for (i in context.mAssignedMemberDetailList.indices){
                    for(j in model.assignedTo){
                        if(context.mAssignedMemberDetailList[i].id==j){
                            val selectedMember=SelectedMembers(
                                context.mAssignedMemberDetailList[i].id!!,
                                context.mAssignedMemberDetailList[i].image!!
                            )
                            selectedMembersList.add(selectedMember)
                        }
                    }
                }

                if(selectedMembersList.size>0){

                    if(selectedMembersList.size==1 && selectedMembersList[0].id==model.createdBy){
                        holder.binding.rvCardSelectedMembersList.visibility=View.GONE
                    }else{
                        holder.binding.rvCardSelectedMembersList.visibility=View.VISIBLE
                        holder.binding.rvCardSelectedMembersList.layoutManager=GridLayoutManager(context,4)
                        holder.binding.rvCardSelectedMembersList.adapter=CardMemberListItemsAdapter(context,selectedMembersList,object :cardMemberItemClickListener{
                            override fun onClick() {
                                listener.onItemClicked(holder.adapterPosition)
                            }
                        },false)
                    }

                }else{
                    holder.binding.rvCardSelectedMembersList.visibility=View.GONE
                }

            }
        }

    }

}
interface itemClickListenerForCard{
    fun onItemClicked(cardPosition:Int)
}