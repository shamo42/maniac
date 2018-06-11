package com.kyoapps.maniac.viewmodel

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.kyoapps.maniac.room.dao.ThreadDao

class MainVMF(private val mainDS: MainDS) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainVM::class.java)) { return MainVM(mainDS) as T }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}