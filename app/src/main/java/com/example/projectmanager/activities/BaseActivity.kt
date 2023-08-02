package com.example.projectmanager.activities

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.projectmanager.R
import com.example.projectmanager.databinding.ActivityBaseBinding
import com.example.projectmanager.databinding.ActivitySignInBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.app
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

open class BaseActivity : AppCompatActivity() {
    lateinit var binding1: ActivityBaseBinding
    lateinit var mprogressDialog:Dialog
    private var doubleBackToExitPressedOnce = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding1= ActivityBaseBinding.inflate(layoutInflater)
        setContentView(binding1.root)

    }
    fun showProgressDialog(text:String){
        mprogressDialog=Dialog(this)
        mprogressDialog.setContentView(R.layout.dialog_progress)
        mprogressDialog.findViewById<TextView>(R.id.tv_progress_text).text=text
        mprogressDialog.setCanceledOnTouchOutside(false)
        mprogressDialog.show()
    }
    fun dismissDialog(){
        mprogressDialog.dismiss()
    }

    fun getCurrentUserId(): String {
       return FirebaseAuth.getInstance().currentUser!!.uid
    }
    fun doubleBackToExit(){
        if(doubleBackToExitPressedOnce){
            super.onBackPressed()
            return
        }
        Toast.makeText(this,"Press again to close app",Toast.LENGTH_SHORT).show()
        doubleBackToExitPressedOnce=true
        lifecycleScope.launch {
            delay(2000)
            doubleBackToExitPressedOnce=false
        }
    }

    fun showErrorSnackBar(message: String) {
        val snackBar =
            Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
        val snackBarView = snackBar.view
        snackBarView.setBackgroundColor(
            ContextCompat.getColor(
                this@BaseActivity,
                R.color.snackbar_error_color
            )
        )
        snackBar.show()
    }

}