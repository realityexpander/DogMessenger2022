package com.realityexpander.dogs.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.realityexpander.dogs.model.DogBreed
import com.realityexpander.dogs.model.DogDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetailViewModel(application: Application) : BaseViewModel(application) {

    val dogLiveData = MutableLiveData<DogBreed>()

    fun fetch(uuid: Int) {
        launch {
            val dog = withContext(Dispatchers.IO) {
                DogDatabase(getApplication()).dogDao().getDog(uuid)
            }
            dogLiveData.value = dog ?: DogBreed(
                    "unknown",
                    "unknown",
                    "unknown",
                    "unknown",
                    "unknown",
                    "unknown",
                    "unknown"
                )
        }
    }
}