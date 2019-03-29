package com.zktechproductionhk.ibeaconservice;

import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.AsyncTask;
import android.util.Log;

import com.zktechproductionhk.ibeaconservice.Hardwares.EddyStoneSpec.EddyStone;
import com.zktechproductionhk.ibeaconservice.Hardwares.EddyStoneSpec.EddyStoneRecord;
import com.zktechproductionhk.ibeaconservice.Hardwares.Sensors;

import java.util.ArrayList;

public class ScanningTask extends AsyncTask<Void, Void, Void> {
    public interface LifeCycle {
        void start();

        void waiting();

        void startScan();

        void progress(int progress);

        void stopScan();

        void finish(ArrayList<VectorDoc> vectorDocs);

        void error(String error);
    }

    private static final String TAG = "[ScanningTask]";
    private Sensors sensors;
    private int duration;
    private VectorDoc buffer;
    private ArrayList<VectorDoc> vectorDocs;
    private LifeCycle lifeCycle;
    private ScanCallback scanCallback;
    private Integer scanResolution, scanDelay;

    public ScanningTask(Sensors sensors, int duration, LifeCycle lifeCycle) {
        this.sensors = sensors;
        this.duration = duration;
        this.vectorDocs = new ArrayList<>();
        this.lifeCycle = lifeCycle;

        this.scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                if (result.getScanRecord().getServiceUuids() != null) {
                    String stone = EddyStone.getStone(result.getScanRecord().getBytes());
                    if (stone != null) {
                        String[] stoneInfo = stone.split("_");
                        buffer.addRecord(new EddyStoneRecord(stoneInfo[0], stoneInfo[1], result.getRssi()));
                    }
                }
            }
        };
    }

    void scan() {
        sensors.scan(scanCallback);
    }

    void stopScan() {
        sensors.stopScan(scanCallback);
    }

    void resetBuffer() {
        buffer = new VectorDoc();
    }

    @Override
    protected void onPreExecute() {
        resetBuffer();
        lifeCycle.start();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        int curSecond = 0;
        try {
            scan();
            lifeCycle.waiting();
            Thread.sleep(scanDelay);
            lifeCycle.startScan();
            while (curSecond++ < duration) {
                Thread.sleep((this.scanResolution == null ? 500 : this.scanResolution));
                lifeCycle.progress(curSecond);
                if (!buffer.isEmpty())
                    vectorDocs.add(buffer);
                resetBuffer();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            lifeCycle.error(e.getMessage());
        } finally {
            stopScan();
            lifeCycle.stopScan();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        for (VectorDoc doc : vectorDocs)
            Log.d(TAG, "Doc: " + doc.toString() + ", length: " + doc.size());
        lifeCycle.finish(vectorDocs);
    }
}
