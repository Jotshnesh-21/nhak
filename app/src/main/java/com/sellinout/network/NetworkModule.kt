package com.sellinout.network

import android.app.Application
import android.util.Log

import com.google.gson.Gson
import com.pixplicity.easyprefs.library.Prefs
import com.sellinout.BuildConfig
import com.sellinout.utils.SharePrefsKey
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn

import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideRetrofit(
        client: OkHttpClient,
        gsonConverterFactory: GsonConverterFactory,
    ): Retrofit {
        val api_url = Prefs.getString(SharePrefsKey.STR_BASE_URL, BuildConfig.BASE_URL)
        return Retrofit.Builder()
            .baseUrl(api_url)
            .client(client)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(gsonConverterFactory)
            .build()
        /*return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(gsonConverterFactory)
            .build()*/
    }

    private var retrofit: Retrofit? = null

    fun getClient(baseUrl: String): Retrofit {

        Log.e(
            "BASEURL",
            "NEWWORK :> *&^*& " + baseUrl
        )
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(OkHttpClient.Builder()
                    .addInterceptor(TokenInterceptor())
                    .addInterceptor(provideLoggingInterceptor())
                    .connectTimeout(90, TimeUnit.SECONDS)
                    .readTimeout(90, TimeUnit.SECONDS)
                    .writeTimeout(90, TimeUnit.SECONDS)
                    .build())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }


    @Provides
    @Singleton
    fun provideGson(): Gson {
        //custom type adapter shall be added if required
        return Gson()
    }

    @Provides
    @Singleton
    fun provideGsonConverterFactor(gson: Gson): GsonConverterFactory {
        return GsonConverterFactory.create(gson)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        cache: Cache,
        interceptor: HttpLoggingInterceptor,
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(TokenInterceptor())
            .addInterceptor(interceptor)
            .cache(cache)
            .connectTimeout(90, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .writeTimeout(90, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideCache(context: Application): Cache {
        val cacheSize: Long = 10 * 1024 * 1024 //10 MB
        return Cache(context.cacheDir, cacheSize)
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        val interceptor = HttpLoggingInterceptor()
        if (BuildConfig.DEBUG) {
            interceptor.level = HttpLoggingInterceptor.Level.BODY
        } else {
            interceptor.level = HttpLoggingInterceptor.Level.NONE
        }
        return interceptor
    }

    /*    @Singleton
        @Provides
        fun provideSessionManager(
            authRepo: AuthRepo
        ): SessionManager {
            return SessionManager(authRepo)
        }*/

}

class TokenInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val isLogin = false
        if (isLogin) {
            request = request.newBuilder()
                .addHeader("Authorization", "Bearer " + "")
                .addHeader("language", "en")
                .build()

        } else {
            request = request.newBuilder()
                .addHeader("language", "en")
                .build()
        }
        return chain.proceed(request)
    }
}