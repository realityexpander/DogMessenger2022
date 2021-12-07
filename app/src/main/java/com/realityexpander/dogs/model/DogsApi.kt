package com.realityexpander.dogs.model

import io.reactivex.Single
import retrofit2.http.GET

interface DogsApi {
    @GET("DevTides/DogsApi/master/dogs.json")
    fun getDogs(): Single<List<DogBreed>>

//    @GET("DevTides/DogsApi/slave/dogs2.json")
//    fun getDogsEndpoint2(): Single<List<DogBreed>>
}