package com.example.pdfscanner.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.pdfscanner.Form;
import com.example.pdfscanner.FormSingleton;
import com.example.pdfscanner.database.ScannerFileDbSchema.FileTable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ScannerFileLab {
    private static ScannerFileLab scannerFileLab;
    private Context context;
    private SQLiteDatabase database;

    private ScannerFileLab(Context context){
        this.context = context.getApplicationContext();
        this.database = new ScannerFileBaseHelper(context).getWritableDatabase();
    }
    public void addScannerFile(ScannerFile s) {
        ContentValues values = getContentValues(s);
        database.insert(FileTable.NAME,null,values);
    }


    public static ScannerFileLab get(Context context) {
        if (scannerFileLab == null) {
            scannerFileLab = new ScannerFileLab(context);
        }
        return scannerFileLab;
    }

    public void updateScannerFile(ScannerFile s) {
        String uuidString = s.getId().toString();
        ContentValues values = getContentValues(s);
        database.update(FileTable.NAME, values, FileTable.Cols.UUID + "= ?",new String[] {uuidString});
    }

    public List<ScannerFile> getScannerFiles() {
        List<ScannerFile> scannerFiles = new ArrayList<>();

        ScannerFileCursorWrapper cursorWrapper = queryScannerFiles(null,null);
        try {
            cursorWrapper.moveToFirst();
            while (!cursorWrapper.isAfterLast()) {
                scannerFiles.add(cursorWrapper.getScannerFile());
                cursorWrapper.moveToNext();
            }
        } finally {
            cursorWrapper.close();
        }

        return scannerFiles;
    }

    public List<ScannerFile> searchScannerFiles(String title) {
        List<ScannerFile> scannerFiles = new ArrayList<>();

        ScannerFileCursorWrapper cursorWrapper = queryScannerFiles(FileTable.Cols.TITLE+" LIKE ?",new String[]{"%"+title+"%"});
        try {
            cursorWrapper.moveToFirst();
            while (!cursorWrapper.isAfterLast()) {
                scannerFiles.add(cursorWrapper.getScannerFile());
                cursorWrapper.moveToNext();
            }
        } finally {
            cursorWrapper.close();
        }

        return scannerFiles;
    }

    public boolean checkTitle(String file_name) {
        ScannerFileCursorWrapper cursorWrapper = queryScannerFiles(FileTable.Cols.TITLE + "= ?", new String[]{file_name});
        if (cursorWrapper.getCount()==0) {
            return true;
        } else {
            return false;
        }
    }

    public void removeScannerFile(ScannerFile s) {
        String uuidString = s.getId().toString();
        database.delete(FileTable.NAME, FileTable.Cols.UUID + "= ?", new String[]{uuidString});
    }

    public ScannerFile getScannerFile(UUID id) {
        ScannerFileCursorWrapper cursorWrapper = queryScannerFiles(FileTable.Cols.UUID + "= ?", new String[]{id.toString()});
        try {
            if (cursorWrapper.getCount() == 0) {
                return null;
            }
            cursorWrapper.moveToFirst();
            return cursorWrapper.getScannerFile();
        } finally {
            cursorWrapper.close();
        }
    }

    private ScannerFileCursorWrapper queryScannerFiles(String whereClause, String[] whereArgs) {
        Cursor cursor = database.query(FileTable.NAME, null, whereClause, whereArgs, null,null,null);
        return new ScannerFileCursorWrapper(cursor);
    }

    private static ContentValues getContentValues(ScannerFile scannerFile){
        ContentValues values = new ContentValues();
        values.put(FileTable.Cols.UUID, scannerFile.getId().toString());
        values.put(FileTable.Cols.TITLE, scannerFile.getTitle());
        values.put(FileTable.Cols.TYPE, scannerFile.getType());
        values.put(FileTable.Cols.DATE, scannerFile.getDate().getTime());
        return values;
    }
}
