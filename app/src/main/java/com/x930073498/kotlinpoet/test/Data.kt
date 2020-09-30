package com.x930073498.kotlinpoet.test

import androidx.lifecycle.MutableLiveData

data class Data(val data: String)


class DataObserve {
    val parentLiveData = MutableLiveData<Data>()
}