package com.example.projectmanager.Firebase

import android.app.Activity
import com.example.projectmanager.activities.*
import com.example.projectmanager.models.Board
import com.example.projectmanager.models.User
import com.example.projectmanager.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class Firestore : BaseActivity() {
    private val mfirestore = FirebaseFirestore.getInstance()

    fun registerUser(activity: SignUpActivity, user: User) {
        mfirestore.collection(Constants.users)
            .document(FirebaseAuth.getInstance().currentUser!!.uid)
            .set(user, SetOptions.merge())
            .addOnSuccessListener {
                activity.UserRegisteredSuccess()
            }.addOnFailureListener {
                activity.dismissDialog()
            }
    }

    fun LoadUserData(activity: Activity, readBoardsList: Boolean = false) {
        mfirestore.collection(Constants.users)
            .document(FirebaseAuth.getInstance().currentUser!!.uid)
            .get()
            .addOnSuccessListener {
                val loggedInUser = it.toObject(User::class.java)!!
                when (activity) {
                    is SignInActivity -> {
                        activity.UserSignInSuccess(loggedInUser)
                    }
                    is MainActivity -> {
                        activity.updateNavigationUserDetails(loggedInUser, readBoardsList)
                    }
                    is MyProfileActivity -> {
                        activity.setUserDataInUI(loggedInUser)
                    }
                }
            }.addOnFailureListener {
                when (activity) {
                    is SignInActivity -> {
                        activity.dismissDialog()
                    }
                    is MainActivity -> {
                        activity.dismissDialog()
                    }
                    is MyProfileActivity -> {
                        activity.dismissDialog()
                    }
                }
            }
    }

    fun updateUserDetails(activity:Activity, userHashMap: HashMap<String, Any>) {

        mfirestore.collection(Constants.users).document(getCurrentUserId())
            .update(userHashMap).addOnSuccessListener {
                when(activity){
                    is MyProfileActivity->activity.profileUpdateSuccess()
                    is MainActivity->activity.tokenUpdateSuccess()
                }

            }.addOnFailureListener {
                when(activity){
                    is MyProfileActivity->activity.dismissDialog()
                    is MainActivity->activity.dismissDialog()
                }
            }
    }

    fun createBoard(activity: CreateBoardActivity, board: Board) {
        mfirestore.collection(Constants.board).document().set(board, SetOptions.merge())
            .addOnSuccessListener {
                activity.boardCreatedSuccessfully()
            }.addOnCanceledListener {
                activity.dismissDialog()
                activity.showErrorSnackBar("Board Creation Failed")
            }
    }

    fun getBoardsList(activity: MainActivity) {
        mfirestore.collection(Constants.board)
            .whereArrayContains(Constants.assignedTo, getCurrentUserId()).get()
            .addOnSuccessListener {
                val boardsList: ArrayList<Board> = ArrayList()
                for (i in it.documents) {
                    val board = i.toObject(Board::class.java)!!
                    board.documentId = i.id
                    boardsList.add(board)
                }
                activity.populateBoardListToUI(boardsList)
            }.addOnFailureListener {
                activity.dismissDialog()
            }
    }

    fun getBoardDetails(activity: TaskListActivity, mBoardDocumentID: String) {

        mfirestore.collection(Constants.board).document(mBoardDocumentID).get()
            .addOnSuccessListener {
                val board = it.toObject(Board::class.java)
                board!!.documentId = mBoardDocumentID
                activity.boardDetails(board)
            }.addOnFailureListener {
                activity.dismissDialog()
                it.printStackTrace() // can give error
            }
    }

    fun addUpdateTaskListInBoard(activity: Activity, board: Board) {
        var taskListHashMap = HashMap<String, Any>()
        taskListHashMap[Constants.taskList] = board.taskList
        mfirestore.collection(Constants.board).document(board.documentId).update(taskListHashMap)
            .addOnSuccessListener {
                if (activity is TaskListActivity) {
                    activity.addUpdateTaskListSuccess()
                } else if(activity is CardDetailsActivity) {
                    activity.addUpdateTaskListSuccess()
                }
            }.addOnFailureListener {
                if (activity is TaskListActivity) {
                    activity.dismissDialog()
                } else if(activity is CardDetailsActivity){
                    activity.dismissDialog()
                }
                it.printStackTrace() //can give error
            }
    }

    fun getAssignedMembersListDetails(
        activity: Activity,
        assignedTo: ArrayList<String>
    ) {
        mfirestore.collection(Constants.users).whereIn(Constants.id, assignedTo).get()
            .addOnSuccessListener {
                var userslist = ArrayList<User>()
                for (i in it.documents) {
                    val user = i.toObject(User::class.java)!!
                    userslist.add(user)
                }
                if (activity is MembersActivity) {
                    activity.setUpMembersList(userslist)
                }else if(activity is TaskListActivity){
                    activity.boardMembersDetailList(userslist)
                }
            }.addOnFailureListener {
                it.printStackTrace() // can give error
                if (activity is MembersActivity) {
                    activity.dismissDialog()
                }else if(activity is TaskListActivity){
                    activity.dismissDialog()
                }
            }
    }

    fun geMemberDetails(membersActivity: MembersActivity, email: String) {
        mfirestore.collection(Constants.users).whereEqualTo(Constants.email, email).get()
            .addOnSuccessListener {
                if (it.documents.size > 0) {
                    val user = it.documents[0].toObject(User::class.java)
                    membersActivity.memberDetails(user)
                } else {
                    membersActivity.dismissDialog()
                    membersActivity.showErrorSnackBar("No such member found!")
                }
            }.addOnFailureListener {
            it.printStackTrace() // can give error
            membersActivity.dismissDialog()
        }
    }

    fun assignMembersToBoard(membersActivity: MembersActivity, mBoardDetails: Board, user: User) {

        val assignedToHashMap = HashMap<String, Any>()
        assignedToHashMap[Constants.assignedTo] = mBoardDetails.assignedTo

        mfirestore.collection(Constants.board).document(mBoardDetails.documentId)
            .update(assignedToHashMap).addOnSuccessListener {
                membersActivity.memberAssignedSuccess(user)
            }.addOnFailureListener {
                membersActivity.dismissDialog()
                it.printStackTrace() // can give error
            }
    }
}