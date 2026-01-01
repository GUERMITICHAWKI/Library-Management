package com.example.gestionbibliotheque

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gestionbibliotheque.databinding.ActivityLivreBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LivreActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLivreBinding
    private lateinit var db: DatabaseHelper
    private lateinit var adapter: LivreAdapter

    private var ecrivainId: Int = -1
    private var selectedLivre: Livre? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLivreBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        db = DatabaseHelper(this)

        // Extras (ID obligatoire + nom optionnel). [web:1145]
        ecrivainId = intent.getIntExtra(DatabaseHelper.EXTRA_ECRIVAIN_ID, -1)
        val nomEcrivain = intent.getStringExtra(DatabaseHelper.EXTRA_ECRIVAIN_NOM) ?: ""

        if (ecrivainId == -1) {
            Toast.makeText(this, "Écrivain introuvable", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        title = if (nomEcrivain.isNotBlank()) "Livres de $nomEcrivain" else "Livres"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        adapter = LivreAdapter(mutableListOf()) { l ->
            selectedLivre = l
            binding.etTitre.setText(l.titre)
        }

        binding.rvLivres.layoutManager = LinearLayoutManager(this)
        binding.rvLivres.adapter = adapter

        binding.btnAjouter.setOnClickListener { addLivre() }
        binding.btnModifier.setOnClickListener { confirmUpdate() }
        binding.btnSupprimer.setOnClickListener { confirmDelete() }

        loadLivres()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_livre, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            // Flèche retour (Up)
            android.R.id.home -> {
                finish()
                true
            }

            // Icône flèche (menu)
            R.id.menu_retour_ecrivains -> {
                // Ramène à EcrivainActivity sans empiler plusieurs activités. [web:1477]
                val intent = Intent(this, EcrivainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }
                startActivity(intent)
                finish()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun addLivre() {
        val titre = binding.etTitre.text.toString().trim()

        if (titre.isEmpty()) {
            Toast.makeText(this, "Remplir le titre", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val id = withContext(Dispatchers.IO) { db.insertLivre(titre, ecrivainId) }
            if (id <= 0L) {
                Toast.makeText(this@LivreActivity, "Erreur ajout", Toast.LENGTH_SHORT).show()
                return@launch
            }
            clearForm()
            loadLivres()
            Toast.makeText(this@LivreActivity, "Livre ajouté", Toast.LENGTH_SHORT).show()
        }
    }

    private fun confirmUpdate() {
        val current = selectedLivre
        if (current == null) {
            Toast.makeText(this, "Sélectionne un livre", Toast.LENGTH_SHORT).show()
            return
        }

        val titre = binding.etTitre.text.toString().trim()
        if (titre.isEmpty()) {
            Toast.makeText(this, "Remplir le titre", Toast.LENGTH_SHORT).show()
            return
        }

        if (titre == current.titre) {
            Toast.makeText(this, "Aucune modification", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Confirmation")
            .setMessage("Voulez-vous modifier ce livre ?")
            .setPositiveButton("Oui") { dialog, _ ->
                lifecycleScope.launch {
                    val rows = withContext(Dispatchers.IO) { db.updateLivre(current.id, titre) }
                    if (rows <= 0) {
                        Toast.makeText(this@LivreActivity, "Erreur modification", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                    clearForm()
                    loadLivres()
                    Toast.makeText(this@LivreActivity, "Livre modifié", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Non") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun confirmDelete() {
        val current = selectedLivre
        if (current == null) {
            Toast.makeText(this, "Sélectionne un livre", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Confirmation")
            .setMessage("Voulez-vous supprimer ce livre ?")
            .setPositiveButton("Oui") { dialog, _ ->
                lifecycleScope.launch {
                    val rows = withContext(Dispatchers.IO) { db.deleteLivre(current.id) }
                    if (rows <= 0) {
                        Toast.makeText(this@LivreActivity, "Erreur suppression", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                    clearForm()
                    loadLivres()
                    Toast.makeText(this@LivreActivity, "Livre supprimé", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Non") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun loadLivres() {
        lifecycleScope.launch {
            val list = withContext(Dispatchers.IO) { db.getLivresByEcrivain(ecrivainId) }
            adapter.updateList(list)
        }
    }

    private fun clearForm() {
        selectedLivre = null
        adapter.clearSelection()
        binding.etTitre.text?.clear()
    }
}
