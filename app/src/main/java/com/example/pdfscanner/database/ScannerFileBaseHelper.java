package com.example.pdfscanner.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.pdfscanner.database.ScannerFileDbSchema.FileTable;
import androidx.annotation.Nullable;

public class ScannerFileBaseHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "pdfScanner.db";

    public ScannerFileBaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + FileTable.NAME + "(" + "_id integer primary key autoincrement, " + FileTable.Cols.UUID
        + ", " + FileTable.Cols.TITLE + ", " + FileTable.Cols.TYPE + ", " + FileTable.Cols.DATE + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
