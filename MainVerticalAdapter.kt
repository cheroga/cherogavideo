package com.moisnostudio.jerutiapp.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.moisnostudio.jerutiapp.R
import com.moisnostudio.jerutiapp.model.Sample

class MainVerticalAdapter(
    private val categorias: List<com.moisnostudio.jerutiapp.model.CategoriaConCanales>,
    private val onCanalClick: (Sample) -> Unit
) : RecyclerView.Adapter<MainVerticalAdapter.ViewHolder>() {

    init {
        // CLAVE 1: IDs estables para que el foco no salte al volver del video
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        // Usamos el hash del título para que cada fila tenga un ID único y constante
        return categorias[position].titulo.hashCode().toLong()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtTitulo: TextView = view.findViewById(R.id.txtTituloCategoria)
        val rvHorizontal: RecyclerView = view.findViewById(R.id.rvHorizontal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_fila_categoria, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = categorias[position]
        holder.txtTitulo.text = item.titulo

        // CLAVE 2: Configurar el LayoutManager con persistencia de hijos
        if (holder.rvHorizontal.layoutManager == null) {
            holder.rvHorizontal.layoutManager = LinearLayoutManager(
                holder.itemView.context,
                LinearLayoutManager.HORIZONTAL,
                false
            ).apply {
                recycleChildrenOnDetach = false // Mantiene a los hijos "vivos" en memoria
            }
        }

        // CLAVE 3: El adaptador horizontal también debe tener IDs estables (como vimos antes)
        holder.rvHorizontal.adapter = CanalHorizontalAdapter(item.canales, onCanalClick)

        holder.rvHorizontal.apply {
            isFocusable = false
            descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS
        }
    }

    override fun getItemCount() = categorias.size
}
