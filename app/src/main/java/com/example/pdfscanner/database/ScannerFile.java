package com.example.pdfscanner.database;

import java.util.Date;
import java.util.UUID;

public class ScannerFile {
    private UUID mId;
    private String mTitle;
    private Date mDate;
    private String mType;

    public ScannerFile() {
        this.mId = UUID.randomUUID();
        mDate = new Date();
    }

    public ScannerFile(UUID id) {
        this.mId = id;
        mDate = new Date();
    }

    public UUID getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date mDate) {
        this.mDate = mDate;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        this.mType = type;
    }

}
