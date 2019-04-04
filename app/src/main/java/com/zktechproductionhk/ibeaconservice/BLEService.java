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
    public enum SCAN_MODE {
        REGULAR(1000 * 10), INTENSIVE(1000 * 3);

        long interval;

        SCAN_MODE(long interval) {
            this.interval = interval;
        }
    }

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

    private static Timer convergeChecker, intervalController;
    private static boolean isConvergeCheckerStarted = false, isScheduledStopped = false;

    private SCAN_MODE scanMode = SCAN_MODE.REGULAR;

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
        if (isConvergeCheckerStarted) return;
        isConvergeCheckerStarted = true;
        convergeChecker = new Timer();
        convergeChecker.schedule(new TimerTask() {
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

    private void intervalControl() {
        Log.i(TAG, "Interval control start");
        intervalController = new Timer();
        intervalController.schedule(new TimerTask() {
            @Override
            public void run() {
                stopScan(true);
                Log.i(TAG, "Interval control stop scanning, isScheduledStopped: " + isScheduledStopped);
                delayStart(scanMode.interval);
            }
        }, 1000 * 10);
    }

    private void delayStart(long delay) {
        Log.i(TAG, "Delay start");
        intervalController = new Timer();
        intervalController.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.i(TAG, "Delay start run, isScanning: " + isScanning + ", isScheduledStopped: " + isScheduledStopped);
                if (isScanning && isScheduledStopped) {
                    Log.i(TAG, "Delay start before scanning, isScheduledStopped: " + isScheduledStopped);
                    startScan(true);
                }
            }
        }, delay);
    }

    private void startScan(boolean isPerformedByScheduler) {
        if (!isPerformedByScheduler && isScanning) return;
        isScanning = true;
        Log.i(TAG, "Scan start");
        checkConverage();
        ble.scan(scanCallback);
        if (isPerformedByScheduler) isScheduledStopped = false;
        intervalControl();
    }

    private void stopScan(boolean isPerformedByScheduler) {
        convergeChecker.cancel();
        ble.stopScan(scanCallback);
        if (isPerformedByScheduler) isScheduledStopped = true;
        else {
            isScheduledStopped = false;
            isScanning = false;
        }
        intervalController.cancel();
        Log.i(TAG, "Scan stop");
    }

    //Services here
    public void setScanMode(SCAN_MODE nextScanMode) {
        this.scanMode = nextScanMode;
    }

    public SCAN_MODE getScanMode(){
        return scanMode;
    }

    public void startScan() {
        startScan(false);
    }

    public void stopScan() {
        stopScan(false);
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
            convergeChecker.cancel();
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
