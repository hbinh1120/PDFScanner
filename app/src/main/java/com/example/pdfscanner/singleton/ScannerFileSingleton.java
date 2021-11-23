package com.example.pdfscanner.singleton;

import android.content.Context;

import com.example.pdfscanner.model.ScannerFile;

import java.util.ArrayList;
import java.util.List;

public class ScannerFileSingleton {
    private static ScannerFileSingleton formSingleton;
    private List<ScannerFile> scannerFiles;

    public ScannerFileSingleton(Context context) {
        scannerFiles = new ArrayList<>();
    }

    public static ScannerFileSingleton get(Context context) {
        if (formSingleton == null) {
            formSingleton = new ScannerFileSingleton(context);
        }
        return formSingleton;
    }

    public void put(List<ScannerFile> scannerFiles) {
        if (scannerFiles!=null && scannerFiles.size()>0) {
            this.scannerFiles = scannerFiles;
        }
    }

    public void delete(int id) {
        int index = -1;
        for (int i=0; i<scannerFiles.size(); i++) {
            if (scannerFiles.get(i).getId()==id) {
                index = i;
                break;
            }
        }
        if (index!=-1) scannerFiles.remove(index);
    }

    public void update(ScannerFile s) {
        for (int i=0; i<scannerFiles.size(); i++) {
            if (scannerFiles.get(i).getId()==s.getId()) {
                scannerFiles.set(i,s);
                break;
            }
        }
    }

    public List<ScannerFile> getScannerFiles() {
        return scannerFiles;
    }

    public ScannerFile getScannerFile(int id) {
        for (ScannerFile s : scannerFiles) {
            if (s.getId()==id) {
                return s;
            }
        }
        return new ScannerFile("null","null","null","null");
    }

}
