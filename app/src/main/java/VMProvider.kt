//package com.example.testprojectmusicplayer.di//package com.example.testprojectmusicplayer.di
//
//import androidx.lifecycle.ViewModel
//import com.example.testprojectmusicplayer.viewModel.HomeViewModel
//import dagger.Module
//import dagger.Provides
//import dagger.hilt.InstallIn
//import dagger.hilt.components.SingletonComponent
//import javax.inject.Provider
//import javax.inject.Singleton
//
//@Module
//@InstallIn(SingletonComponent::class)
//object ViewModelModule {
//
//    @Provides
//    @Singleton
//    fun provideViewModelFactory(
//        viewModelProviders: Map<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModel>>
//    ): ViewModelFactory {
//        return ViewModelFactory(viewModelProviders)
//    }
//
//    @Provides
//    @Singleton
//    fun provideViewModelProviders(
//        homeViewModelProvider: Provider<HomeViewModel>
//    ): Map<Class<HomeViewModel>, Provider<HomeViewModel>> {
//        return mapOf(
//            HomeViewModel::class.java to homeViewModelProvider
//            // Add other ViewModels here as needed
//        )
//    }
//}
