package com.zktechproductionhk.ibeaconservice.Hardwares.EddyStoneSpec;

import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.os.ParcelUuid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EddyStone {
    public static final String UUID = "0000fe9a-0000-1000-8000-00805f9b34fb";

    public static ArrayList<ScanFilter> filters = new ArrayList<ScanFilter>() {{
        new ScanFilter.Builder()
                .setServiceUuid(ParcelUuid.fromString(UUID)).build();
    }};

    public static ScanSettings settings =
            new ScanSettings.Builder()
//                    .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER | ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();

    public static Map<String, String> stones = new HashMap<String, String>() {{
        put("834DE4C872", "White");
        put("94E7C036D7", "White");
        put("0727F083BC", "White");
        put("1D6A13DE93", "White");
        put("013C196A73", "LightBlue");
        put("DBD1F3D350", "LightBlue");
        put("943912ED3D", "LightBlue");
        put("B80566F311", "LightBlue");
        put("4BD82129C6", "Green");
        put("C3925B28CF", "Green");
        put("5B75794CB2", "Green");
        put("B54B8479B5", "Green");
        put("66AB78F457", "DarkBlue");
        put("72098BE378", "DarkBlue");
        put("1A7A10469B", "DarkBlue");
        put("256748DC09", "DarkBlue");
    }};

    public static HashMap<String, Double> emptyRecord = new HashMap<String, Double>() {{
        put("834DE4C872", 0.0);
        put("94E7C036D7", 0.0);
        put("0727F083BC", 0.0);
        put("1D6A13DE93", 0.0);
        put("013C196A73", 0.0);
        put("DBD1F3D350", 0.0);
        put("943912ED3D", 0.0);
        put("B80566F311", 0.0);
        put("4BD82129C6", 0.0);
        put("C3925B28CF", 0.0);
        put("5B75794CB2", 0.0);
        put("B54B8479B5", 0.0);
        put("66AB78F457", 0.0);
        put("72098BE378", 0.0);
        put("1A7A10469B", 0.0);
        put("256748DC09", 0.0);
    }};

    public static String getStone(byte[] bytes) {
        String id = toHex(bytes).substring(24, 34);
        if (!stones.containsKey(id)) return null;
        return id + "_" + stones.get(id);
    }

    private static String toHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (byte b : bytes)
            builder.append(toHex(b));
        return builder.toString();
    }

    private static String toHex(byte b) {
        return ("" + "0123456789ABCDEF".charAt(0xf & b >> 4) + "0123456789ABCDEF".charAt(b & 0xf));
    }
}
