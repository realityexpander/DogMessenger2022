package com.realityexpander.dogs.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface DogDao {
    @Insert
    suspend fun insertAll(vararg dogs: DogBreed): List<Long> // use spread operator

    @Insert
    suspend fun insertAll2(dogs: List<DogBreed>): List<Long>  // use plain list

    @Query("SELECT * FROM dogbreed")
    suspend fun getAllDogs(): List<DogBreed>

    @Query("SELECT * FROM dogbreed WHERE uuid = :dogId")
    suspend fun getDog(dogId: Int): DogBreed?

    @Query("DELETE FROM dogbreed")
    suspend fun deleteAllDogs()
}