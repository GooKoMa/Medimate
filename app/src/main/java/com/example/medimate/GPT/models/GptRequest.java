package com.example.medimate.GPT.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GptRequest {
    @SerializedName("model")
    String model;

    @SerializedName("messages")
    List<Message> messages;

    public GptRequest(String model, List<Message> messages) {
        this.model = model;
        this.messages = messages;
    }
}
