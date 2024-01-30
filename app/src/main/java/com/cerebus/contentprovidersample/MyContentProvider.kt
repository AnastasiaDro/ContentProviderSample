package com.cerebus.contentprovidersample

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.database.Cursor
import android.database.SQLException
import android.net.Uri
import com.cerebus.contentprovidersample.DbHelper.Companion.TABLE_NAME
import java.lang.IllegalArgumentException

class MyContentProvider : ContentProvider() {

    private lateinit var dbHelper: DbHelper

    private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
        addURI(AUTHORITY, DbHelper.TABLE_NAME, NAMES)
        addURI(AUTHORITY, DbHelper.TABLE_NAME, NAMES_ID)
    }

    override fun onCreate(): Boolean {
        val context: Context = context ?: return false
        dbHelper = DbHelper(context)
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        val cursor: Cursor?
        val db = dbHelper.readableDatabase
        when(uriMatcher.match(uri)) {
            NAMES -> cursor = db.query(TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder)
            NAMES_ID -> {
                val id = uri.lastPathSegment
                val newSelection = "${DbHelper.COLUMN_ID}=$id"
                cursor = db.query(TABLE_NAME, projection, newSelection, selectionArgs, null, null, sortOrder)
            }
            else -> {
                db.close()
                throw IllegalArgumentException("Unknowm URI; $uri")
            }
        }
        return cursor
    }

    override fun getType(uri: Uri): String =
        when(uriMatcher.match(uri)) {
            NAMES -> ALL
            NAMES_ID -> ONE_NAME
            else -> throw IllegalArgumentException("Unknown URI type or uri: $uri")
        }
    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val db = dbHelper.writableDatabase
        val rowId = db.insert(TABLE_NAME, null, values)
        if (rowId > 0) {
            val rowUri = ContentUris.withAppendedId(CONTENT_URI, rowId)
            //???
            db.close()
            return rowUri
        }
        throw SQLException("Failed to insert row into $uri")
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        TODO("Not yet implemented")
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        TODO("Not yet implemented")
    }

    companion object {
        private const val AUTHORITY = "com.cerebus.contentprovidersample.MyContentProvider"
        private const val NAMES = 1
        private const val NAMES_ID = 2
        private const val ALL = "com.android.cursor.dir/com.cerebus.names"
        private const val ONE_NAME = "com.android.cursor.item/com.cerebus.names"
        val CONTENT_URI: Uri = Uri.parse("content://${AUTHORITY}/${DbHelper.TABLE_NAME}")
    }
}