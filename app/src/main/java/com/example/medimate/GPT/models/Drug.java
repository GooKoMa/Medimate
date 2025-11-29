package com.example.medimate.GPT.models;

import com.google.gson.annotations.SerializedName;

public class Drug {

    @SerializedName("name") // 예: "코대원에스시럽"
    public String name;

    @SerializedName("appearance") // 예: "갈색 시럽"
    public String appearance;

    @SerializedName("description") // 예: "기침, 가래 완화제입니다."
    public String description;

    @SerializedName("dosage") // 예: "1일 3회, 1포씩 식후 30분..."
    public String dosage;

    @SerializedName("storage") // 예: "실온 보관"
    public String storage;

    @SerializedName("warning") // 예: "졸음, 운전 주의"
    public String warning;

    // (TTS용 getter 메소드들 - Activity에서 편하게 쓰기 위함)
    public String getName() { return name; }
    public String getAppearance() { return appearance; }
    public String getDescription() { return description; }
    public String getDosage() { return dosage; }
    public String getStorage() { return storage; }
    public String getWarning() { return warning; }
}
