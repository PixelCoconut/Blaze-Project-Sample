package com.example.alex.projectblaze

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast

class LoginActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initialiseUI(savedInstanceState)
    }

    fun initialiseUI(savedInstanceState: Bundle?) {
        restoreState(savedInstanceState)
    }

    fun restoreState(state: Bundle?) {
        if (state == null)
            return //no state to restore

        var userID: EditText = findViewById(R.id.textUserID)
        userID.setText(state.getString("UserID"))
    }

    override fun onSaveInstanceState(state: Bundle?) {
        var userID: EditText = findViewById(R.id.textUserID)

        state!!.putString("UserID", userID.text.toString())
        super.onSaveInstanceState(state)
    }

    fun executeLogin(view: View) {

        var userID: EditText = findViewById(R.id.textUserID)

        //if user did not enter an ID
        if (userID.text.toString().trim().equals("")) {
            Toast.makeText(applicationContext, resources.getString(R.string.empty_ID), Toast.LENGTH_SHORT).show()
        }

        //move to new activity
        else {
            var intent = Intent()
            intent.setClass(applicationContext, MapsActivity::class.java)
            startActivity(intent)
        }
    }
}
