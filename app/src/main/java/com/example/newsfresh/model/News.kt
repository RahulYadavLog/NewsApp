package com.example.newsfresh.model

data class News(
    val title: String,
    val author: String,
    val url: String,
    val imageUrl: String,
    val time: String,
    val publisher: String,
    val desc: String
)