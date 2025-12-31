package com.example.gestionbibliotheque

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(ctx: Context) : SQLiteOpenHelper(ctx, "biblio.db", null, 2) {

    companion object {
        private const val T = "ecrivain"
        private const val ID = "id"
        private const val NOM = "nom"
        private const val PRENOM = "prenom"
        private const val TEL = "tel"
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
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $T")
        onCreate(db)
    }

    fun insert(nom: String, prenom: String, tel: String): Long {
        val cv = ContentValues().apply {
            put(NOM, nom); put(PRENOM, prenom); put(TEL, tel)
        }
        // Si TEL existe déjà (UNIQUE), insert() retourne -1
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
}
