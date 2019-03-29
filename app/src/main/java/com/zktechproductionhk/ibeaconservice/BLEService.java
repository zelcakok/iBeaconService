package com.zktechproductionhk.ibeaconservice;

import android.app.Service;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.zktechproductionhk.ibeaconservice.Hardwares.EddyStoneSpec.EddyStone;
import com.zktechproductionhk.ibeaconservice.Hardwares.EddyStoneSpec.EddyStoneRecord;
import com.zktechproductionhk.ibeaconservice.Hardwares.Sensors;

import java.util.Timer;
import java.util.TimerTask;

public class BLEService extends Service {
    public enum HEALTH_STATE {
        EXCELLENT, OKAY, BAD
    }

    public interface OnHealthStateChangeListener {
        void change(HEALTH_STATE healthState);
    }

    private static final String TAG = "[BLEService]";

    private BLEServiceBinder serviceBinder = new BLEServiceBinder();

    private VectorDoc storage;

    private long checkCoverageInterval = 1000 * 10;
    private HEALTH_STATE healthState = HEALTH_STATE.BAD;
    private OnHealthStateChangeListener listener;

    private boolean isScanning = false;
    private Sensors ble;
    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if (result.getScanRecord().getServiceUuids() != null) {
                String stone = EddyStone.getStone(result.getScanRecord().getBytes());
                if (stone != null) {
                    String[] stoneInfo = stone.split("_");
                    EddyStoneRecord record = new EddyStoneRecord(stoneInfo[0], stoneInfo[1], result.getRssi());
                    storage.addRecord(record);
                    Log.i(TAG, "Scan results: " + record);
                }
            }

        }
    };

    private Timer timer;

    public BLEService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return serviceBinder;
    }

    public class BLEServiceBinder extends Binder {
        BLEService getService(Sensors ble, OnHealthStateChangeListener listener) {
            BLEService.this.storage = new VectorDoc();
            BLEService.this.ble = ble;
            BLEService.this.listener = listener;
            return BLEService.this;
        }
    }

    private void checkConverage() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                double coverage = storage.getCoverage();
                setHealthState(coverage);
                Log.i(TAG, "Coverage: " + String.valueOf(coverage) + ", interval: " + checkCoverageInterval);
            }
        }, checkCoverageInterval, checkCoverageInterval);
    }

    private void setHealthState(double coverage) {
        HEALTH_STATE raw = healthState;
        if (coverage > 0.8) healthState = HEALTH_STATE.EXCELLENT;
        else if (coverage > 0.4) healthState = HEALTH_STATE.OKAY;
        else healthState = HEALTH_STATE.BAD;
        if (raw != healthState) listener.change(healthState);
    }

    //Services here
    public void startScan() {
        checkConverage();
        ble.scan(scanCallback);
        isScanning = true;
    }

    public void stopScan() {
        timer.cancel();
        ble.stopScan(scanCallback);
        isScanning = false;
    }

    public boolean isScanning() {
        return isScanning;
    }

    public HEALTH_STATE getHealthState() {
        return healthState;
    }

    public void setCheckCoverageInterval(long interval) {
        checkCoverageInterval = interval;
        if (isScanning) {
            timer.cancel();
            checkConverage();
        }
    }

    public VectorDoc getVectorDoc() {
        try {
            return storage.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
            return null;
        }
    }
}
