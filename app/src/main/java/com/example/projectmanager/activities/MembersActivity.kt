package com.example.projectmanager.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projectmanager.Firebase.Firestore
import com.example.projectmanager.R
import com.example.projectmanager.adapters.MembersListItemAdapter
import com.example.projectmanager.adapters.itemClickListenerForMembers
import com.example.projectmanager.databinding.ActivityMembersBinding
import com.example.projectmanager.models.Board
import com.example.projectmanager.models.User
import com.example.projectmanager.utils.Constants
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

class MembersActivity : BaseActivity() {
    private var anyChanges: Boolean = false
    lateinit var binding: ActivityMembersBinding
    lateinit var mBoardDetails: Board
    private var mAssignedMembersList = ArrayList<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMembersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent.hasExtra(Constants.board)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                mBoardDetails = intent.getSerializableExtra(Constants.board, Board::class.java)!!
            } else {
                @Suppress("DEPRECATION")
                mBoardDetails = intent.getSerializableExtra(Constants.board)!! as Board
            }
        }
        setUpActionBar()
        showProgressDialog(getString(R.string.members))
        Firestore().getAssignedMembersListDetails(
            this@MembersActivity,
            mBoardDetails.assignedTo
        )
    }

    private fun setUpActionBar() {
        setSupportActionBar(binding.toolbarMembersActivity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarMembersActivity.setNavigationOnClickListener { onBackPressed() }
        binding.toolbarMembersActivity.title = getString(R.string.members)
    }

    fun setUpMembersList(list: ArrayList<User>) {
        mAssignedMembersList = list
        dismissDialog()
        binding.apply {
            rvMembersList.layoutManager = LinearLayoutManager(this@MembersActivity)
            rvMembersList.adapter = MembersListItemAdapter(
                this@MembersActivity,
                mAssignedMembersList,
                object : itemClickListenerForMembers {
                    override fun onItemClicked(position: Int, user: User, action: String) {
                        TODO("Not yet implemented")
                    }
                })
            rvMembersList.setHasFixedSize(true)
        }
    }

    fun searchMember() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_search_member)
        dialog.findViewById<TextView>(R.id.tv_add).setOnClickListener {
            val email =
                dialog.findViewById<TextInputEditText>(R.id.et_email_search_member).text.toString()
            if (email.isNotEmpty()) {
                dialog.dismiss()
                showProgressDialog(getString(R.string.please_wait))
                Firestore().geMemberDetails(this@MembersActivity, email)
            } else {
                showErrorSnackBar("please enter email")
            }
        }
        dialog.findViewById<TextView>(R.id.tv_cancel).setOnClickListener {
            dialog.dismiss()
        }
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    fun memberDetails(user: User?) {
        mBoardDetails.assignedTo.add(user!!.id!!)
        Firestore().assignMembersToBoard(this, mBoardDetails, user)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_member, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add_member -> searchMember()
        }
        return super.onOptionsItemSelected(item)
    }

    fun memberAssignedSuccess(user: User) {
        dismissDialog()
        mAssignedMembersList.add(user)
        anyChanges = true
        setUpMembersList(mAssignedMembersList)
        SendNotificationToUserAsyncTask(mBoardDetails.name,user.fcmToken!!).execute()
    }

    override fun onBackPressed() {
        if (anyChanges) {
            setResult(Activity.RESULT_OK)
        }
        super.onBackPressed()
    }

    @SuppressLint("StaticFieldLeak")
    private inner class SendNotificationToUserAsyncTask(val boardName: String, val token: String) :
        AsyncTask<Any, Void, String>() {


        override fun onPreExecute() {
            super.onPreExecute()
            showProgressDialog(getString(R.string.please_wait))
        }


        override fun doInBackground(vararg params: Any): String {
            var result: String

            var connection: HttpURLConnection? = null
            try {
                val url = URL(Constants.FCM_BASE_URL)
                connection = url.openConnection() as HttpURLConnection

                connection.doOutput = true
                connection.doInput = true

                connection.instanceFollowRedirects = false

                connection.requestMethod = "POST"

                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("charset", "utf-8")
                connection.setRequestProperty("Accept", "application/json")


                connection.setRequestProperty(
                    Constants.FCM_AUTHORIZATION, "${Constants.FCM_KEY}=${Constants.FCM_SERVER_KEY}"
                )

                connection.useCaches = false


                val wr = DataOutputStream(connection.outputStream)

                val jsonRequest = JSONObject()

                val dataObject = JSONObject()

                dataObject.put(Constants.FCM_KEY_TITLE, "Assigned to the Board $boardName")

                dataObject.put(
                    Constants.FCM_KEY_MESSAGE,
                    "You have been assigned to the new board by ${mAssignedMembersList[0].name}"
                )

                jsonRequest.put(Constants.FCM_KEY_DATA, dataObject)
                jsonRequest.put(Constants.FCM_KEY_TO, token)

                wr.writeBytes(jsonRequest.toString())
                wr.flush()
                wr.close()

                val httpResult: Int =
                    connection.responseCode

                if (httpResult == HttpURLConnection.HTTP_OK) {

                    val inputStream = connection.inputStream

                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val sb = StringBuilder()
                    var line: String?
                    try {

                        while (reader.readLine().also { line = it } != null) {
                            sb.append(line + "\n")
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } finally {
                        try {

                            inputStream.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                    result = sb.toString()
                } else {
                    result = connection.responseMessage
                }

            } catch (e: SocketTimeoutException) {
                result = "Connection Timeout"
            } catch (e: Exception) {
                result = "Error : " + e.message
            } finally {
                connection?.disconnect()
            }


            return result
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)

            dismissDialog()
            Log.e("JSON Response Result", result)
        }

    }
}
