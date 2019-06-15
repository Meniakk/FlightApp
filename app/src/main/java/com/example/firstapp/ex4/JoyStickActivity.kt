package com.example.firstapp.ex4

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.util.Log
import android.view.View
import android.widget.Toast
import java.io.*
import java.net.Inet4Address
import java.net.InetAddress
import java.net.Socket
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.drawable.BitmapDrawable
import android.support.v4.view.MotionEventCompat
import android.view.MotionEvent
import android.view.MotionEvent.INVALID_POINTER_ID
import android.view.ScaleGestureDetector


class JoyStickDrawing (context: Context): View (context) {

    // The ‘active pointer’ is the one currently moving our object.
    private var mActivePointerId = INVALID_POINTER_ID
    private var xPos : Float = 0.toFloat()
    private var yPos : Float = 0.toFloat()
    private var radius : Float = 0.toFloat()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        xPos = (width / 2).toFloat()
        yPos = (height / 2).toFloat()
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        val action = event!!.actionMasked
        //TODO PREVENT LEAVING OVAL
        when (action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP -> {
                var xTouch = event.getX()
                var yTouch = event.getY()

                // Check if the new position is inside the joystick
                if (((xPos + radius) >= xTouch) && ((xPos - radius) <= xTouch)
                    && ((yPos + radius) >= yTouch) && ((yPos - radius) <= yTouch)) {
                    xPos = event.getX()
                    yPos = event.getY()
                    // Save the ID of this pointer (for dragging)
                    mActivePointerId = event.getPointerId(0)
                    invalidate()
                }
            }

            MotionEvent.ACTION_CANCEL -> {
                xPos = (width / 2).toFloat()
                yPos = (height / 2).toFloat()
            }
        }
        return true
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas!!.drawRGB (255, 255, 255)
        val width = getWidth()
        val height = getHeight()

        val brush = Paint()
        brush.setARGB (255, 79, 26, 26)
        brush.setStrokeWidth(20f)
        brush.setStyle(Paint.Style.STROKE)
        val screen = RectF(0f, 0f, width.toFloat(), height.toFloat())
        canvas.drawOval(screen, brush)

        brush.setStyle(Paint.Style.FILL)
        brush.setARGB (255, 173, 173, 173)
        canvas.drawOval(screen, brush)

        val minor: Int
        if (width < height)
            minor = width
        else
            minor = height

        brush.setStyle(Paint.Style.FILL)
        brush.setARGB (255, 204, 0, 0)
        if (xPos == 0.0f && yPos == 0.0f) {
            xPos = (width / 2).toFloat()
            yPos = (height / 2).toFloat()
            radius = (minor / 8).toFloat()
        }
        canvas.drawCircle(xPos, yPos, radius, brush)
    }

    /*override fun onTouchEvent(event: MotionEvent?): Boolean {
        val action = event!!.actionMasked

        when (action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP -> {
                event.also { pointerIndex ->
                    // Remember where we started (for dragging)
                    xPos = event.getX()
                    yPos = event.getY()
                }

                // Save the ID of this pointer (for dragging)
                mActivePointerId = event.getPointerId(0)
            }

            MotionEvent.ACTION_CANCEL -> {
                xPos = (width / 2).toFloat()
                yPos = (height / 2).toFloat()
            }
        }
        return true
    }*/
}

class JoyStickActivity : AppCompatActivity() {

    private var ip: String = ""
    private var port: String = ""
    private lateinit var socket: Socket
    private lateinit var mBufferOut: PrintWriter
    private lateinit var mBufferIn: BufferedReader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(JoyStickDrawing(this))

        ip = intent.getStringExtra("IP")
        port = intent.getStringExtra("PORT")

        ConnectTask().run()

        //findViewById<EditText>(R.id.editText111).setText(ip)
        //findViewById<EditText>(R.id.editText222).setText(port)
    }

    /*fun onClickBtn(v: View) {

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
    }*/

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
