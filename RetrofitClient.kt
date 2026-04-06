package com.moisnostudio.jerutiapp.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // Esta base se usará solo si no se manda una URL completa,
    // pero como ahora usamos @Url, es flexible.
    private const val BASE_URL = "https://raw.githubusercontent.com/"

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
