package com.example.firstapp.ex4

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import android.os.StrictMode





class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun onClickBtn(v: View) {

        val ip : String   = (findViewById<EditText>(R.id.ip_edit_text  )).text.toString()
        val port : String = (findViewById<EditText>(R.id.port_edit_text)).text.toString()
        val intent = JoyStickActivity.newIntent(this)

        if (ip.isNullOrEmpty() || port.isNullOrEmpty()) {
            Toast.makeText(this, "Bad port or ip.", Toast.LENGTH_SHORT).show()
            return
        }

        intent.putExtra("IP", ip)
        intent.putExtra("PORT", port)

        startActivity(intent)
    }
}
