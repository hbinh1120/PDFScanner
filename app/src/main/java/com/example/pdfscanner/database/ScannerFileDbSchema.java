package com.example.pdfscanner.database;

public class ScannerFileDbSchema {
    public static final class FileTable {
        public static final String NAME = "scanner_files";
        public static final class Cols {
            public static final String UUID = "uuid";
            public static final String TITLE = "title";
            public static final String TYPE = "type";
            public static final String DATE = "date_created";
        }
    }
}
