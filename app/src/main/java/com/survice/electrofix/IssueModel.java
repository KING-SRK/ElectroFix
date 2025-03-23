package com.survice.electrofix;

public class IssueModel {
    private String id, userName, userEmail, message, type, screenshotUrl;

    public IssueModel() {}

    public IssueModel(String id, String userName, String userEmail, String message, String type, String screenshotUrl) {
        this.id = id;
        this.userName = userName;
        this.userEmail = userEmail;
        this.message = message;
        this.type = type;
        this.screenshotUrl = screenshotUrl;
    }

    // Getter Methods
    public String getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }

    public String getScreenshotUrl() {
        return screenshotUrl;
    }

    // Setter Methods (If needed)
    public void setId(String id) {
        this.id = id;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setScreenshotUrl(String screenshotUrl) {
        this.screenshotUrl = screenshotUrl;
    }
}