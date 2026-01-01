package com.example.gestionbibliotheque

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LivreAdapter(
    private val items: MutableList<Livre>,
    private val onSelect: (Livre) -> Unit
) : RecyclerView.Adapter<LivreAdapter.VH>() {

    private var selectedId: Int? = null

    fun updateList(newList: List<Livre>) {
        items.clear()
        items.addAll(newList)
        selectedId = null
        notifyDataSetChanged()
    }

    fun clearSelection() {
        selectedId = null
        notifyDataSetChanged()
    }

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitre: TextView = itemView.findViewById(R.id.tvTitre)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_livre, parent, false)
        return VH(v)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val l = items[position]
        holder.tvTitre.text = l.titre

        // Option simple (sans design): on change l'alpha pour montrer sélection
        holder.itemView.alpha = if (selectedId == l.id) 0.6f else 1f

        holder.itemView.setOnClickListener {
            selectedId = l.id
            notifyDataSetChanged()
            onSelect(l)
        }
    }
}
