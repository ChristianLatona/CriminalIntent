package com.bignerdranch.android.criminalintent

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.UUID
import java.util.Date

@Entity
data class Crime(@PrimaryKey val id: UUID = UUID.randomUUID(),
                 var title: String = "",
                 var date: Date = Date(),
                 var isSolved: Boolean = false,
                 var suspect: String = "",
                 var phone: String = ""
) : Serializable{
    val photoFileName
        get() = "IMG_$id.jpg"
}
