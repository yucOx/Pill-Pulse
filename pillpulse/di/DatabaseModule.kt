package com.yucox.pillpulse.di

import com.google.firebase.auth.FirebaseAuth
import com.yucox.pillpulse.data.locale.entity.AlarmRealm
import com.yucox.pillpulse.data.remote.api.ApiService
import com.yucox.pillpulse.data.remote.repository.PillRepositoryImpl
import com.yucox.pillpulse.data.remote.repository.FirebaseUserRepositoryImpl
import com.yucox.pillpulse.domain.repository.IFirebaseUserRepository
import com.yucox.pillpulse.domain.repository.IPillRepository
import com.yucox.pillpulse.presentation.viewmodel.PillViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DatabaseModule {
    @Singleton
    @Provides
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://85.105.201.173:9091/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Singleton
    @Provides
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Singleton
    @Provides
    fun provideRealm(): Realm {
        val config = RealmConfiguration.Builder(
            setOf(AlarmRealm::class)
        ).compactOnLaunch().build()
        return Realm.open(config)
    }

    @Singleton
    @Provides
    fun provideFirebaseAuth(): FirebaseAuth {
        val auth = FirebaseAuth.getInstance()
        return auth
    }

    @Singleton
    @Provides
    fun provideFirebaseUserRepository(
        auth: FirebaseAuth
    ): IFirebaseUserRepository {
        val repository = FirebaseUserRepositoryImpl(auth)
        return repository
    }

    @Singleton
    @Provides
    fun providePillRepository(apiService: ApiService): IPillRepository {
        val repository = PillRepositoryImpl(apiService)
        return repository
    }

    @Singleton
    @Provides
    fun providePillViewModel(
        authRepository: FirebaseUserRepositoryImpl,
        pillRepository: PillRepositoryImpl
    ): PillViewModel {
        val viewModel = PillViewModel(authRepository, pillRepository)
        return viewModel
    }
}