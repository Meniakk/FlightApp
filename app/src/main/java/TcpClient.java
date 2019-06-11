import android.os.StrictMode;
import android.util.Log;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;

public final class TcpClient {

    private String ip;
    private String port;
    private PrintWriter mBufferOut = null;
    private BufferedReader mBufferIn = null;
    private Socket socket = null;
    private boolean shouldStop = false;

    //todo TEST
    public TcpClient(String ip, String port) {
        this.ip = ip;
        this.port = port;
    }

    public boolean connect() {
        try {
            InetAddress sAddress = Inet4Address.getByName(ip);
            int port_int = Integer.parseInt(port);
            socket = new Socket(sAddress, port_int);
            mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
            mBufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            while (!shouldStop) {

                String serverMsg = mBufferIn.readLine();
                if (serverMsg != null && !serverMsg.isEmpty()) {
                    Log.e("Server Response", serverMsg);
                }
            }

            if (!disconnect()) {
                throw new Exception("Could not disconnect.");
            }

        } catch (Exception e) {
            Log.e("Exception", e.getMessage());
            return false;
        }
        return true;
    }

    public void sendMessage(String msg) {
        if (socket != null && mBufferOut != null && !mBufferOut.checkError()) {
            mBufferOut.println(msg);
        } else {
            Log.e("ERROR","Could not send message");
        }


    }

    private boolean disconnect() {

        if (socket == null || socket.isClosed()) {
            return true;
        }

        try {
            mBufferOut.close();
            socket.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void stopClient() {
        this.shouldStop = true;
    }
}
