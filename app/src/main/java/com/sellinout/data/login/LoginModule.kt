package com.sellinout.data.login

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton
import kotlin.contracts.ExperimentalContracts

@Module(includes = [LoginSources::class])
@InstallIn(SingletonComponent::class)
object LoginModule {
    @Provides
    @Singleton
    fun provideLoginApiService(retrofit: Retrofit): LoginNetworkService {
        return retrofit.create(LoginNetworkService::class.java)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class LoginSources {
    @ExperimentalContracts
    @Binds
    @Singleton
    abstract fun provideLoginRepo(impl: LoginRepoImpl): LoginRepo
}