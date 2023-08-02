package com.example.projectmanager.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.DatePicker
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.example.projectmanager.Firebase.Firestore
import com.example.projectmanager.R
import com.example.projectmanager.adapters.CardMemberListItemsAdapter
import com.example.projectmanager.adapters.cardMemberItemClickListener
import com.example.projectmanager.databinding.ActivityCardDetailsBinding
import com.example.projectmanager.dialogs.LabelColorListDialog
import com.example.projectmanager.dialogs.MembersListDialog
import com.example.projectmanager.models.Board
import com.example.projectmanager.models.SelectedMembers
import com.example.projectmanager.models.User
import com.example.projectmanager.utils.Constants
import com.example.projectmanager.utils.Constants.taskList
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CardDetailsActivity : BaseActivity() {
    private var mSelectedColor: String = ""
    private var anyChanges = false
    private lateinit var binding: ActivityCardDetailsBinding
    lateinit var mBoardDetil: Board
    private var cardListPosition: Int = -1
    private var taskListPosition: Int = -1
    private var mAssignedMembersList = ArrayList<User>()
    private var mSelectedDueDateInMillis:Long=0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCardDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getDataFromIntent()
        setUpActionBar()
        binding.btnUpdateCardDetails.setOnClickListener {
            if (binding.etNameCardDetails.text.toString().isNotEmpty()) {
                updateCardDetails(binding.etNameCardDetails.text.toString())
            } else {
                Toast.makeText(
                    this@CardDetailsActivity,
                    "please enter the card name",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        binding.tvSelectLabelColor.setOnClickListener {
            labelColorListDialog()
        }
        binding.tvSelectMembers.setOnClickListener {
            membersListDialog()
        }
        binding.tvSelectDueDate.setOnClickListener {
            showDatePicker()
        }
        setupSelectedMembersList()
    }

    private fun membersListDialog() {
        val cardAssignedMembers =
            mBoardDetil.taskList[taskListPosition].cards[cardListPosition].assignedTo
        if (cardAssignedMembers.size > 0) {
            for (i in mAssignedMembersList.indices) {
                for (j in cardAssignedMembers) {
                    if (mAssignedMembersList[i].id == j) {
                        mAssignedMembersList[i].selected = true
                    }
                }
            }
        } else {
            for (i in mAssignedMembersList) {
                i.selected = false
            }
        }
        val dialog = object : MembersListDialog(
            this,
            mAssignedMembersList,
            getString(R.string.str_select_member)
        ) {
            override fun onItemSelected(user: User, action: String) {
                if (action == Constants.SELECT) {
                    if (!mBoardDetil.taskList[taskListPosition].cards[cardListPosition].assignedTo.contains(
                            user.id
                        )
                    ) {
                        mBoardDetil.taskList[taskListPosition].cards[cardListPosition].assignedTo.add(
                            user.id!!
                        )
                    }
                } else {
                    mBoardDetil.taskList[taskListPosition].cards[cardListPosition].assignedTo.remove(
                        user.id
                    )

                    for (i in mAssignedMembersList.indices) {
                        if (mAssignedMembersList[i].id == user.id) {
                            mAssignedMembersList[i].selected = false
                        }
                    }
                }
                setupSelectedMembersList()
            }

        }
        dialog.show()
    }

    private fun labelColorListDialog() {
        val colorsList: ArrayList<String> = ArrayList()
        colorsList.add("#43C86F")
        colorsList.add("#0C90F1")
        colorsList.add("#F72400")
        colorsList.add("#7A8089")
        colorsList.add("#D57C1D")
        colorsList.add("#770000")
        colorsList.add("#0022F8")
        val dialog = object : LabelColorListDialog(
            this,
            colorsList,
            getString(R.string.str_select_label_color),
            mSelectedColor
        ) {
            override fun onItemSelected(color: String) {
                mSelectedColor = color
                binding.tvSelectLabelColor.text = ""
                binding.tvSelectLabelColor.setBackgroundColor(Color.parseColor(mSelectedColor))
            }

        }
        dialog.show()
    }

    private fun getDataFromIntent() {

        if (intent.hasExtra(Constants.BOARD_DETAIL)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                mBoardDetil =
                    intent.getSerializableExtra(Constants.BOARD_DETAIL, Board::class.java)!!
            } else {
                @Suppress("DEPRECATION")
                mBoardDetil = intent.getSerializableExtra(Constants.BOARD_DETAIL) as Board
            }
        }
        if (intent.hasExtra(Constants.CARD_LIST_ITEM_POSITION)) {
            cardListPosition = intent.getIntExtra(Constants.CARD_LIST_ITEM_POSITION, -1)
        }
        if (intent.hasExtra(Constants.TASK_LIST_ITEM_POSITION)) {
            taskListPosition = intent.getIntExtra(Constants.TASK_LIST_ITEM_POSITION, -1)
        }
        if (intent.hasExtra(Constants.BOARD_MEMBERS_LIST)) {
            @Suppress("DEPRECATION")
            mAssignedMembersList =
                intent.getSerializableExtra(Constants.BOARD_MEMBERS_LIST) as ArrayList<User>

        }
    }

    private fun setUpActionBar() {
        setSupportActionBar(binding.toolbarCardDetailsActivity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarCardDetailsActivity.setNavigationOnClickListener { onBackPressed() }
        binding.toolbarCardDetailsActivity.title =
            mBoardDetil.taskList[taskListPosition].cards[cardListPosition].name
        binding.etNameCardDetails.setText(mBoardDetil.taskList[taskListPosition].cards[cardListPosition].name)
        val color = mBoardDetil.taskList[taskListPosition].cards[cardListPosition].labelColor
        val sdf=SimpleDateFormat("dd/mm/yy",Locale.ENGLISH)
        val date=sdf.format(Date(mBoardDetil.taskList[taskListPosition].cards[cardListPosition].dueDate))
        binding.tvSelectDueDate.text=date

        if (color.isNotEmpty()) {
            mSelectedColor = color
            binding.tvSelectLabelColor.text = ""
            binding.tvSelectLabelColor.setBackgroundColor(Color.parseColor(mSelectedColor))
        }
    }

    fun updateCardDetails(name: String) {
        anyChanges = true
        mBoardDetil.taskList[taskListPosition].cards[cardListPosition].name = name
        mBoardDetil.taskList[taskListPosition].cards[cardListPosition].labelColor = mSelectedColor
        mBoardDetil.taskList[taskListPosition].cards[cardListPosition].dueDate=mSelectedDueDateInMillis
        val taskList = mBoardDetil.taskList
        taskList.removeAt(taskList.size - 1)
        showProgressDialog(getString(R.string.please_wait))
        Firestore().addUpdateTaskListInBoard(this@CardDetailsActivity, mBoardDetil)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_delete_card, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_delete_card -> showAlertDialog(mBoardDetil.taskList[taskListPosition].cards[cardListPosition].name)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (anyChanges) {
            setResult(Activity.RESULT_OK)
        }
        super.onBackPressed()
    }

    fun addUpdateTaskListSuccess() {
        dismissDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun deleteCard() {
        val cardList = mBoardDetil.taskList[taskListPosition].cards
        cardList.removeAt(cardListPosition)
        val taskList = mBoardDetil.taskList
        taskList.removeAt(taskList.size - 1)
        showProgressDialog(getString(R.string.please_wait))
        Firestore().addUpdateTaskListInBoard(this@CardDetailsActivity, mBoardDetil)
    }

    private fun showAlertDialog(cardName: String) {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle(getString(R.string.alert))
        alertDialog.setMessage(getString(R.string.confirmation_message_to_delete_card, cardName))
        alertDialog.setIcon(android.R.drawable.ic_dialog_alert)
        alertDialog.setCancelable(false)
        alertDialog.setPositiveButton("Yes") { dialogInterface, which ->
            dialogInterface.dismiss()
            anyChanges = true
            deleteCard()
        }
        alertDialog.setNegativeButton("No") { dialogInterface, which ->
            dialogInterface.dismiss()
        }
        alertDialog.show()
    }

    private fun setupSelectedMembersList() {
        val cardAssignedMembersList =
            mBoardDetil.taskList[taskListPosition].cards[cardListPosition].assignedTo
        val selectedMembersList = ArrayList<SelectedMembers>()

        for (i in mAssignedMembersList.indices) {
            for (j in cardAssignedMembersList) {
                if (mAssignedMembersList[i].id == j) {
                    val selectedMember = SelectedMembers(
                        mAssignedMembersList[i].id!!,
                        mAssignedMembersList[i].image!!
                    )

                    selectedMembersList.add(selectedMember)
                }
            }
        }

        if (selectedMembersList.size > 0) {
            selectedMembersList.add(SelectedMembers("", ""))
            binding.tvSelectMembers.visibility = View.GONE
            binding.rvSelectedMembersList.visibility = View.VISIBLE

            binding.rvSelectedMembersList.layoutManager = GridLayoutManager(this, 6)
            binding.rvSelectedMembersList.adapter = CardMemberListItemsAdapter(
                this,
                selectedMembersList,
                object : cardMemberItemClickListener {
                    override fun onClick() {
                        membersListDialog()
                    }
                })
        } else {
            binding.tvSelectMembers.visibility = View.VISIBLE
            binding.rvSelectedMembersList.visibility = View.GONE
        }
    }
    fun showDatePicker() {
        val c=Calendar.getInstance()
        val year=c.get(Calendar.YEAR)
        val month=c.get(Calendar.MONTH)
        val day=c.get(Calendar.DAY_OF_MONTH)

        val dpd=DatePickerDialog(
            this,
             { view,year,monthOfYear,dayOfMonth ->

                val sday=if(dayOfMonth<10) "0$dayOfMonth" else "$dayOfMonth"
                val smonth=if((monthOfYear+1)<10) "0$monthOfYear" else "$monthOfYear"
                val sdate="$sday/$smonth/$year"
                binding.tvSelectDueDate.text=sdate


                val sdf=SimpleDateFormat("dd/mm/yyyy", Locale.ENGLISH)
                val date=sdf.parse(sdate)

                mSelectedDueDateInMillis=date.time
            },year,
            month,
            day
        )
        dpd.show()
    }
}