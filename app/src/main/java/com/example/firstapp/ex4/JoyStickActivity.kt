package com.example.firstapp.ex4

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import TcpClient
import android.util.Log
import android.view.View
import android.widget.Toast
import java.io.*
import java.net.Inet4Address
import java.net.InetAddress
import java.net.Socket


class JoyStickActivity : AppCompatActivity() {

    private lateinit var tcpClient : TcpClient
    private var ip: String = ""
    private var port: String = ""
    private lateinit var socket: Socket
    private lateinit var mBufferOut: PrintWriter
    private lateinit var mBufferIn: BufferedReader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_joy_stick)

        ip = intent.getStringExtra("IP")
        port = intent.getStringExtra("PORT")

        ConnectTask().run()

        findViewById<EditText>(R.id.editText111).setText(ip)
        findViewById<EditText>(R.id.editText222).setText(port)
    }

    fun onClickBtn(v: View) {

        val msg : String = (findViewById<EditText>(R.id.msg_edittext)).text.toString() + "\n"

        if (msg.isNullOrEmpty()) {
            Toast.makeText(this, "Bad msg.", Toast.LENGTH_SHORT).show()
            return
        }

        if (socket.isConnected  && !mBufferOut.checkError()) {
            try {
                mBufferOut.println(msg)
            } catch (e : java.lang.Exception) {
                Log.e("Exception", e.message)
            }
        } else {
            Log.e("ERROR", "Could not send message")
        }
    }

    override fun onDestroy() {
        DisconnectTask().run()
        super.onDestroy()
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, JoyStickActivity::class.java)
        }

    }

    inner class ConnectTask : Runnable {
        override fun run() {
            try {
                val sAddress :InetAddress = Inet4Address.getByName(ip)
                socket = Socket(sAddress, Integer.parseInt(port))
                mBufferOut = PrintWriter(BufferedWriter(OutputStreamWriter(socket.getOutputStream())), true)
                mBufferIn = BufferedReader(InputStreamReader(socket.getInputStream()))
            } catch (e : Exception) {
                Log.e("Exception", e.message)
            }
        }
    }

    inner class DisconnectTask : Runnable {
        override fun run() {
            try {
                mBufferIn.close()
                mBufferOut.close()
            } catch (e : Exception) {
                Log.e("Exception", e.message)
            }

            if (!socket.isClosed) {
                try {
                    socket.close()
                } catch (e: java.lang.Exception) {
                    Log.e("Exception", e.message)
                }
            }
        }
    }

}
