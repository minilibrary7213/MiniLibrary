package uk.ac.tees.mad.minilibrary.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")

data class BookEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val subject: String,
    val fileName: String,
    val fileUrl: String,
    val uploadedAt: Long,
    val isSynced: Boolean
)