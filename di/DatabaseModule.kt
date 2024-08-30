package com.yucox.pillpulse.di

import com.google.firebase.auth.FirebaseAuth
import com.yucox.pillpulse.model.ApiService
import com.yucox.pillpulse.repository.PillRepositoryImpl
import com.yucox.pillpulse.model.AlarmRealm
import com.yucox.pillpulse.model.PillRealm
import com.yucox.pillpulse.repository.FirebaseUserRepository
import com.yucox.pillpulse.model.IFirebaseUserRepository
import com.yucox.pillpulse.model.PillRepository
import com.yucox.pillpulse.viewmodel.PillViewModel
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
            setOf(AlarmRealm::class, PillRealm::class)
        )
            .compactOnLaunch()
            .build()
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
        val repository = FirebaseUserRepository(auth)
        return repository
    }

    @Singleton
    @Provides
    fun providePillRepository(apiService: ApiService): PillRepository {
        val repository = PillRepositoryImpl(apiService)
        return repository
    }

    @Singleton
    @Provides
    fun providePillViewModel(
        auth: FirebaseAuth,
        pillRepository: PillRepositoryImpl
    ): PillViewModel {
        val viewModel = PillViewModel(auth, pillRepository)
        return viewModel
    }
}