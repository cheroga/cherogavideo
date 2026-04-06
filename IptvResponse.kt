package com.moisnostudio.jerutiapp.model

// Esta clase es la que espera el Adapter
data class CategoriaConCanales(
    val titulo: String,
    val canales: List<Sample>
)

data class IptvData(
    val name: String,
    val samples: List<Sample>
)

data class Sample(
    val name: String = "Canal",
    val url: String = "",
    val type: String? = null,
    val drm_license_uri: String? = null,
    val icono: String? = null
)
