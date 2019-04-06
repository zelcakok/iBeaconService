package com.zktechproductionhk.ibeaconservice;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.button.MaterialButton;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity implements View.OnClickListener, BLEService.OnHealthStateChangeListener, BLEService.OnBLEScanListener {
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
                MainActivity.this.bleService.setOnHealthStateChangeListener(MainActivity.this);
                MainActivity.this.bleService.setOnBLEScanListener(MainActivity.this);
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
                bleService.stopScan();
                runOnUiThread(() -> {
                    btnGetMeasureResult.setEnabled(false);
                    bleService.resetHealthState();
                });
            } else {
                bleService.startScan();
            }
        } else if (v == btnGetMeasureResult) {
            VectorDoc doc = bleService.getVectorDoc();
            Log.i(TAG, "DOC: " + doc);
        } else if (v == txtScanMode) {
            if (bleService.getScanMode() == BLEService.SCAN_MODE.REGULAR) {
                bleService.setScanMode(BLEService.SCAN_MODE.INTENSIVE);
                txtScanMode.setText("Intensive");
            } else {
                bleService.setScanMode(BLEService.SCAN_MODE.REGULAR);
                txtScanMode.setText("Regular");
            }
        }
    }

    @Override
    public void change(BLEService.HEALTH_STATE healthState) {
        runOnUiThread(() -> btnGetMeasureResult.setEnabled(true));
    }

    @Override
    public void onBLEScanStart() {
        runOnUiThread(() -> fab.setImageDrawable(getResources().getDrawable(R.drawable.bluetooth)));
    }

    @Override
    public void onBLEScanStop() {
        runOnUiThread(() -> fab.setImageDrawable(getResources().getDrawable(R.drawable.bluetooth_off)));
    }
}
