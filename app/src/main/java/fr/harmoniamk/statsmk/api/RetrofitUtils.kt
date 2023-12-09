package fr.harmoniamk.statsmk.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitUtils {

    fun <T> createRetrofit(apiClass: Class<T>, url: String): T {
        val builder = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val client = OkHttpClient.Builder().addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        return Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(MoshiConverterFactory.create(builder))
            .client(client.build())
            .build()
            .create(apiClass)
    }
}