package com.example.medimate.OCR;

public class DetailItem {
    private String title;
    private String content;

    public DetailItem(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public String getTitle() { return title; }
    public String getContent() { return content; }
}
