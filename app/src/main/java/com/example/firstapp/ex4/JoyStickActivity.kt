package com.example.firstapp.ex4

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
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
import java.lang.Math.abs
import java.lang.Math.sqrt
import kotlin.math.sqrt


class JoyStickDrawing (context: Context): View (context) {

    // The ‘active pointer’ is the one currently moving our object.
    private var xPos : Float = 0f
    private var yPos : Float = 0f
    private var radius : Float = 0f
    private var screen = RectF(0f, 0f, width.toFloat(), height.toFloat())
    private var outerRadius : Float = 0f

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        xPos = (width / 2).toFloat()
        yPos = (height / 2).toFloat()
    }

    fun sendMessage(msg : String) {

        var parent_activity : JoyStickActivity = context as JoyStickActivity
        var socket : Socket = parent_activity.getSocket()
        var bufferOut : PrintWriter = parent_activity.getBufferOut()

        if (msg.isNullOrEmpty()) {
            Toast.makeText(context,"Bad msg.", Toast.LENGTH_SHORT).show()
            return
        }

        if (socket.isConnected  && !bufferOut.checkError()) {
            try {
                bufferOut.println(msg)
                bufferOut.flush()
            } catch (e : java.lang.Exception) {
                Log.e("Exception", e.message)
            }
        } else {
            Log.e("ERROR", "Could not send message")
        }
    }

    fun sendToServer() {
        // Use normalization formula
        var normalizedValX = 0f
        var normalizedValY = 0f

        if (this.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            normalizedValX = (2 * (xPos / width)) - 1
            normalizedValY = (2 * ((yPos - ((height / 2) + outerRadius)) / (((height / 2) - outerRadius)- ((height / 2) + outerRadius)))) - 1
        } else {
            normalizedValX = (2 * ((xPos - ((width / 2) - outerRadius)) / (((width / 2) + outerRadius) - ((width / 2) - outerRadius)))) - 1
            normalizedValY = (2 * ((yPos - height) / -height)) - 1
        }

        val msg1 : String = "set aileron to " + (normalizedValX).toString() + "\n"
        val msg2 : String = "set elevator to " + (normalizedValY).toString() + "\n"

        sendMessage(msg1)
        sendMessage(msg2)
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        val action = event!!.actionMasked

        when (action) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                xPos = (width / 2).toFloat()
                yPos = (height / 2).toFloat()
                invalidate()
            }

            else -> {
                val xTouch = event.getX()
                val yTouch = event.getY()
                val xCenter = (width / 2).toFloat()
                val yCenter = (height / 2).toFloat()

                val isTouchOnJoystick = ((xPos + radius) >= xTouch) && ((xPos - radius) <= xTouch)
                        && ((yPos + radius) >= yTouch) && ((yPos - radius) <= yTouch)

                val isTouchInOval = (sqrt((yCenter - yTouch) * (yCenter - yTouch) + (xCenter - xTouch) * (xCenter - xTouch))) <= outerRadius
                //val isTouchInOval = (abs(xCenter - xTouch) <= outerRadius) && (abs(yCenter - yTouch) <= outerRadius)

                // Check if the new position is on the joystick circle and in the oval
                if (isTouchOnJoystick && isTouchInOval) {
                    xPos = event.getX()
                    yPos = event.getY()
                    sendToServer()
                    invalidate()
                }
                else if (!isTouchInOval) {
                    sendToServer()
                }
            }
        }
        return true
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas!!.drawRGB (255, 255, 255)
        val width = getWidth()
        val height = getHeight()

        val minor: Int
        if (width < height)
            minor = width
        else
            minor = height

        val brush = Paint()
        brush.setARGB (255, 79, 26, 26)
        brush.setStrokeWidth(20f)
        brush.setStyle(Paint.Style.STROKE)

        screen = RectF(0f, 0f, width.toFloat(), height.toFloat())
        //canvas.drawOval (screen, brush)
        outerRadius = (minor / 2).toFloat()
        canvas.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), outerRadius, brush)

        brush.setStyle(Paint.Style.FILL)
        brush.setARGB (255, 173, 173, 173)
        canvas.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), outerRadius, brush)

        brush.setStyle(Paint.Style.FILL)
        brush.setARGB (255, 204, 0, 0)
        if (xPos == 0.0f && yPos == 0.0f) {
            xPos = (width / 2).toFloat()
            yPos = (height / 2).toFloat()
        }
        radius = (minor / 6).toFloat()

        canvas.drawCircle(xPos, yPos, radius, brush)
    }
}

class JoyStickActivity : AppCompatActivity() {

    private var ip: String = ""
    private var port: String = ""
    private lateinit var socket: Socket
    private lateinit var mBufferOut: PrintWriter
    private lateinit var mBufferIn: BufferedReader

    fun getSocket() : Socket {
        return socket
    }

    fun getBufferOut() : PrintWriter {
        return mBufferOut
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(JoyStickDrawing(this))

        ip = intent.getStringExtra("IP")
        port = intent.getStringExtra("PORT")

        ConnectTask().run()
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
