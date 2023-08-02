package com.example.projectmanager.activities

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.projectmanager.Firebase.Firestore
import com.example.projectmanager.R
import com.example.projectmanager.databinding.ActivityMyProfileBinding
import com.example.projectmanager.models.User
import com.example.projectmanager.utils.Constants
import com.google.firebase.storage.FirebaseStorage
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.coroutines.launch

class MyProfileActivity : BaseActivity() {
    lateinit var binding: ActivityMyProfileBinding
    private var galleryUri: Uri? = null
    private var fProfileLink: String=""
    private lateinit var mUserDetails: User
    open val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) {
        try {
            galleryUri = it
            lifecycleScope.launch {
                Glide.with(this@MyProfileActivity).load(galleryUri).centerCrop()
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(binding.ivProfileUserImage)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupActionBar()
        Firestore().LoadUserData(this)
        binding.ivProfileUserImage.setOnClickListener {
            Constants.requestPermission(this)
        }
        binding.btnUpdate.setOnClickListener {
            if(galleryUri!=null){
                uploadUserImage()
            }else {
                showProgressDialog(resources.getString(R.string.please_wait))
                updateUserDetails()
            }
        }
    }



    private fun setupActionBar() {
        setSupportActionBar(binding.toolbarMyProfileActivity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarMyProfileActivity.setNavigationOnClickListener {
            onBackPressed()
        }
    }


    fun setUserDataInUI(user: User) {
        mUserDetails=user
        lifecycleScope.launch {
            Glide.with(this@MyProfileActivity).load(user.image).centerCrop()
                .placeholder(R.drawable.ic_user_place_holder)
                .into(binding.ivProfileUserImage)
        }
        binding.etEmail.setText(user.email)
        binding.etName.setText(user.name)
    }


    private fun uploadUserImage() {
        showProgressDialog(resources.getString(R.string.please_wait))
        val sRef = FirebaseStorage.getInstance().reference.child(
            "USER_IMAGE" + System.currentTimeMillis() + "." + getFileExtension(galleryUri)
        )
        sRef.putFile(galleryUri!!).addOnSuccessListener {
            it.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri->
                fProfileLink=uri.toString()
                updateUserDetails()
            }
        }.addOnFailureListener{
            showErrorSnackBar(it.message.toString())
            dismissDialog()
        }
    }

    private fun updateUserDetails() {
        val userHashMap=HashMap<String,Any>()
        if (fProfileLink.isNotEmpty() && fProfileLink != mUserDetails.image) {
            userHashMap[Constants.image] = fProfileLink
        }

        if (binding.etName.text.toString() != mUserDetails.name) {
            userHashMap[Constants.name] = binding.etName.text.toString()
        }
        Firestore().updateUserDetails(this,userHashMap)
    }

    private fun getFileExtension(uri: Uri?): String? {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri!!))
    }

    fun profileUpdateSuccess() {
        dismissDialog()
        Toast.makeText(this@MyProfileActivity, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
        setResult(Activity.RESULT_OK)
        finish()
    }
}