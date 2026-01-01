package com.example.gestionbibliotheque

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gestionbibliotheque.databinding.ActivityEcrivainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EcrivainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEcrivainBinding
    private lateinit var db: DatabaseHelper
    private lateinit var adapter: EcrivainAdapter

    private var selectedEcrivain: Ecrivain? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEcrivainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.app_name)


        binding.btnAll.setOnClickListener {
            binding.etSearch.text?.clear()
            loadAll()
        }

        title = "Écrivains"
        db = DatabaseHelper(this)

        adapter = EcrivainAdapter(mutableListOf()) { e ->
            selectedEcrivain = e
            binding.etNom.setText(e.nom)
            binding.etPrenom.setText(e.prenom)
            binding.etTel.setText(e.tel)
        }

        binding.rvEcrivains.layoutManager = LinearLayoutManager(this)
        binding.rvEcrivains.adapter = adapter

        binding.btnAjouter.setOnClickListener { addEcrivain() }
        binding.btnModifier.setOnClickListener { confirmUpdate() }
        binding.btnSupprimer.setOnClickListener { confirmDelete() }
        binding.btnSearch.setOnClickListener { doSearch() }

        loadAll()
    }

    private fun isValidTel(tel: String): Boolean {
        return tel.length == 8 && tel.all { it.isDigit() }
    }

    private fun addEcrivain() {
        val nom = binding.etNom.text.toString().trim()
        val prenom = binding.etPrenom.text.toString().trim()
        val tel = binding.etTel.text.toString().trim()

        if (nom.isEmpty() || prenom.isEmpty() || tel.isEmpty()) {
            Toast.makeText(this, "Remplir tous les champs", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isValidTel(tel)) {
            Toast.makeText(this, "Téléphone doit contenir 8 chiffres", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val id = withContext(Dispatchers.IO) { db.insert(nom, prenom, tel) }
            if (id == -1L) {
                Toast.makeText(this@EcrivainActivity, "Téléphone déjà existe", Toast.LENGTH_SHORT).show()
                return@launch
            }
            clearForm()
            loadAll()
            Toast.makeText(this@EcrivainActivity, "Écrivain ajouté", Toast.LENGTH_SHORT).show()
        }
    }

    private fun confirmUpdate() {
        val current = selectedEcrivain
        if (current == null) {
            Toast.makeText(this, "Sélectionne un écrivain", Toast.LENGTH_SHORT).show()
            return
        }

        val nom = binding.etNom.text.toString().trim()
        val prenom = binding.etPrenom.text.toString().trim()
        val tel = binding.etTel.text.toString().trim()

        if (nom.isEmpty() || prenom.isEmpty() || tel.isEmpty()) {
            Toast.makeText(this, "Remplir tous les champs", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isValidTel(tel)) {
            Toast.makeText(this, "Téléphone doit contenir 8 chiffres", Toast.LENGTH_SHORT).show()
            return
        }

        val changed = (nom != current.nom) || (prenom != current.prenom) || (tel != current.tel)
        if (!changed) {
            Toast.makeText(this, "Aucune modification", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Confirmation")
            .setMessage("Voulez-vous modifier cet écrivain ?")
            .setPositiveButton("Oui") { dialog, _ ->
                lifecycleScope.launch {
                    val rows = withContext(Dispatchers.IO) { db.update(current.id, nom, prenom, tel) }
                    if (rows <= 0) {
                        Toast.makeText(this@EcrivainActivity, "Erreur modification", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                    clearForm()
                    loadAll()
                    Toast.makeText(this@EcrivainActivity, "Écrivain modifié", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Non") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun confirmDelete() {
        val current = selectedEcrivain
        if (current == null) {
            Toast.makeText(this, "Sélectionne un écrivain dans la liste", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Confirmation")
            .setMessage("Voulez-vous supprimer cet écrivain ?")
            .setPositiveButton("Oui") { dialog, _ ->
                lifecycleScope.launch {
                    val rows = withContext(Dispatchers.IO) { db.delete(current.id) }
                    if (rows <= 0) {
                        Toast.makeText(this@EcrivainActivity, "Erreur suppression", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                    clearForm()
                    loadAll()
                    Toast.makeText(this@EcrivainActivity, "Écrivain supprimé", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Non") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun loadAll() {
        lifecycleScope.launch {
            val list = withContext(Dispatchers.IO) { db.getAll() }
            adapter.updateList(list)
        }
    }

    private fun doSearch() {
        val term = binding.etSearch.text.toString().trim()
        lifecycleScope.launch {
            val list = withContext(Dispatchers.IO) {
                if (term.isEmpty()) db.getAll() else db.search(term)
            }
            adapter.updateList(list)
        }
    }

    private fun clearForm() {
        selectedEcrivain = null
        adapter.clearSelection()
        binding.etNom.text?.clear()
        binding.etPrenom.text?.clear()
        binding.etTel.text?.clear()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_ecrivains -> {
                loadAll()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
