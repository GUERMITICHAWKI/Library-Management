package com.example.gestionbibliotheque

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class EcrivainAdapter(
    private val items: MutableList<Ecrivain>,
    private val onClick: (Ecrivain) -> Unit
) : RecyclerView.Adapter<EcrivainAdapter.VH>() {

    private var selectedId: Int? = null

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val cardRoot: CardView = v.findViewById(R.id.cardRoot)
        val tvNomPrenom: TextView = v.findViewById(R.id.tvNomPrenom)
        val tvTel: TextView = v.findViewById(R.id.tvTel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_ecrivain, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val e = items[position]
        holder.tvNomPrenom.text = "${e.nom} ${e.prenom}"
        holder.tvTel.text = e.tel

        val isSelected = (e.id == selectedId)
        holder.cardRoot.setCardBackgroundColor(
            if (isSelected) Color.parseColor("#2D6A4F") else Color.parseColor("#2A2A2A")
        )

        holder.itemView.setOnClickListener {
            val oldId = selectedId
            selectedId = e.id

            oldId?.let { oid ->
                val oldPos = items.indexOfFirst { it.id == oid }
                if (oldPos != -1) notifyItemChanged(oldPos)
            }
            notifyItemChanged(holder.adapterPosition)

            onClick(e)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newList: List<Ecrivain>) {
        items.clear()
        items.addAll(newList)
        if (selectedId != null && items.none { it.id == selectedId }) selectedId = null
        notifyDataSetChanged()
    }

    fun clearSelection() {
        val oldId = selectedId
        selectedId = null
        oldId?.let { oid ->
            val oldPos = items.indexOfFirst { it.id == oid }
            if (oldPos != -1) notifyItemChanged(oldPos)
        }
    }
}
