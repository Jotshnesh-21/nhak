package com.sellinout.data.selinout

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton
import kotlin.contracts.ExperimentalContracts

@Module(includes = [SellInOutSources::class])
@InstallIn(SingletonComponent::class)
object SellInOutModule {
    @Provides
    @Singleton
    fun provideSellInOutApiService(retrofit: Retrofit):SellInOutNetworkService{
        return retrofit.create(SellInOutNetworkService::class.java)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class SellInOutSources {
    @ExperimentalContracts
    @Binds
    @Singleton
    abstract fun provideSellInOutRepo(impl: SellInOutImpl): SellInOutRepo
}