package com.powerlifting.assistant.di

import com.powerlifting.assistant.data.api.PowerliftingApi
import com.powerlifting.assistant.data.auth.FirebaseTokenProvider
import com.powerlifting.assistant.data.repo.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideProfileRepository(api: PowerliftingApi, tokenProvider: FirebaseTokenProvider): ProfileRepository =
        ProfileRepository(api, tokenProvider)

    @Provides
    @Singleton
    fun provideNutritionRepository(api: PowerliftingApi, tokenProvider: FirebaseTokenProvider): NutritionRepository =
        NutritionRepository(api, tokenProvider)

    @Provides
    @Singleton
    fun provideProgramRepository(api: PowerliftingApi, tokenProvider: FirebaseTokenProvider): ProgramRepository =
        ProgramRepository(api, tokenProvider)

    @Provides
    @Singleton
    fun provideWorkoutRepository(api: PowerliftingApi, tokenProvider: FirebaseTokenProvider): WorkoutRepository =
        WorkoutRepository(api, tokenProvider)

    @Provides
    @Singleton
    fun provideAchievementsRepository(api: PowerliftingApi, tokenProvider: FirebaseTokenProvider): AchievementsRepository =
        AchievementsRepository(api, tokenProvider)
}
