package com.autotech.barcodemobilereader.controller.barcodescanner.camera;

import android.os.Handler;
import android.os.HandlerThread;

class CameraThread {
    private static CameraThread instance;
    private final Object LOCK = new Object();
    private Handler handler;
    private HandlerThread thread;

    private int openCount = 0;

    private CameraThread() {
    }

    public static CameraThread getInstance() {
        if (instance == null) {
            instance = new CameraThread();
        }
        return instance;
    }

    protected void enqueue(Runnable runnable) {
        synchronized (LOCK) {
            checkRunning();
            this.handler.post(runnable);
        }
    }

    protected void enqueueDelayed(Runnable runnable, long delayMillis) {
        synchronized (LOCK) {
            checkRunning();
            this.handler.postDelayed(runnable, delayMillis);
        }
    }

    private void checkRunning() {
        synchronized (LOCK) {
            if (this.handler == null) {
                if (openCount <= 0) {
                    throw new IllegalStateException("CameraThread is not open");
                }
                this.thread = new HandlerThread("CameraThread");
                this.thread.start();
                this.handler = new Handler(thread.getLooper());
            }
        }
    }

    private void quit() {
        synchronized (LOCK) {
            this.thread.quit();
            this.thread = null;
            this.handler = null;
        }
    }

    protected void decrementInstances() {
        synchronized (LOCK) {
            openCount -= 1;
            if (openCount == 0) {
                quit();
            }
        }
    }

    protected void incrementAndEnqueue(Runnable runner) {
        synchronized (LOCK) {
            openCount += 1;
            enqueue(runner);
        }
    }
}
