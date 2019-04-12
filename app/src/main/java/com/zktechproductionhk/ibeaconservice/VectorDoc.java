package com.zktechproductionhk.ibeaconservice;

import com.zktechproductionhk.ibeaconservice.Hardwares.EddyStoneSpec.EddyStone;
import com.zktechproductionhk.ibeaconservice.Hardwares.EddyStoneSpec.EddyStoneRecord;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;


public class VectorDoc {
    private HashMap<String, Double> doc;

    public VectorDoc() {
        this.doc = (HashMap<String, Double>) EddyStone.emptyRecord.clone();
    }

    public VectorDoc addRecord(EddyStoneRecord record) {
        doc.put(record.identifier, (double) record.RSSI);
        return this;
    }

    public double cosSim(VectorDoc vectorDoc) {
        double sim = 0;
        for (String beacon : vectorDoc.doc.keySet()) {
            if (doc.containsKey(beacon))
                sim += doc.get(beacon) * vectorDoc.doc.get(beacon);
        }
        return sim;
    }

    public VectorDoc normalize() {
        HashMap<String, Double> normalized = new HashMap<>();
        double docLen = 0;
        for (String key : doc.keySet())
            docLen += Math.pow(doc.get(key), 2);
        docLen = Math.sqrt(docLen);
        for (String key : doc.keySet())
            normalized.put(key, doc.get(key) / docLen);
        this.doc.clear();
        this.doc = (HashMap<String, Double>) normalized.clone();
        return this;
    }

    public int size() {
        return doc.size();
    }

    public boolean isEmpty() {
        return doc.size() == 0;
    }

    public double getCoverage() {
        int coverage = 0;
        for (Double val : doc.values())
            coverage += (val == 0 ? 0 : 1);
        return coverage / EddyStone.emptyRecord.size();
    }

    public Collection<Double> values() {
        return doc.values();
    }

    public Set<String> keys() {
        return doc.keySet();
    }

    @Override
    public String toString() {
        return "VectorDoc{" +
                "doc=" + doc +
                '}';
    }

    @Override
    protected VectorDoc clone() throws CloneNotSupportedException {
        VectorDoc vectorDoc = new VectorDoc();
        vectorDoc.doc.putAll(doc);
        return vectorDoc;
    }
}

