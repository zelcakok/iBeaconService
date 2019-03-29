package com.zktechproductionhk.ibeaconservice.Hardwares;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

public class Permission extends Activity {
    public interface OnPermissionGranted {
        void granted();

        void rejected();
    }

    public static final int REQUEST = 100;

    private OnPermissionGranted onPermissionGranted;
    private static Permission instance;

    private Permission() {
    }

    public static Permission getInstance() {
        if (instance == null) instance = new Permission();
        return instance;
    }

    public static boolean isGranted(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isAllGranted(Context context) {
        return isGranted(context, Manifest.permission.BLUETOOTH) &&
                isGranted(context, Manifest.permission.BLUETOOTH_ADMIN) &&
                isGranted(context, Manifest.permission.ACCESS_FINE_LOCATION) &&
                isGranted(context, Manifest.permission.ACCESS_COARSE_LOCATION) &&
                isGranted(context, Manifest.permission.INTERNET);
    }

    /*
        May split to individual with REQUEST_CODE
     */
    public void request(Activity activity, OnPermissionGranted onPermissionGranted) {
        this.onPermissionGranted = onPermissionGranted;
        ActivityCompat.requestPermissions(activity,
                new String[]{
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.INTERNET
                },
                REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            onPermissionGranted.granted();
        else onPermissionGranted.rejected();
    }
}

