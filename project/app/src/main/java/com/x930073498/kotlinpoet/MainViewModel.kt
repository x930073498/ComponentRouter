package com.x930073498.kotlinpoet

import android.app.Application
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle

class MainViewModel @ViewModelInject constructor(
    private val foo: Foo,
    @Assisted val savedStateHandle: SavedStateHandle,
    application: Application,
) : AndroidViewModel(application) {

    fun test() {
        foo.test()
    }
}