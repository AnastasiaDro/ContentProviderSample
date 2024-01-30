package com.cerebus.contentprovidersample

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Handler
import android.util.Log

class DbHelper(context: Context, thread: CustomThreadWorker?, val mainHandler: Handler?, val showName: () -> Unit, val showDeleteName: () -> Unit) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    constructor(context: Context) : this(context, null, null, {}, {})

    private val customHandler = object : CustomHandler(thread?.looper) {
        override fun onReceiveMessage(message: CustomMessage) {
            Log.d(DATABASE_NAME, message.desc)
        }
    }
    companion object {
        const val DATABASE_NAME = "MyDatabase.db"
        const val DATABASE_VERSION = 1
        const val TABLE_NAME = "ExampleTable"
        const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableSql = ("CREATE TABLE IF NOT EXISTS $TABLE_NAME ($COLUMN_ID INTEGER PRIMARY KEY, $COLUMN_NAME TEXT)")
        db?.execSQL(createTableSql)
    }

    /** Dummy realisation **/
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun addData(name: String) {
        val db = this.writableDatabase
        val values = ContentValues()
        Log.d(DATABASE_NAME, "data $name was added")
        values.put(COLUMN_NAME, name)
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    fun addDataAsync(name: String) {
        customHandler.post(CustomMessage(customHandler, {
            addData(name)
            mainHandler!!.post { showName.invoke() }
        }, "Custom handler received message ADD $name"))
    }

    fun deleteDataAsync(name: String) {
        customHandler.post(CustomMessage(customHandler, {
            deleteData(name)
            mainHandler!!.post { showDeleteName.invoke() }
            }, "Custom handler received message DELETE $name"))
    }

    fun makeQuery(query: String): Cursor {
        val db = this.readableDatabase
        return db.rawQuery(query, null)
    }

    @SuppressLint("Range")
    fun getData(): ArrayList<String> {
        val data = ArrayList<String>()
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_NAME"
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                val name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME))
                data.add(name)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return data
    }

    fun closeCursor(cursor: Cursor) {
        cursor.close()
        this.readableDatabase.close()
    }

    fun deleteData(name: String) {
        val db = this.writableDatabase
        Log.d(DATABASE_NAME, "data $name was deleted")
        db.delete(TABLE_NAME, "$COLUMN_NAME = ?", arrayOf(name))
        db.close()
    }
}