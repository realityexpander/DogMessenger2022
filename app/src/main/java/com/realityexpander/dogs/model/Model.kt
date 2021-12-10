package com.realityexpander.dogs.model

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity  // defaults table name to the name of class
data class DogBreed(
    @ColumnInfo(name = "breed_id")  // For Database
    @SerializedName("id")    // For API
    val breedId: String?,

    @ColumnInfo(name = "dog_name")
    @SerializedName("name")
    val dogBreed: String?,

    @ColumnInfo(name = "life_span")
    @SerializedName("life_span")
    val lifeSpan: String?,

    @ColumnInfo(name = "breed_group")
    @SerializedName("breed_group")
    val breedGroup: String?,

    @ColumnInfo(name = "bred_for")
    @SerializedName("bred_for")
    val bredFor: String?,

    @SerializedName("temperament")
    val temperament: String?,

    @ColumnInfo(name = "dog_url")
    @SerializedName("url")
    val imageUrl: String?,

//    @PrimaryKey(autoGenerate = true) // not sure why we cant use this way
//    var uuid: Int = 0
) {
    @PrimaryKey(autoGenerate = true) // so dont need to use with constructor, but Room will create a key automatically
    var uuid: Int = 0
}

data class DogPalette(var color: Int)

data class SmsInfo(
    var to: String = "",
    var text: String = "",
    var subject: String = "",
    var imageUrl: String?,
    val imageUri: Uri?
)