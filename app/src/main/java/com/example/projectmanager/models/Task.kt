package com.example.projectmanager.models

import java.io.Serializable

data class Task(
    var title: String = "",
    val createdBy: String = "",
    var cards: ArrayList<Card> = ArrayList()
): Serializable
