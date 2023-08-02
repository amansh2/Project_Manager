package com.example.projectmanager.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projectmanager.R
import com.example.projectmanager.adapters.MembersListItemAdapter
import com.example.projectmanager.adapters.itemClickListenerForMembers
import com.example.projectmanager.databinding.DialogListBinding
import com.example.projectmanager.models.User

abstract class MembersListDialog(
    private val context: Context,
    private var list: ArrayList<User>,
    private val title: String = ""
) : Dialog(context) {

    private var adapter: MembersListItemAdapter? = null
    private lateinit var binding:DialogListBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState ?: Bundle())

        binding=DialogListBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        setUpRecyclerView()
    }

    private fun setUpRecyclerView() {
        binding.tvTitle.text = title

        if (list.size > 0) {

            binding.rvList.layoutManager = LinearLayoutManager(context)
            adapter = MembersListItemAdapter(context, list,object :itemClickListenerForMembers{
                override fun onItemClicked(position: Int, user: User, action: String) {
                    dismiss()
                    onItemSelected(user,action)
                }
            })
            binding.rvList.adapter = adapter

        }
    }

    protected abstract fun onItemSelected(user: User, action:String)
}