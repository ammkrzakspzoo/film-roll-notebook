package com.filmroll.notebook.core.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AppViewModelFactory(private val createModel: () -> ViewModel) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val model = createModel()
        require(modelClass.isInstance(model))
        return modelClass.cast(model)
    }
}
