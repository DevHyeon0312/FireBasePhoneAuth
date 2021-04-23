package com.devhyeon.phoneauth.di

import com.devhyeon.phoneauth.viewmodels.PhoneAuthViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val AppModule = module {
    viewModel {
        PhoneAuthViewModel()
    }
}