package com.realityexpander.dogs.view

import android.view.View

interface DogClickListener {
    fun onDogClicked(v: View)
    fun onDogNameClicked(v: View)
}

interface ClickListener {
    fun click(v: View)
}