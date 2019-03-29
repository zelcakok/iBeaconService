package com.zktechproductionhk.ibeaconservice;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.zktechproductionhk.ibeaconservice.Hardwares.Permission;
import com.zktechproductionhk.ibeaconservice.Hardwares.Sensors;

public abstract class BLEServiceBindController implements Permission.OnPermissionGranted {
    public abstract void OnBLEServiceBind(BLEService bleService);

    public abstract void OnHealthStateChanged(BLEService.HEALTH_STATE healthState);

    private static final String TAG = "[BLESerBindCtler]";

    private Activity activity;
    private boolean isPermissionGranted = false;

    private boolean isBound = false;
    private BLEService bleService;
    private ServiceConnection serviceConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BLEService.BLEServiceBinder binder = (BLEService.BLEServiceBinder) service;
            bleService = binder.getService(ble, (BLEService.HEALTH_STATE healthState) -> OnHealthStateChanged(healthState));
            isBound = true;
            OnBLEServiceBind(bleService);
            Log.i(TAG, "BLE Service is bound: " + isBound);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    private Sensors ble;


    public BLEServiceBindController(Activity activity) {
        this.activity = activity;

        ble = Sensors.getInstance(activity);

        if (!Permission.isAllGranted(activity))
            Permission.getInstance().request(activity, this);
        else isPermissionGranted = true;
    }

    @Override
    public void granted() {
        isPermissionGranted = true;
    }

    @Override
    public void rejected() {
        isPermissionGranted = false;
    }

    public void bindService() {
        if (!isPermissionGranted) {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("Error: Permissions are not granted")
                    .setMessage("Please grant the required permissions first.")
                    .setNegativeButton("Okay", null)
                    .show();
            return;
        }
        Intent intent = new Intent(activity, BLEService.class);
        activity.bindService(intent, serviceConn, Context.BIND_AUTO_CREATE);
    }

    public void unBindService() {
        if (!isPermissionGranted) return;
        activity.unbindService(serviceConn);
        Log.i(TAG, "BLE Service is un-bound: " + isBound);
    }
}
