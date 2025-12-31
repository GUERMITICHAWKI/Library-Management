package com.example.gestionbibliotheque

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.example.gestionbibliotheque.databinding.ActivityLivreBinding

class LivreActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLivreBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLivreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = "Livres"
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_ecrivains -> {
                startActivity(Intent(this, EcrivainActivity::class.java))
                true
            }
            R.id.menu_livres -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
