package com.autotech.barcodemobilereader.controller.zxing.client.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Handler;

public final class InactivityTimer {

    private static final long INACTIVITY_DELAY_MS = 5 * 60 * 1000L;

    private final Context context;
    private final BroadcastReceiver powerStatusReceiver;
    private boolean registered = false;
    private Handler handler;
    private Runnable callback;
    private boolean onBattery;

    public InactivityTimer(Context context, Runnable callback) {
        this.context = context;
        this.callback = callback;

        powerStatusReceiver = new PowerStatusReceiver();
        handler = new Handler();
    }

    public void activity() {
        cancelCallback();
        if (onBattery) {
            handler.postDelayed(callback, INACTIVITY_DELAY_MS);
        }
    }

    public void start() {
        registerReceiver();
        activity();
    }

    public void cancel() {
        cancelCallback();
        unregisterReceiver();
    }

    private void unregisterReceiver() {
        if (registered) {
            context.unregisterReceiver(powerStatusReceiver);
            registered = false;
        }
    }

    private void registerReceiver() {
        if (!registered) {
            context.registerReceiver(powerStatusReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            registered = true;
        }
    }

    private void cancelCallback() {
        handler.removeCallbacksAndMessages(null);
    }

    private void onBattery(boolean onBattery) {
        this.onBattery = onBattery;
        if (registered) {
            activity();
        }
    }

    private final class PowerStatusReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                final boolean onBatteryNow = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) <= 0;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        onBattery(onBatteryNow);
                    }
                });
            }
        }
    }
}
