package com.example.projectmanager.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projectmanager.Firebase.Firestore
import com.example.projectmanager.R
import com.example.projectmanager.adapters.TaskListAdapter
import com.example.projectmanager.databinding.ActivityTaskListBinding
import com.example.projectmanager.models.Board
import com.example.projectmanager.models.Card
import com.example.projectmanager.models.Task
import com.example.projectmanager.models.User
import com.example.projectmanager.utils.Constants
import com.example.projectmanager.utils.Constants.board

class TaskListActivity : BaseActivity() {
    private lateinit var mBoardDocumentID:String
    private lateinit var mBoardDetails:Board
    public var mAssignedMemberDetailList=ArrayList<User>()
    lateinit var binding: ActivityTaskListBinding
    private val addMembersActivityLauncher=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode==Activity.RESULT_OK ){
            showProgressDialog(getString(R.string.please_wait))
            Firestore().getBoardDetails(this@TaskListActivity,mBoardDocumentID)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityTaskListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if(intent.hasExtra(Constants.documentId)){
            mBoardDocumentID=intent.getStringExtra(Constants.documentId)!!
        }
        showProgressDialog(getString(R.string.please_wait))
        Firestore().getBoardDetails(this@TaskListActivity,mBoardDocumentID)

    }
    private fun setUpActionBar() {
        setSupportActionBar(binding.toolbarTaskListActivity)
        binding.toolbarTaskListActivity.setNavigationOnClickListener {
            onBackPressed()
        }
        binding.toolbarTaskListActivity.title=mBoardDetails.name
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    fun boardDetails(board: Board) {
        dismissDialog()
        mBoardDetails=board
        setUpActionBar()
        showProgressDialog(getString(R.string.please_wait))
        Firestore().getAssignedMembersListDetails(this,mBoardDetails.assignedTo)
    }

    fun addUpdateTaskListSuccess() {
        dismissDialog()
        showProgressDialog(getString(R.string.please_wait))
        Firestore().getBoardDetails(this,mBoardDetails.documentId)
    }

    fun createTaskList(taskListName:String){
        val task=Task(taskListName,Firestore().getCurrentUserId())
        mBoardDetails.taskList.add(0,task)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)

        showProgressDialog(getString(R.string.please_wait))
        Firestore().addUpdateTaskListInBoard(this@TaskListActivity,mBoardDetails)
    }

    fun updateTaskList(position: Int, listName: String, model: Task) {
        val task=Task(listName,model.createdBy)

        mBoardDetails.taskList[position]=task
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)

        showProgressDialog(getString(R.string.please_wait))
        Firestore().addUpdateTaskListInBoard(this@TaskListActivity,mBoardDetails)


    }

    fun deleteTaskList(position: Int){

        mBoardDetails.taskList.removeAt(position)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        showProgressDialog(resources.getString(R.string.please_wait))
        Firestore().addUpdateTaskListInBoard( this@TaskListActivity, mBoardDetails)
    }

    fun addCardToTaskList(position: Int, cardName: String) {

        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)
        val cardAssignedUserList:ArrayList<String> = ArrayList()
        cardAssignedUserList.add(Firestore().getCurrentUserId())

        val card= Card(cardName,Firestore().getCurrentUserId(),cardAssignedUserList)

        val cardList=mBoardDetails.taskList[position].cards
        cardList.add(card)

        mBoardDetails.taskList[position].cards=cardList

        showProgressDialog(getString(R.string.please_wait))
        Firestore().addUpdateTaskListInBoard(this, mBoardDetails)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_members,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_members->{
                val intent=Intent(this,MembersActivity::class.java)
                intent.putExtra(board,mBoardDetails)
                addMembersActivityLauncher.launch(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun cardDetails(taskListPosition: Int, cardPosition: Int) {
        val intent=Intent(this@TaskListActivity,CardDetailsActivity::class.java)
        intent.putExtra(Constants.BOARD_DETAIL, mBoardDetails)
        intent.putExtra(Constants.TASK_LIST_ITEM_POSITION, taskListPosition)
        intent.putExtra(Constants.CARD_LIST_ITEM_POSITION, cardPosition)
        intent.putExtra(Constants.BOARD_MEMBERS_LIST,mAssignedMemberDetailList as java.io.Serializable)
        addMembersActivityLauncher.launch(intent)
    }

    fun boardMembersDetailList(userslist: ArrayList<User>) {
        dismissDialog()
        mAssignedMemberDetailList=userslist
        val addTaskList = Task(resources.getString(R.string.add_list))
        mBoardDetails.taskList.add(addTaskList)

        binding.rvTaskList.layoutManager =
            LinearLayoutManager(this@TaskListActivity, LinearLayoutManager.HORIZONTAL, false)
        binding.rvTaskList.setHasFixedSize(true)


        val adapter = TaskListAdapter(this@TaskListActivity, mBoardDetails.taskList)
        binding.rvTaskList.adapter = adapter

    }

    fun updateCardInTaskList(position: Int, cards: ArrayList<Card>) {
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)
        mBoardDetails.taskList[position].cards=cards
        showProgressDialog(getString(R.string.please_wait))
        Firestore().addUpdateTaskListInBoard(this,mBoardDetails)
    }
}