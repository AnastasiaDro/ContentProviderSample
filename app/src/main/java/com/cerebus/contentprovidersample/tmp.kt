package com.cerebus.contentprovidersample

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri

class BooksProvider : ContentProvider() {

    private lateinit var database: SQLiteDatabase

    companion object {
        private const val DATABASE_NAME = "books.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "books"
        private const val AUTHORITY = "com.example.booksprovider"
        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/$TABLE_NAME")
        private const val CONTENT_TYPE = "vnd.android.cursor.dir/vnd.example.books"
        private const val CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.example.books"
        private const val BOOKS = 1
        private const val BOOK_ID = 2

        private val sUriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, TABLE_NAME, BOOKS)
            addURI(AUTHORITY, "$TABLE_NAME/#", BOOK_ID)
        }
    }

    private class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL("CREATE TABLE $TABLE_NAME (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "title TEXT NOT NULL, " +
                    "author TEXT NOT NULL);")
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
            onCreate(db)
        }
    }

    override fun onCreate(): Boolean {
        val context: Context = context ?: return false
        val dbHelper = DatabaseHelper(context)
        database = dbHelper.writableDatabase
        return database != null
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        val cursor: Cursor?
        when (sUriMatcher.match(uri)) {
            BOOKS -> cursor = database.query(TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder)
            BOOK_ID -> {
                val id = uri.lastPathSegment
                val newSelection = "_id=$id"
                cursor = database.query(TABLE_NAME, projection, newSelection, selectionArgs, null, null, sortOrder)
            }
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
        cursor?.setNotificationUri(context!!.contentResolver, uri)
        return cursor
    }

    override fun getType(uri: Uri): String? {
        return when (sUriMatcher.match(uri)) {
            BOOKS -> CONTENT_TYPE
            BOOK_ID -> CONTENT_ITEM_TYPE
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val rowId = database.insert(TABLE_NAME, null, values)
        if (rowId > 0) {
            val rowUri = ContentUris.withAppendedId(CONTENT_URI, rowId)
            context!!.contentResolver.notifyChange(rowUri, null)
            return rowUri
        }
        throw SQLException("Failed to insert row into $uri")
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        val count: Int
        when (sUriMatcher.match(uri)) {
            BOOKS -> count = database.delete(TABLE_NAME, selection, selectionArgs)
            BOOK_ID -> {
                val id = uri.lastPathSegment
                val newSelection = "_id=$id" + (if (!selection.isNullOrEmpty()) " AND ($selection)" else "")
                count = database.delete(TABLE_NAME, newSelection, selectionArgs)
            }
            else -> throw IllegalArgumentException("Unknown URI $uri")
        }
        context!!.contentResolver.notifyChange(uri, null)
        return count
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        val count: Int
        when (sUriMatcher.match(uri)) {
            BOOKS -> count = database.update(TABLE_NAME, values, selection, selectionArgs)
            BOOK_ID -> {
                val id = uri.lastPathSegment
                val newSelection = "_id=$id" + (if (!selection.isNullOrEmpty()) " AND ($selection)" else "")
                count = database.update(TABLE_NAME, values, newSelection, selectionArgs)
            }
            else -> throw IllegalArgumentException("Unknown URI $uri")
        }
        context!!.contentResolver.notifyChange(uri, null)
        return count
    }
}
