package com.x930073498.component

import android.app.Application

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle

class MainViewModel constructor(

    val savedStateHandle: SavedStateHandle,
    application: Application,
) : AndroidViewModel(application) {


}