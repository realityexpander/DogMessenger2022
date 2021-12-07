package com.realityexpander.dogs.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.realityexpander.dogs.model.DogBreed
import com.realityexpander.dogs.model.DogDatabase
import com.realityexpander.dogs.model.DogsApiService
import com.realityexpander.dogs.util.NotificationsHelper
import com.realityexpander.dogs.util.SharedPreferencesHelper
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch

class ListViewModel(application: Application): BaseViewModel(application) {

    private var prefHelper = SharedPreferencesHelper(getApplication())
    private var cacheRefreshInterval = 5 * 60 * 1_000

    private val dogsService = DogsApiService()
    private val disposable = CompositeDisposable()

    val dogs = MutableLiveData<List<DogBreed>>()
    val dogsLoadError = MutableLiveData<Boolean>()
    val loading = MutableLiveData<Boolean>()

    fun refresh() {
        checkCacheRefreshInterval()
        val lastUpdatedTime = prefHelper.getLastUpdatedTimeMs()

        if(lastUpdatedTime != 0L && System.currentTimeMillis() - lastUpdatedTime < cacheRefreshInterval) {
            fetchFromDatabase()
        } else {
            fetchFromRemote()
        }
    }

    private fun checkCacheRefreshInterval() {
        cacheRefreshInterval = prefHelper.getCacheRefreshIntervalSeconds() * 1_000
    }

    fun refreshBypassCache() {
        fetchFromRemote()
    }

    private fun fetchFromDatabase() {
        loading.value = true

        launch {
            val dogs = DogDatabase(getApplication()).dogDao().getAllDogs()
            dogsRetrieved(dogs)
            Toast.makeText(getApplication(), "Dogs retrieved from database", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchFromRemote() {
        loading.value = true

        disposable.add(
            dogsService.getDogs()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object: DisposableSingleObserver<List<DogBreed>>() {

                    override fun onSuccess(dogList: List<DogBreed>) {
                        storeDogsLocally(dogList)
                        Toast.makeText(getApplication(), "Dogs retrieved from endpoint", Toast.LENGTH_SHORT).show()
                        NotificationsHelper(getApplication()).createNofitication()
                    }

                    override fun onError(e: Throwable) {
                        dogsLoadError.value = true
                        loading.value = false
                        e.printStackTrace()
                    }

                })
        )
    }

    private fun dogsRetrieved(dogList: List<DogBreed>) {
        dogs.value = dogList
        dogsLoadError.value = false
        loading.value = false
    }

    private fun storeDogsLocally(list: List<DogBreed>) {
        launch {
            val dao = DogDatabase(getApplication()).dogDao()
            dao.deleteAllDogs()
//            val result = dao.insertAll(*list.toTypedArray()) // use spread operator
            val result = dao.insertAll2(list) // use plain list
            var i = 0
            while (i < list.size) {
                list[i].uuid = result[i].toInt()
                ++i
            }
            dogsRetrieved(list)
        }
        prefHelper.saveLastUpdatedTimeMs(System.currentTimeMillis())
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }
}

// Spread operator - takes a Array, passes to vararg with spread(*) operator
fun main() {
    val x = listOf("123","456","789").toTypedArray()

    fun varArgFun(vararg arg:String) {
        arg.forEach { println(it) }
    }

    varArgFun(*x)
}