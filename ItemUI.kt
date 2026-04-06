package com.moisnostudio.jerutiapp.ui

import com.moisnostudio.jerutiapp.model.Sample

sealed class ItemUI {
    data class Titulo(val nombre: String) : ItemUI()
    data class Canal(val datos: Sample) : ItemUI()
}