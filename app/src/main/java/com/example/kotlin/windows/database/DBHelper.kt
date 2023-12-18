package com.example.kotlin.windows.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(private val context: Context) {

    private val dbName = "LocalDB"
    private val dbVersion = 1
    private var currentTableName: String = ""

    private inner class DBHelper(context: Context, val filed: List<Pair<String, String>>) : SQLiteOpenHelper(context, dbName, null, dbVersion) {
        override fun onCreate(db: SQLiteDatabase) {
            var command = "CREATE TABLE IF NOT EXISTS $currentTableName (id INTEGER PRIMARY KEY AUTOINCREMENT"
            for (pair in filed) {
                command += ", " + pair.first + " " + pair.second
            }
            command += ")"
            db.execSQL(command)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL("DROP TABLE IF EXISTS $currentTableName")
            onCreate(db)
        }
    }

    private var dbHelper: DBHelper? = null
    private var database: SQLiteDatabase? = null

    fun selectTable(tableName: String, filed: List<Pair<String, String>>) {
        currentTableName = tableName
        dbHelper = DBHelper(context, filed)
        database = dbHelper!!.writableDatabase
    }

    fun clearSelectedTable() {
        val db = dbHelper?.writableDatabase
        if (db != null) {
            dbHelper?.onUpgrade(db, 1, 1)
        }
    }

    fun recordExists(columnName: String, valueToCheck: String): Boolean {
        val query = "SELECT $columnName FROM $currentTableName WHERE $columnName = ?"
        val cursor = database?.rawQuery(query, arrayOf(valueToCheck))

        cursor?.use {
            if (it.moveToFirst())
                return it.count > 0
        }

        return false
    }

    fun deleteRecordsByColumnValue(columnName: String, value: String, operator: String = "=") {
        val whereClause = "$columnName " + operator + " ?"
        val whereArgs = arrayOf(value)

        database?.delete(currentTableName, whereClause, whereArgs)
    }

    fun getAllDataFromCurrentTable(): List<List<String>> {
        val data = mutableListOf<List<String>>()
        val cursor = database?.rawQuery("SELECT * FROM $currentTableName;", null)
        cursor?.use {
            val columnCount = cursor.columnCount
            while (it.moveToNext()) {
                val rowData = mutableListOf<String>()
                for (i in 0 until columnCount) {
                    val value = it.getString(i)
                    rowData.add(value)
                }
                data.add(rowData)
            }
        }
        return data
    }

    fun getAllDataFromCurrentTableByCursor(): Cursor? {
        return database?.rawQuery("SELECT * FROM $currentTableName;", null)
    }

    fun addDataToCurrentTable(data: List<Pair<String, String>>) {
        val values = ContentValues()
        for (item in data) {
            values.put(item.first, item.second)
        }
        database?.insert(currentTableName, null, values)
    }

    fun getDataFromCurrentTableWhere(columnName: String, operator: String, filterValue: String): List<List<String>> {
        val data: MutableList<List<String>> = mutableListOf<List<String>>()

        val selection = "$columnName $operator ?"
        val selectionArgs = arrayOf(filterValue)

        val cursor = database?.query(currentTableName, null, selection, selectionArgs, null, null, null)

        cursor?.use {
            val columnCount = cursor.columnCount
            while (it.moveToNext()) {
                val rowData = mutableListOf<String>()
                for (i in 0 until columnCount) {
                    val value = it.getString(i)
                    rowData.add(value)
                }
                data.add(rowData)
            }
        }

        return data
    }

    fun getDataFromCurrentTableWhereByCursor(columnName: String, operator: String, filterValue: String): Cursor? {
        val selection = "$columnName $operator ?"
        val selectionArgs = arrayOf(filterValue)
        return database?.query(currentTableName, null, selection, selectionArgs, null, null, null)
    }

    fun updateColumnById(id: Int, columnName: String, value: String) {
        val values = ContentValues()
        values.put(columnName, value)

        val whereClause = "id = ?"
        val whereArgs = arrayOf(id.toString())

        database?.update(currentTableName, values, whereClause, whereArgs)
    }

    fun getId(cursor: Cursor): Int {
        val idColumnIndex = cursor.getColumnIndex("id")
        return cursor.getInt(idColumnIndex)
    }

    fun setAllRecordWhere(
        column: String,
        operator: String,
        value: String,
        columnTarget: String,
        newValue: String
    ) {
        val values = ContentValues()
        values.put(columnTarget, newValue)

        val whereClause = "$column $operator ?"
        val whereArgs = arrayOf(value)

        database?.update(currentTableName, values, whereClause, whereArgs)
    }


}