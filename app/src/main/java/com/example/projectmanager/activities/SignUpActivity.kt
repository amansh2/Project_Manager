package com.example.projectmanager.activities

import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.projectmanager.Firebase.Firestore
import com.example.projectmanager.R
import com.example.projectmanager.databinding.ActivitySignUpBinding
import com.example.projectmanager.models.User
import com.example.projectmanager.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch

class SignUpActivity : BaseActivity() {
    lateinit var binding2: ActivitySignUpBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding2 = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding2.root)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setUpActionBar()
        binding2.btnSignUp.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        val name = binding2.etName.text.toString().trim { it <= ' ' }
        val email = binding2.etEmail.text.toString().trim { it <= ' ' }
        val password = binding2.etPassword.text.toString().trim { it <= ' ' }

        if (isvalid(name, email, password)) {
            showProgressDialog(resources.getString(R.string.please_wait))
            lifecycleScope.launch {
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            val firebaseUser = it.result.user
                            val user = User(firebaseUser?.uid, name, firebaseUser?.email)
                            Firestore().registerUser(this@SignUpActivity,user)
                        } else {
                            showErrorSnackBar(it.exception?.message.toString())
                            dismissDialog()
                        }
                    }
            }
        }
    }

    private fun isvalid(name: String, email: String, password: String): Boolean {
        return when {
            TextUtils.isEmpty(name) -> {
                showErrorSnackBar("Name field is empty")
                false
            }
            TextUtils.isEmpty(email) -> {
                showErrorSnackBar("Email field is empty")
                false
            }
            TextUtils.isEmpty(password) -> {
                showErrorSnackBar("password field is empty")
                false
            }
            else -> {
                true
            }
        }
    }

    private fun setUpActionBar() {
        setSupportActionBar(binding2.toolbarSignUpActivity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding2.toolbarSignUpActivity.setNavigationOnClickListener { onBackPressed() }
    }

    fun UserRegisteredSuccess() {
        dismissDialog()
        Toast.makeText(
            this@SignUpActivity,
            "you have successfully registered",
            Toast.LENGTH_LONG
        ).show()
        FirebaseAuth.getInstance().signOut()
        finish()
    }
}