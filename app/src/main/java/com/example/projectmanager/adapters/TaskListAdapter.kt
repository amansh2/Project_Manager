package com.example.projectmanager.adapters

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projectmanager.activities.TaskListActivity
import com.example.projectmanager.databinding.ItemTaskBinding
import com.example.projectmanager.models.Card
import com.example.projectmanager.models.Task
import java.util.*
import kotlin.collections.ArrayList

open class TaskListAdapter(
    private val context: Context,
    private var list: ArrayList<Task>
) : RecyclerView.Adapter<TaskListAdapter.MyViewHolder>() {
    private var mPositionDraggedFrom = -1
    private var mPositionDraggedTo = -1
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val binding = ItemTaskBinding.inflate(LayoutInflater.from(context), parent, false)

        val layoutParams = LinearLayout.LayoutParams(
            (parent.width * 0.7).toInt(),
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins((15.toDp()).toPx(), 0, (40.toDp()).toPx(), 0)
        binding.root.layoutParams = layoutParams

        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, @SuppressLint("RecyclerView") position: Int) {

        val model = list[position]
        holder.binding.apply {
            if (position == list.size - 1) {
                tvAddTaskList.visibility = View.VISIBLE
                llTaskItem.visibility = View.GONE
            } else {
                tvAddTaskList.visibility = View.GONE
                llTaskItem.visibility = View.VISIBLE
            }

            tvAddTaskList.setOnClickListener {
                tvAddTaskList.visibility = View.GONE
                cvAddTaskListName.visibility = View.VISIBLE
            }
            ibCloseListName.setOnClickListener {
                tvAddTaskList.visibility = View.VISIBLE
                cvAddTaskListName.visibility = View.GONE
            }
            tvTaskListTitle.text = model.title
            ibDoneListName.setOnClickListener {
                val title = etTaskListName.text.toString()
                if (title.isNotEmpty()) {
                    if (context is TaskListActivity) {
                        context.createTaskList(title)
                    } else {
                        Toast.makeText(context, "Please enter list name", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            ibEditListName.setOnClickListener {
                llTitleView.visibility = View.GONE
                etEditTaskListName.setText(etTaskListName.text)
                cvEditTaskListName.visibility = View.VISIBLE
            }
            ibCloseEditableView.setOnClickListener {
                llTitleView.visibility = View.VISIBLE
                cvEditTaskListName.visibility = View.GONE
            }
            ibDoneEditListName.setOnClickListener {
                val name = etEditTaskListName.text.toString()
                if (name.isNotEmpty()) {
                    if (context is TaskListActivity) {
                        context.updateTaskList(position, name, list[position])
                    }
                }
            }
            ibDeleteList.setOnClickListener {
                alertDialogForDeleteList(position, model.title)
            }

            tvAddCard.setOnClickListener {
                tvAddCard.visibility = View.GONE
                cvAddCard.visibility = View.VISIBLE
            }
            ibCloseCardName.setOnClickListener {
                tvAddCard.visibility = View.VISIBLE
                cvAddCard.visibility = View.GONE
            }
            ibDoneCardName.setOnClickListener {
                val cardName = etCardName.text.toString()
                if (cardName.isNotEmpty()) {
                    if (context is TaskListActivity) {
                        context.addCardToTaskList(position, cardName)
                    } else {
                        Toast.makeText(context, "Please enter card name", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            rvCardList.layoutManager=LinearLayoutManager(context)
            rvCardList.setHasFixedSize(true)
            val adapter=CardListItemsAdapter(context,model.cards,object :itemClickListenerForCard{
                override fun onItemClicked(cardPosition:Int) {
                   if(context is TaskListActivity){
                       context.cardDetails(position,cardPosition)
                   }
                }
            })
            rvCardList.adapter=adapter
            val dividerItemDecoration=DividerItemDecoration(context,DividerItemDecoration.VERTICAL)
            rvCardList.addItemDecoration(dividerItemDecoration)

            val helper=ItemTouchHelper(object :ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN,0){
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    val draggedPosition=viewHolder.adapterPosition
                    val targetPostion=target.adapterPosition
                    if(mPositionDraggedFrom==-1) mPositionDraggedFrom=draggedPosition
                    mPositionDraggedTo=targetPostion

                    Collections.swap(list[position].cards,draggedPosition,targetPostion)
                    adapter.notifyItemMoved(draggedPosition,targetPostion)
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                }

                override fun clearView(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder
                ) {
                    super.clearView(recyclerView, viewHolder)

                    if(mPositionDraggedFrom!=-1 && mPositionDraggedTo!=-1 && mPositionDraggedFrom!=mPositionDraggedTo){
                        (context as TaskListActivity).updateCardInTaskList(
                            position,
                            list[position].cards
                        )
                    }
                    mPositionDraggedFrom=-1
                    mPositionDraggedTo=-1
                }

            })

            helper.attachToRecyclerView(rvCardList)
        }

    }

    private fun alertDialogForDeleteList(position: Int, title: String) {
        val dialog = AlertDialog.Builder(context)
        dialog.setTitle("Alert")
        dialog.setMessage("Are you sure you want to delete $title.")
        dialog.setIcon(android.R.drawable.ic_dialog_alert)
        dialog.setPositiveButton("Yes") { dialog, which ->
            if (context is TaskListActivity) {
                context.deleteTaskList(position)
            }
            dialog.dismiss()
        }
        dialog.setNegativeButton("No") { dialog, which ->
            dialog.dismiss()
        }
        dialog.setCancelable(false)
        dialog.show()
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private fun Int.toDp(): Int =
        (this / Resources.getSystem().displayMetrics.density).toInt()


    private fun Int.toPx(): Int =
        (this * Resources.getSystem().displayMetrics.density).toInt()

    class MyViewHolder(val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root)
}
