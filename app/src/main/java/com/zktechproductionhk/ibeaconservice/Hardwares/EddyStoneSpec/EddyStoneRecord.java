package com.zktechproductionhk.ibeaconservice.Hardwares.EddyStoneSpec;

public class EddyStoneRecord {
    public String identifier;
    public String color;
    public int RSSI;

    public EddyStoneRecord(String identifier, String color, int RSSI) {
        this.identifier = identifier;
        this.color = color;
        this.RSSI = RSSI;
    }

    @Override
    public String toString() {
        return "EddyStoneRecord{" +
                "identifier='" + identifier + '\'' +
                ", color='" + color + '\'' +
                ", RSSI=" + RSSI +
                '}';
    }
}
