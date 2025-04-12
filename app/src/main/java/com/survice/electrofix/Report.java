package com.survice.electrofix;

public class Report {
    private String issueDescription;
    private String suggestion;
    private String screenshotUrl;

    public Report(String issueDescription, String suggestion) {
        this.issueDescription = issueDescription;
        this.suggestion = suggestion;
    }

    public String getIssueDescription() {
        return issueDescription;
    }

    public void setIssueDescription(String issueDescription) {
        this.issueDescription = issueDescription;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }

    public String getScreenshotUrl() {
        return screenshotUrl;
    }

    public void setScreenshotUrl(String screenshotUrl) {
        this.screenshotUrl = screenshotUrl;
    }
}