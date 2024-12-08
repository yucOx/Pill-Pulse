package com.yucox.pillpulse.di

import android.content.Context
import com.yucox.pillpulse.data.manager.AlarmManagerImpl
import com.yucox.pillpulse.domain.manager.IAlarmManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ManagerModule {

    @Provides
    @Singleton
    fun provideAlarmManager(
        @ApplicationContext context: Context,
    ): IAlarmManager {
        return AlarmManagerImpl(context)
    }
}