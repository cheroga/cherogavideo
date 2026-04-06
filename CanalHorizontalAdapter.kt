package com.moisnostudio.jerutiapp.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.moisnostudio.jerutiapp.R
import com.moisnostudio.jerutiapp.model.Sample

class CanalHorizontalAdapter(
    private val canales: List<Sample>,
    private val onCanalClick: (Sample) -> Unit
) : RecyclerView.Adapter<CanalHorizontalAdapter.ViewHolder>() {
    init {
        setHasStableIds(true) // Activa la persistencia de ID
    }

    // Usamos el hashCode de la URL o el nombre para que el ID sea único y constante
    override fun getItemId(position: Int): Long = canales[position].url.hashCode().toLong()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nombre: TextView = view.findViewById(R.id.txtNombreCanal)
        val icono: ImageView = view.findViewById(R.id.imgIcono)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_canal_horizontal, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val canal = canales[position]

        // ELIMINA ESTA LÍNEA: holder.itemView.id = View.generateViewId()
        // Generar IDs dinámicos en el Bind es lo que causa el salto, porque el ID cambia cada vez.

        holder.nombre.text = canal.name

        Glide.with(holder.itemView.context)
            .load(canal.icono)
            .placeholder(android.R.drawable.ic_menu_report_image)
            .into(holder.icono)

        holder.itemView.setOnClickListener { onCanalClick(canal) }

        holder.itemView.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                view.animate().scaleX(1.1f).scaleY(1.1f).setDuration(200).start()
                view.setBackgroundResource(R.drawable.bg_focus_canal)
                view.z = 10f
            } else {
                view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start()
                view.setBackgroundResource(android.R.color.transparent)
                view.z = 0f
            }
        }
    }

    override fun getItemCount() = canales.size
}
