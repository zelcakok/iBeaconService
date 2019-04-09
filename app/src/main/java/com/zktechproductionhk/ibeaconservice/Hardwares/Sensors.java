package com.zktechproductionhk.ibeaconservice.Hardwares;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import com.zktechproductionhk.ibeaconservice.Hardwares.EddyStoneSpec.EddyStone;


public class Sensors extends Activity {
    public static final int REQUEST_BLE = 105;

    private static final String TAG = "[MAIN]";
    private static Sensors instance;
    private Permission permission;
    private Activity activity;
    private BluetoothAdapter bleAdapter;
    private BluetoothLeScanner bleScanner;

    public static Sensors getInstance(Activity activity) {
        if (instance == null) instance = new Sensors(activity);
        return instance;
    }

    private Sensors(Activity activity) {
        this.activity = activity;
        if (!isBLESupported()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("Error: No Bluetooth hardware")
                    .setMessage("This device doesn't support BLE.")
                    .setNegativeButton("Okay", (dialog, which) -> activity.finish())
                    .show();
        }
        final BluetoothManager bleMgr = (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
        bleAdapter = bleMgr.getAdapter();
        checkBLE();
    }

    boolean isBLESupported() {
        return activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    public void scan(ScanCallback callback) {
        if (bleScanner == null) checkBLE();
        else bleScanner.startScan(EddyStone.filters, EddyStone.settings, callback);
    }

    public void stopScan(ScanCallback callback) {
        bleScanner.stopScan(callback);
    }

    void checkBLE() {
        if (bleAdapter == null || !bleAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, REQUEST_BLE);
        } else {
            bleScanner = bleAdapter.getBluetoothLeScanner();
        }
    }

    public boolean isBLEEnabled() {
        return bleAdapter != null && bleAdapter.isEnabled();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_BLE)
            Log.d("[SENSOR]", "Result code: " + resultCode);
    }
}
