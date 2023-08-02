package com.example.projectmanager.activities

import android.net.Uri
import android.os.Bundle
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.projectmanager.Firebase.Firestore
import com.example.projectmanager.R
import com.example.projectmanager.databinding.ActivityCreateBoardBinding
import com.example.projectmanager.models.Board
import com.example.projectmanager.utils.Constants
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch

class CreateBoardActivity : BaseActivity() {
    private var fboardLink: String? = ""
    private lateinit var binding: ActivityCreateBoardBinding
    private var galleryUri: Uri? = null
    private var muserName: String? = ""
    val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) {
        try {
            galleryUri = it
            lifecycleScope.launch {
                Glide.with(this@CreateBoardActivity).load(galleryUri).centerCrop()
                    .placeholder(R.drawable.ic_board_place_holder)
                    .into(binding.ivBoardImage)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpActionBar()
        binding.btnCreate.setOnClickListener {
            if (galleryUri != null) {
                uploadBoardImage()
            } else {
                showProgressDialog(getString(R.string.please_wait))
                createBoard()
            }
        }
        binding.ivBoardImage.setOnClickListener {
            Constants.requestPermission(this)
        }

        if (intent.hasExtra(Constants.name)) {
            muserName = intent.getStringExtra(Constants.name)
        }
    }

    private fun setUpActionBar() {
        setSupportActionBar(binding.toolbarCreateBoardActivity)
        binding.toolbarCreateBoardActivity.setNavigationIcon(R.drawable.baseline_menu_24)
        binding.toolbarCreateBoardActivity.setNavigationOnClickListener {
            onBackPressed()
        }
        binding.toolbarCreateBoardActivity.title = resources.getString(R.string.create_board_title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }


    private fun uploadBoardImage() {
        showProgressDialog(resources.getString(R.string.please_wait))
        val sRef = FirebaseStorage.getInstance().reference.child(
            "USER_IMAGE" + System.currentTimeMillis() + "." + getFileExtension(galleryUri)
        )
        sRef.putFile(galleryUri!!).addOnSuccessListener {
            it.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                fboardLink = uri.toString()
                createBoard()
            }
        }.addOnFailureListener {
            showErrorSnackBar(it.message.toString())
            dismissDialog()
        }
    }

    private fun createBoard() {
        val assignedUsersArrayList: ArrayList<String> = ArrayList()
        assignedUsersArrayList.add(getCurrentUserId())
        val board = Board(
            binding.etBoardName.text.toString(),
            fboardLink!!,
            muserName!!,
            assignedUsersArrayList
        )
        Firestore().createBoard(this@CreateBoardActivity, board)
    }

    private fun getFileExtension(uri: Uri?): String? {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri!!))
    }

    fun boardCreatedSuccessfully() {
        dismissDialog()
        setResult(RESULT_OK)
        Toast.makeText(this@CreateBoardActivity,"Board created successfully",Toast.LENGTH_SHORT).show()
        finish()
    }

}

