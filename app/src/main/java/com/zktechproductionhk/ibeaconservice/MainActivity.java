package com.zktechproductionhk.ibeaconservice;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.button.MaterialButton;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "[MAIN]";

    private BLEService bleService;
    private BLEServiceBindController bleSerBindController;

    private FloatingActionButton fab;
    private MaterialButton btnGetMeasureResult;

    private TextView txtScanMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bleSerBindController = new BLEServiceBindController(this) {
            @Override
            public void OnBLEServiceBind(BLEService bleService) {
                MainActivity.this.bleService = bleService;
                MainActivity.this.bleService.setCheckCoverageInterval(1000 * 5);
            }

            @Override
            public void OnHealthStateChanged(BLEService.HEALTH_STATE healthState) {
                runOnUiThread(() -> btnGetMeasureResult.setEnabled(healthState == BLEService.HEALTH_STATE.BAD ? false : true));
            }
        };
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(this);

        btnGetMeasureResult = findViewById(R.id.btnGetMeasureResult);
        btnGetMeasureResult.setOnClickListener(this);
        btnGetMeasureResult.setEnabled(false);

        txtScanMode = findViewById(R.id.txtScanMode);
        txtScanMode.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        bleSerBindController.bindService();
    }

    @Override
    protected void onStop() {
        super.onStop();
        bleSerBindController.unBindService();
    }

    @Override
    public void onClick(View v) {
        if (v == fab) {
            if (bleService.isScanning()) {
                fab.setImageDrawable(getResources().getDrawable(R.drawable.bluetooth_off));
                bleService.stopScan();
            } else {
                fab.setImageDrawable(getResources().getDrawable(R.drawable.bluetooth));
                bleService.startScan();
            }
        } else if (v == btnGetMeasureResult) {
            VectorDoc doc = bleService.getVectorDoc();
            Log.i(TAG, "DOC: " + doc);
        } else if(v == txtScanMode) {
            if (bleService.getScanMode() == BLEService.SCAN_MODE.REGULAR) {
                fab.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                bleService.setScanMode(BLEService.SCAN_MODE.INTENSIVE);
                txtScanMode.setText("Intensive");
            } else {
                fab.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                bleService.setScanMode(BLEService.SCAN_MODE.REGULAR);
                txtScanMode.setText("Regular");
            }
        }
    }
}
