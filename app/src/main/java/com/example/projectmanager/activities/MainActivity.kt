package com.example.projectmanager.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.projectmanager.Firebase.Firestore
import com.example.projectmanager.R
import com.example.projectmanager.adapters.BoardItemsAdapter
import com.example.projectmanager.adapters.itemClickListener
import com.example.projectmanager.databinding.ActivityMainBinding
import com.example.projectmanager.models.Board
import com.example.projectmanager.models.User
import com.example.projectmanager.utils.Constants
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceIdReceiver
import com.google.firebase.iid.internal.FirebaseInstanceIdInternal
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    private var muserName: String? = ""
    lateinit var binding: ActivityMainBinding
    private lateinit var mSharedPreferences: SharedPreferences
    private val profileActivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                try {
                    Firestore().LoadUserData(this@MainActivity)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    private val createBoardLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            try {
                Firestore().getBoardsList(this@MainActivity)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.navView.setNavigationItemSelectedListener(this)
        setUpActionBar()
        mSharedPreferences =
            this.getSharedPreferences(Constants.PROJECTMANAGER_PREFERENCES, Context.MODE_PRIVATE)
        val tokenUpdated=mSharedPreferences.getBoolean(Constants.FCM_TOKEN_UPDATED,false)
        if(tokenUpdated){
            showProgressDialog(getString(R.string.please_wait))
            Firestore().LoadUserData(this,true)
        }else{
            FirebaseMessaging.getInstance().token.addOnSuccessListener {
                updateFCMToaken(it)
            }
        }
        binding.appBar.fabCreateBoard.setOnClickListener {
            val intent = Intent(this, CreateBoardActivity::class.java)
            intent.putExtra(Constants.name, muserName)
            createBoardLauncher.launch(intent)
        }
    }

    private fun setUpActionBar() {
        setSupportActionBar(binding.appBar.toolbarMainActivity)
        binding.appBar.toolbarMainActivity.setNavigationIcon(R.drawable.baseline_menu_24)
        binding.appBar.toolbarMainActivity.setNavigationOnClickListener {
            toggleDrawer()
        }
    }

    private fun toggleDrawer() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_my_profile -> {
                profileActivityLauncher.launch(
                    Intent(
                        this@MainActivity,
                        MyProfileActivity::class.java
                    )
                )
            }

            R.id.nav_sign_out -> {
                FirebaseAuth.getInstance().signOut()
                mSharedPreferences.edit().clear().apply()
                val intent = Intent(this, IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            doubleBackToExit()
        }
    }

    fun updateNavigationUserDetails(user: User, readBoardsList: Boolean) {
        dismissDialog()
        val img: ImageView = findViewById(R.id.iv_user_image)
        lifecycleScope.launch {
            Glide.with(this@MainActivity).load(user.image).centerCrop()
                .placeholder(R.drawable.ic_user_place_holder)
                .into(img)
        }

        val text: TextView = findViewById(R.id.tv_username)
        text.text = user.name
        muserName = user.name
        if (readBoardsList) {
            showProgressDialog(resources.getString(R.string.please_wait))
            Firestore().getBoardsList(this@MainActivity)
        }
    }

    fun populateBoardListToUI(boardsList: ArrayList<Board>) {
        dismissDialog()
        if (boardsList.size > 0) {

            binding.appBar.mainContent.rvBoardsList.visibility = View.VISIBLE
            binding.appBar.mainContent.tvNoBoardsAvailable.visibility = View.GONE

            binding.appBar.mainContent.rvBoardsList.layoutManager =
                LinearLayoutManager(this@MainActivity)
            binding.appBar.mainContent.rvBoardsList.setHasFixedSize(true)

            val adapter =
                BoardItemsAdapter(boardsList, this@MainActivity, object : itemClickListener {
                    override fun onItemClicked(board: Board) {
                        val intent = Intent(this@MainActivity, TaskListActivity::class.java)
                        intent.putExtra(Constants.documentId, board.documentId)
                        startActivity(intent)
                    }

                })
            binding.appBar.mainContent.rvBoardsList.adapter = adapter
        } else {
            binding.appBar.mainContent.rvBoardsList.visibility = View.GONE
            binding.appBar.mainContent.tvNoBoardsAvailable.visibility = View.VISIBLE
        }
    }

    fun tokenUpdateSuccess() {
        dismissDialog()
        val editor=mSharedPreferences.edit()
        editor.putBoolean(Constants.FCM_TOKEN_UPDATED,true)
        editor.apply()
        showProgressDialog(getString(R.string.please_wait))
        Firestore().LoadUserData(this,true)
    }
    private fun updateFCMToaken(token:String){
        val userHashMap=HashMap<String,Any>()
        userHashMap[Constants.FCM_TOKEN]=token
        showProgressDialog(getString(R.string.please_wait))
        Firestore().updateUserDetails(this,userHashMap)
    }

}