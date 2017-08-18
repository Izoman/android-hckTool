/*
 * This file is part of Network Spoofer for Android.
 * Network Spoofer lets you change websites on other people’s computers
 * from an Android phone.
 * Copyright (C) 2014 Will Shackleton <will@digitalsquid.co.uk>
 *
 * Network Spoofer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Network Spoofer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Network Spoofer, in the file COPYING.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.izoman.hcktool.expert.roguenetwork;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class NSProxy implements LogConf {
    
    public static final int PROXY_PORT = 3128;
    
    protected final List<Spoof> spoofs;

    private boolean launchTaskCancelled = false;
    private final Object launchTaskCancelledSync = new Object();

    public NSProxy(Context context, List<Spoof> spoofs) {
        this.spoofs = spoofs;
        for (Spoof spoof : this.spoofs) {
            spoof.transientInit(context);
        }
    }
    
    @SuppressLint("NewApi")
    public void start() {
        launchTask.start();
    }
    
    public void stop() {
        synchronized (launchTaskCancelledSync) {
            launchTaskCancelled = true;
        }
        try {
            ss.close();
        } catch (IOException e) { }
    }
    
    private ServerSocket ss;
    
    private Thread launchTask =
            new Thread() {

        public void run() {
            Log.i(TAG, "Starting proxy server");
            final int procs = Runtime.getRuntime().availableProcessors();
            final BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(procs * 20);
            final ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
                    procs,
                    procs * 5,
                    1,
                    TimeUnit.MINUTES,
                    queue);
            try {
                ss = new ServerSocket(3128);
                while(!isCancelled()) {
                    Socket socket;
                    try {
                        socket = ss.accept();
                    } catch (SocketException e) {
                        // Socket closed
                        break;
                    }
                    Log.v(TAG, "New socket accepted, queue size is " + queue.size());
                    
                    ProxyTask task = new ProxyTask(socket);
                    threadPool.execute(task);
                    threadPool.setRejectedExecutionHandler(new RejectedExecutionHandler() {
                        @Override
                        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                            if (executor.isTerminating()) {
                                Log.i(TAG, "Rejected job ignored during shutdown of thread pool");
                            } else {
                                // Block until job can be placed in queue
                                try {
                                    queue.put(r);
                                } catch (InterruptedException e) {
                                    Log.i(TAG, "Failed to add job to queue");
                                }
                            }
                        }
                    });
                }
            } catch (IOException e) {
                Log.e(TAG, "Socket accepting failed", e);
                return;
            } finally {
                if(ss != null)
                    try {
                        ss.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to close proxy socket", e);
                    }
            }
        }

        public boolean isCancelled() {
            synchronized (launchTaskCancelledSync) {
                return launchTaskCancelled;
            }
        }
    };
    
    private static final byte[] NEWLINE = new byte[] {'\r','\n'};

    private int num = 1;
    
    private class ProxyTask implements Runnable {
        
        private final Socket socket;
        
        public ProxyTask(Socket socket) {
            this.socket = socket;
        }
        
        @Override
        public void run() {
            doInBackground(socket);
        }

        protected int doInBackground(Socket socket) {
            MagicInputStream input = null;
            BufferedOutputStream output = null;
            try {
                Log.v(TAG, "New connection opened");
                input = new MagicInputStream(
                        new BufferedInputStream(socket.getInputStream()));
                output = new BufferedOutputStream(socket.getOutputStream());

                HttpRequest request = new HttpRequest();
                request.setRequestLine(input.readStringLine());
                String header;
                while(!(header = input.readStringLine()).equals("")) {
                    request.addHeader(header);
                }
                // Rest is content now.
                if(request.hasHeader("Content-Length")) {
                    int len = Integer.parseInt(
                            request.getHeader("Content-Length").get(0));
                    request.readAllContent(input, len);
                }
                
                // Filter annoying requests
                if(!filterRequest(request)) return 1;
                
                // Manipulate request
                manipulateRequest(request);
                
                // Execute
                try {
                    HttpResponse response = executeRequest(request);
                    if(!whitelistRequest(request))
                        manipulateResponse(response, request);
                    output.write(String.format("HTTP/1.1 %d %s\r\n",
                            response.getResponseCode(),
                            response.getResponseMessage()).getBytes());
                    
                    for(Entry<String, List<String>> entry :
                        response.getHeaderPairs().entrySet()) {
                        for(String value : entry.getValue())
                            output.write(String.format("%s:%s\r\n",
                                    entry.getKey(),
                                    value).getBytes());
                    }
                    Log.d(TAG, "Writing content");
                    output.write(NEWLINE);
                    output.write(response.getContent());
                    output.flush();
                } catch(HttpExecuteException e) {
                    Log.e(TAG, "Failed to execute HTTP request", e);
                } catch(IOException e) {
                    Log.e(TAG, "Failed to execute HTTP request", e);
                }
            } catch (IOException e) {
                Log.i(TAG, "Network communications failed for HTTP connection", e);
            } finally {
                if(input != null)
                    try {
                        output.close();
                        input.close();
                        socket.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to close reader for HTTP connection", e);
                    }
            }
            return 0;
        }
    }
    
    private void manipulateRequest(HttpRequest request) {
        for(Spoof spoof : spoofs)
            spoof.modifyRequest(request);
    }
    
    private void manipulateResponse(HttpResponse response, HttpRequest request) {
        for(Spoof spoof : spoofs) {
            try {
                spoof.modifyResponse(response, request);
            } catch(Exception e) {
                Log.w(TAG, "Manipulate failed", e);
            }
        }
    }
    
    static {
        HttpURLConnection.setFollowRedirects(false);
    }
    
    /**
     * 
     * @param request
     * @return
     * @throws IOException 
     * @throws HttpExecuteException
     * @throws MalformedURLException
     */
    private HttpResponse executeRequest(HttpRequest request)
            throws IOException {
        if(request.shouldIgnoreResponse())
            return new HttpResponse();
        if(!request.hasHeader("Host"))
            throw new HttpExecuteException("HTTP Host not set");
        String host = request.getHeader("Host").get(0);
        
        URL url = new URL("http", host, 80, request.getPath());
        
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(request.getMethod());
        for(Entry<String, List<String>> entry : request.getHeaderPairs().entrySet()) {
            for(String value : entry.getValue())
                connection.addRequestProperty(entry.getKey(), value);
        }
        if (request.getMethod().equals("POST")) {
            // Write post data if it exists
            connection.getOutputStream().write(request.getContent());
        }

        HttpResponse response = new HttpResponse();
        response.setResponseCode(connection.getResponseCode());
        response.setResponseMessage(connection.getResponseMessage());
        for(Entry<String, List<String>> entry : connection.getHeaderFields().entrySet()) {
            for(String value : entry.getValue())
                response.addHeader(entry.getKey(), value);
        }
        // responseType is eg. the 4 in 403
        int responseType = response.getResponseCode() / 100;
        if(responseType == 4 || responseType == 5)
            response.readAllContent(connection.getErrorStream());
        else
            response.readAllContent(connection.getInputStream());
        
        // Add Network Spoofer fingerprint
        response.addHeader("X-Network-Spoofer", "ON");

        return response;
    }
    
    /**
     * Filters out known annoying requests.
     * @param request
     * @return <code>true</code> if the request should continue
     */
    private boolean filterRequest(HttpRequest request) {
        String host = request.getHost();
        if(host.contains(".dropbox.com")) return false;
        return true;
    }
    
    /**
     * Filters out requests that shouldn't be modified.
     * @param request
     * @return <code>true</code> if the request shouldn't be modified.
     */
    private boolean whitelistRequest(HttpRequest request) {
        String host = request.getHost();
        if(host.equals("gravityscript.googlecode.com")) return true;
        return false;
    }
}
