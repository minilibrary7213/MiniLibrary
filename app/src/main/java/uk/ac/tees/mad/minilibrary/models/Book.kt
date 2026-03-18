package uk.ac.tees.mad.minilibrary.models

data class Book(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val subject: String = "",
    val fileName: String = "",
    val fileUrl: String = "",
    val uploadedAt: Long = System.currentTimeMillis()
)