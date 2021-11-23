package com.example.pdfscanner.model;

public class UserChange {
    private String username;
    private String oldpassword;
    private String newpassword;

    public UserChange(String username, String oldpassword, String newpassword) {
        this.username = username;
        this.oldpassword = oldpassword;
        this.newpassword = newpassword;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getOldpassword() {
        return oldpassword;
    }

    public void setOldpassword(String oldpassword) {
        this.oldpassword = oldpassword;
    }

    public String getNewpassword() {
        return newpassword;
    }

    public void setNewpassword(String newpassword) {
        this.newpassword = newpassword;
    }
}
