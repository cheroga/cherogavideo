package com.moisnostudio.jerutiapp.network

import com.moisnostudio.jerutiapp.model.IptvData
import retrofit2.http.GET
import retrofit2.http.Url

interface ApiService {
    // Al usar @Url, puedes pasarle una dirección completa de GitHub,
    // de un dominio .com o de un archivo .php
    @GET
    suspend fun getIptvData(@Url url: String): List<IptvData>
}
