package com.example.projectmanager.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.projectmanager.Firebase.Firestore
import com.example.projectmanager.R
import com.example.projectmanager.databinding.ActivitySignInBinding
import com.example.projectmanager.models.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class SignInActivity : BaseActivity() {
    lateinit var binding: ActivitySignInBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setUpActionBar()
        binding.btnSignIn.setOnClickListener {
            signInuser()
        }
    }

    private fun signInuser() {
        val email = binding.etEmail.text.toString().trim { it <= ' ' }
        val password = binding.etPassword.text.toString().trim { it <= ' ' }
        if(isvalid(email,password)){
            showProgressDialog(resources.getString(R.string.please_wait))
            lifecycleScope.launch {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password).addOnCompleteListener {
                    dismissDialog()
                    if (it.isSuccessful) {
                        Firestore().LoadUserData(this@SignInActivity)
                    }else{
                        showErrorSnackBar(it.exception?.message.toString())
                    }
                }
            }
        }
    }

    private fun isvalid(email: String, password: String): Boolean {
        return when{
            TextUtils.isEmpty(email) -> {
                showErrorSnackBar("Email field is empty!")
                false
            }
            TextUtils.isEmpty(password) -> {
                showErrorSnackBar("password field is empty!")
                false
            }
            else -> {
                true
            }
        }
    }

    private fun setUpActionBar() {
        setSupportActionBar(binding.toolbarSignInActivity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarSignInActivity.setNavigationOnClickListener { onBackPressed() }
    }

    fun UserSignInSuccess(loggedInUser: User) {
        startActivity(Intent(this@SignInActivity,MainActivity::class.java))
        Toast.makeText(
            this@SignInActivity,
            "sign in successful!",
            Toast.LENGTH_LONG
        ).show()
        dismissDialog()
        finish()
    }
}