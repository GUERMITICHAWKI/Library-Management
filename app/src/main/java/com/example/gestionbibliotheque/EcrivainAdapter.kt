package com.example.gestionbibliotheque

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EcrivainAdapter(
    private val items: MutableList<Ecrivain>,
    private val onSelect: (Ecrivain) -> Unit
) : RecyclerView.Adapter<EcrivainAdapter.VH>() {

    private var selectedId: Int? = null

    fun updateList(newList: List<Ecrivain>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    fun clearSelection() {
        selectedId = null
        notifyDataSetChanged()
    }

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNomPrenom: TextView = itemView.findViewById(R.id.tvNomPrenom)
        val tvTel: TextView = itemView.findViewById(R.id.tvTel)
        val btnLivres: Button = itemView.findViewById(R.id.btnLivres)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_ecrivain, parent, false)
        return VH(v)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val e = items[position]

        holder.tvNomPrenom.text = "${e.nom} ${e.prenom}"
        holder.tvTel.text = e.tel

        holder.itemView.isSelected = (selectedId == e.id)

        // clic simple = sélectionner
        holder.itemView.setOnClickListener {
            selectedId = e.id
            notifyDataSetChanged()
            onSelect(e)
        }

        // bouton = ouvrir Livres de cet écrivain [web:1152]
        holder.btnLivres.setOnClickListener {
            val ctx = holder.itemView.context
            val intent = Intent(ctx, LivreActivity::class.java).apply {
                putExtra(DatabaseHelper.EXTRA_ECRIVAIN_ID, e.id)
                putExtra(DatabaseHelper.EXTRA_ECRIVAIN_NOM, "${e.nom} ${e.prenom}") // nom affiché dans LivreActivity
            }
            ctx.startActivity(intent)
        }
    }
}
