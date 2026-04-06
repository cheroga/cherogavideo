package com.moisnostudio.jerutiapp.ui

import com.moisnostudio.jerutiapp.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide // Necesitarás añadir Glide al build.gradle
import com.moisnostudio.jerutiapp.model.Sample

class CanalAdapter(
    private val items: List<ItemUI>,
    private val onCanalClick: (Sample) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_TITULO = 0
        private const val TYPE_CANAL = 1
    }

    override fun getItemViewType(position: Int) = when (items[position]) {
        is ItemUI.Titulo -> TYPE_TITULO
        is ItemUI.Canal -> TYPE_CANAL
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_TITULO) {
            // Crea un layout simple 'item_titulo.xml' con un TextView
            val view = inflater.inflate(R.layout.item_titulo, parent, false)
            TituloViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.item_canal, parent, false)
            CanalViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        if (holder is TituloViewHolder && item is ItemUI.Titulo) {
            holder.txtTitulo.text = item.nombre
        } else if (holder is CanalViewHolder && item is ItemUI.Canal) {
            holder.nombre.text = item.datos.name
            Glide.with(holder.itemView.context).load(item.datos.icono).into(holder.icono)
            holder.itemView.setOnClickListener { onCanalClick(item.datos) }
        }
    }

    override fun getItemCount() = items.size

    class TituloViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtTitulo: TextView = view.findViewById(R.id.txtTituloSeccion)
    }

    class CanalViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nombre: TextView = view.findViewById(R.id.txtNombreCanal)
        val icono: ImageView = view.findViewById(R.id.imgIcono)
    }
}
