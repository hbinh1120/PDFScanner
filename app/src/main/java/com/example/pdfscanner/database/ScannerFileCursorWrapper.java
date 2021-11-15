package com.example.pdfscanner.database;

import android.database.Cursor;
import android.database.CursorWrapper;
import com.example.pdfscanner.database.ScannerFileDbSchema.FileTable;

import java.util.Date;
import java.util.UUID;

public class ScannerFileCursorWrapper extends CursorWrapper {

    /**
     * Creates a cursor wrapper.
     *
     * @param cursor The underlying cursor to wrap.
     */
    public ScannerFileCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public ScannerFile getScannerFile() {
        String uuidString = getString(getColumnIndex(FileTable.Cols.UUID));
        String title = getString(getColumnIndex(FileTable.Cols.TITLE));
        long date = getLong(getColumnIndex(FileTable.Cols.DATE));
        String type = getString(getColumnIndex(FileTable.Cols.TYPE));

        ScannerFile scannerFile = new ScannerFile(UUID.fromString(uuidString));
        scannerFile.setTitle(title);
        scannerFile.setDate(new Date(date));
        scannerFile.setType(type);
        return scannerFile;
    }
}
