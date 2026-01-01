package com.example.gestionbibliotheque

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(ctx: Context) : SQLiteOpenHelper(ctx, "biblio.db", null, 3) {

    companion object {
        // ECRIVAIN
        private const val T = "ecrivain"
        private const val ID = "id"
        private const val NOM = "nom"
        private const val PRENOM = "prenom"
        private const val TEL = "tel"
        const val EXTRA_ECRIVAIN_NOM = "EXTRA_ECRIVAIN_NOM"


        // LIVRE
        private const val TL = "livre"
        private const val L_ID = "id"
        private const val L_TITRE = "titre"
        private const val L_ECRIVAIN_ID = "ecrivain_id"

        // Intent key (utile)
        const val EXTRA_ECRIVAIN_ID = "EXTRA_ECRIVAIN_ID"
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        // Active les contraintes FK (API 16+), sinon ON DELETE CASCADE peut ne pas marcher. [web:1099]
        db.setForeignKeyConstraintsEnabled(true) // [web:1099]
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $T(
                $ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $NOM TEXT NOT NULL,
                $PRENOM TEXT NOT NULL,
                $TEL TEXT NOT NULL UNIQUE
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE $TL(
                $L_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $L_TITRE TEXT NOT NULL,
                $L_ECRIVAIN_ID INTEGER NOT NULL,
                FOREIGN KEY($L_ECRIVAIN_ID) REFERENCES $T($ID) ON DELETE CASCADE
            )
            """.trimIndent()
        ) // FK + CASCADE. [web:1101]
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Simple pour projet scolaire : drop & recreate
        db.execSQL("DROP TABLE IF EXISTS $TL")
        db.execSQL("DROP TABLE IF EXISTS $T")
        onCreate(db)
    }

    // ---------------- ECRIVAIN (tes fonctions, inchangées) ----------------
    fun insert(nom: String, prenom: String, tel: String): Long {
        val cv = ContentValues().apply {
            put(NOM, nom); put(PRENOM, prenom); put(TEL, tel)
        }
        return writableDatabase.insert(T, null, cv)
    }

    fun update(id: Int, nom: String, prenom: String, tel: String): Int {
        val cv = ContentValues().apply {
            put(NOM, nom); put(PRENOM, prenom); put(TEL, tel)
        }
        return writableDatabase.update(T, cv, "$ID=?", arrayOf(id.toString()))
    }

    fun delete(id: Int): Int =
        writableDatabase.delete(T, "$ID=?", arrayOf(id.toString()))

    fun getAll(): List<Ecrivain> {
        val list = mutableListOf<Ecrivain>()
        readableDatabase.rawQuery("SELECT * FROM $T ORDER BY $ID DESC", null).use { c ->
            val idI = c.getColumnIndexOrThrow(ID)
            val nomI = c.getColumnIndexOrThrow(NOM)
            val prenomI = c.getColumnIndexOrThrow(PRENOM)
            val telI = c.getColumnIndexOrThrow(TEL)
            while (c.moveToNext()) {
                list.add(Ecrivain(c.getInt(idI), c.getString(nomI), c.getString(prenomI), c.getString(telI)))
            }
        }
        return list
    }

    fun search(term: String): List<Ecrivain> {
        val q = "%$term%"
        val list = mutableListOf<Ecrivain>()
        readableDatabase.rawQuery(
            "SELECT * FROM $T WHERE $NOM LIKE ? OR $PRENOM LIKE ? OR $TEL LIKE ? ORDER BY $ID DESC",
            arrayOf(q, q, q)
        ).use { c ->
            val idI = c.getColumnIndexOrThrow(ID)
            val nomI = c.getColumnIndexOrThrow(NOM)
            val prenomI = c.getColumnIndexOrThrow(PRENOM)
            val telI = c.getColumnIndexOrThrow(TEL)
            while (c.moveToNext()) {
                list.add(Ecrivain(c.getInt(idI), c.getString(nomI), c.getString(prenomI), c.getString(telI)))
            }
        }
        return list
    }

    // ---------------- LIVRE CRUD ----------------
    fun insertLivre(titre: String, ecrivainId: Int): Long {
        val cv = ContentValues().apply {
            put(L_TITRE, titre)
            put(L_ECRIVAIN_ID, ecrivainId)
        }
        return writableDatabase.insert(TL, null, cv)
    }

    fun updateLivre(id: Int, titre: String): Int {
        val cv = ContentValues().apply { put(L_TITRE, titre) }
        return writableDatabase.update(TL, cv, "$L_ID=?", arrayOf(id.toString()))
    }

    fun deleteLivre(id: Int): Int =
        writableDatabase.delete(TL, "$L_ID=?", arrayOf(id.toString()))

    fun getLivresByEcrivain(ecrivainId: Int): List<Livre> {
        val list = mutableListOf<Livre>()
        readableDatabase.rawQuery(
            "SELECT * FROM $TL WHERE $L_ECRIVAIN_ID=? ORDER BY $L_ID DESC",
            arrayOf(ecrivainId.toString())
        ).use { c ->
            val idI = c.getColumnIndexOrThrow(L_ID)
            val titreI = c.getColumnIndexOrThrow(L_TITRE)
            val eIdI = c.getColumnIndexOrThrow(L_ECRIVAIN_ID)
            while (c.moveToNext()) {
                list.add(Livre(c.getInt(idI), c.getString(titreI), c.getInt(eIdI)))
            }
        }
        return list
    }
}
