package com.yucox.pillpulse.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.yucox.pillpulse.model.AlarmRealm
import com.yucox.pillpulse.model.PillRealm
import com.yucox.pillpulse.repository.FirebaseAlarmRepository
import com.yucox.pillpulse.repository.FirebaseUserRepository
import com.yucox.pillpulse.model.IFirebaseAlarmRepository
import com.yucox.pillpulse.model.IFirebaseUserRepository
import com.yucox.pillpulse.model.IFirebasePillRepository
import com.yucox.pillpulse.repository.FirebasePillRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DatabaseModule {
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
    fun provideFirebaseDatabase(): FirebaseDatabase {
        val database = FirebaseDatabase.getInstance()
        return database
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
        database: FirebaseDatabase,
        auth: FirebaseAuth
    ): IFirebaseUserRepository {
        val repository = FirebaseUserRepository(database, auth)
        return repository
    }

    @Singleton
    @Provides
    fun provideFirebaseAlarmRepository(
        database: FirebaseDatabase,
        auth: FirebaseAuth
    ): IFirebaseAlarmRepository {
        val repository = FirebaseAlarmRepository(database, auth)
        return repository
    }

    @Singleton
    @Provides
    fun provideFirebasePillRepository(
        database: FirebaseDatabase,
        mainUserMail: String
    ): IFirebasePillRepository {
        val repository = FirebasePillRepository(
            database,
            mainUserMail
        )
        return repository
    }

    @Singleton
    @Provides
    fun provideUserMail(auth: FirebaseAuth): String {
        return auth.currentUser?.email.toString()
    }
}