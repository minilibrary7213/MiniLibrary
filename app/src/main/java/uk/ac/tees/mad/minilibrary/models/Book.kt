package uk.ac.tees.mad.minilibrary.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Book(
    @SerialName("id")
    val id: String = "",
    @SerialName("user_id")
    val userId: String = "",
    @SerialName("title")
    val title: String = "",
    @SerialName("subject")
    val subject: String = "",
    @SerialName("file_name")
    val fileName: String = "",
    @SerialName("file_url")
    val fileUrl: String = "",
    @SerialName("uploaded_at")
    val uploadedAt: Long = System.currentTimeMillis()
)