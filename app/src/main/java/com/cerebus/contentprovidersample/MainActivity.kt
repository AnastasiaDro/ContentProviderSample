package com.cerebus.contentprovidersample

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var nameTextView: TextView
    private lateinit var saveButton: Button
    private lateinit var deleteButton: Button

    private val dbThread = CustomThreadWorker()
    private val mainHandler = Handler(Looper.getMainLooper())

    private lateinit var dbHelper: DbHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        nameTextView = findViewById(R.id.nameTextView)
        nameEditText = findViewById(R.id.nameEditText)
        saveButton = findViewById(R.id.saveButton)
        deleteButton = findViewById(R.id.deleteButton)


        dbThread.start()
        dbHelper = initDbHelper()

        setClickListeners()
    }

    private fun setClickListeners() {
        saveButton.setOnClickListener {
            if (nameEditText.text.isNotEmpty()) dbHelper.addDataAsync(nameEditText.text.toString())
            hideKeyboard(this)
        }

        deleteButton.setOnClickListener {
            if (nameEditText.text.isNotEmpty()) dbHelper.deleteDataAsync(nameEditText.text.toString())
        }
    }

    fun initDbHelper(): DbHelper {
        return DbHelper(this, dbThread, mainHandler, showName = {
            nameTextView.text = nameEditText.text.toString() + " added asynchronously"
            Log.d("MainActivity", "showName")
        }, showDeleteName = {
            nameTextView.text = nameEditText.text.toString() + " deleted asynchronously"
            Log.d("MainActivity", "deleteName")
        })
    }
}

fun hideKeyboard(activity: Activity) {
    val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    //Find the currently focused view, so we can grab the correct window token from it.
    var view = activity.currentFocus
    //If no view currently has focus, create a new one, just so we can grab a window token from it
    if (view == null) {
        view = View(activity)
    }
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}


