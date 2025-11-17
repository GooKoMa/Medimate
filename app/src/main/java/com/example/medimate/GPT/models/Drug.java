package com.example.medimate.GPT.models;

import java.io.Serializable;

public class Drug implements Serializable {

    private String name;
    private String appearance;
    private String description;
    private String dosage;
    private String storage;
    private String warning;

    public Drug() {}

    public String getName() { return name; }
    public String getAppearance() { return appearance; }
    public String getDescription() { return description; }
    public String getDosage() { return dosage; }
    public String getStorage() { return storage; }
    public String getWarning() { return warning; }

    // setter가 필요하다면 추가
}
