package com.example.projectmanager.utils

import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.startActivity
import com.example.projectmanager.R
import com.example.projectmanager.activities.CreateBoardActivity
import com.example.projectmanager.activities.MyProfileActivity
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

object Constants {
    const val FCM_TOKEN="fcmToken"
    const val FCM_TOKEN_UPDATED="FCM_TOKEN_UPDATED"
    const val PROJECTMANAGER_PREFERENCES="PROJECTMANAGER_PREFERENCES"
    const val UNSELECT="unselect"
    const val SELECT="select"
    const val BOARD_MEMBERS_LIST="board_members_detail"
    const val BOARD_DETAIL="board_detail"
    const val email="email"
    const val users = "users"
    const val name = "name"
    const val image = "image"
    const val board="board"
    const val assignedTo="assignedTo"
    const val documentId="documentId"
    const val taskList="taskList"
    const val id="id"
    const val CARD_LIST_ITEM_POSITION="card_list_item_position"
    const val TASK_LIST_ITEM_POSITION="task_list_item_position"

    const val FCM_BASE_URL:String = "https://fcm.googleapis.com/fcm/send"
    const val FCM_AUTHORIZATION:String = "authorization"
    const val FCM_KEY:String = "key"
    const val FCM_SERVER_KEY:String = "AAAAwz8A6v0:APA91bHFwoKu6f7qkbS7562oB1uqf7YFqjs7o1pNJCs6zlZUsgBDEW9GjVfiVl4zjowj0FaEG4ciVUqTdWalwNBfs6m0W3qyltFLZ4dwHzTyz7-_GqkOU2GeTDARGGgl4C2yzUgPp7mx"

    const val FCM_KEY_TITLE:String = "title"
    const val FCM_KEY_MESSAGE:String = "message"
    const val FCM_KEY_DATA:String = "data"
    const val FCM_KEY_TO:String = "to"
    fun requestPermission(activity: Activity) {
        Dexter.withContext(activity)
            .withPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report!!.areAllPermissionsGranted()) {
                        when(activity){
                            is MyProfileActivity-> activity.galleryLauncher.launch("image/*")
                            is CreateBoardActivity->activity.galleryLauncher.launch("image/*")
                        }

                    } else {
                        Toast.makeText(
                            activity,
                            "You have denied Media permission. Please allow it is mandatory.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<PermissionRequest>?,
                    p1: PermissionToken?
                ) {
                    showRationalDialog(activity)
                }
            }).onSameThread().check()
    }
    private fun showRationalDialog(activity: Activity) {
        val builder = AlertDialog.Builder(activity)
        builder.setMessage("It Looks like you have turned off permissions required for this feature. It can be enabled under Application Settings")
        builder.setPositiveButton("Go To Settings") { _, _ ->
            try {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", activity.packageName, null)
                intent.data = uri
                startActivity(activity,intent,null)
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

}