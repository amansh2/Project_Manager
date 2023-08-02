package com.example.projectmanager.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projectmanager.adapters.ColorClickListener
import com.example.projectmanager.adapters.LabelColorListItemsAdapter
import com.example.projectmanager.databinding.DialogListBinding

abstract class LabelColorListDialog(
    private var context: Context,
    private var list: ArrayList<String>,
    private val title: String = "",
    private val mSelectedColor: String = ""
):Dialog(context){
    private lateinit var binding:DialogListBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= DialogListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCanceledOnTouchOutside(true)
        setUpRecyclerView()
    }

    fun setUpRecyclerView(){

        binding.tvTitle.text=title
        binding.rvList.layoutManager=LinearLayoutManager(context)
        binding.rvList.adapter=LabelColorListItemsAdapter(context,list,mSelectedColor,object :ColorClickListener{
            override fun onClick(position: Int, color: String) {
                dismiss()
                onItemSelected(color)
            }
        })
    }

    abstract fun onItemSelected(color: String)
}