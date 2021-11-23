package com.example.pdfscanner.model;

import java.util.Date;

public class ScannerFile {
    private int id;
    private String file_name;
    private String file_type;
    private String file_url;
    private String date_created;

    public ScannerFile(String file_name, String file_type, String file_url, String date_created) {
        this.file_name = file_name;
        this.file_type = file_type;
        this.file_url = file_url;
        this.date_created = date_created;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public String getFile_type() {
        return file_type;
    }

    public void setFile_type(String file_type) {
        this.file_type = file_type;
    }

    public String getFile_url() {
        return file_url;
    }

    public void setFile_url(String file_url) {
        this.file_url = file_url;
    }

    public String getDate_created() {
        return date_created;
    }

    public void setDate_created(String date_created) {
        this.date_created = date_created;
    }
}
