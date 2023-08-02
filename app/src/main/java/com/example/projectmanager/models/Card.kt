package com.example.projectmanager.models

import java.io.Serializable

data class Card(
    var name: String = "",
    val createdBy: String = "",
    val assignedTo: ArrayList<String> = ArrayList(),
    var labelColor: String = "",
    var dueDate: Long = 0
): Serializable