package com.example.medimate.GPT;

import android.util.Log;
import com.google.gson.Gson;
import com.example.medimate.GPT.models.GptFullResponse; // (새 '틀' import)
import com.example.medimate.GPT.models.GptRequest;
import com.example.medimate.GPT.models.GptResponse;
import com.example.medimate.GPT.models.Message;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GptProcessor {

    // 1. (콜백 수정) 이제 'GptFullResponse' 객체 전체를 반환합니다.
    public interface GptCallback {
        void onSuccess(GptFullResponse responseData); // (수정)
        void onError(String errorMessage);
    }

    private GptApiService apiService;
    private Gson gson;

    public GptProcessor() {
        this.apiService = ApiClient.getApiService();
        this.gson = new Gson();
    }

    // 2. (함수 이름 원복) processText 함수 하나만 사용합니다.
    public void processText(String rawText, String apiKey, GptCallback callback) {
        Log.d("GptProcessor", "Sending text to gpt-4o-mini...");

        // 3. (지시서 수정) '대용량 JSON'을 요청하는 강력한 프롬프트
        String prompt = "You are an assistant that processes OCR text from a medicine bag. " +
                "Your *only* output must be a single, valid JSON object. " +
                "Do not add *any* explanatory text before or after the JSON. \n" +
                "OCR Text: \n" + rawText + "\n\n" +
                "Respond with this exact JSON structure. Fill *all* 5 fields for *each* drug. " +
                "Separate common instructions.\n" +
                "{\n" +
                "  \"drugs\": [\n" +
                "    {\n" +
                "      \"name\": \"(약 이름)\",\n" +
                "      \"appearance\": \"(생김새, 예: '흰색 원형 정제')\",\n" +
                "      \"description\": \"(약 설명, 예: '기침, 가래 완화제')\",\n" +
                "      \"dosage\": \"(복약안내, 예: '1일 3회, 1포씩 식후 30분')\",\n" +
                "      \"storage\": \"(보관방법, 예: '실온 보관')\",\n" +
                "      \"warning\": \"(개별 주의사항, 예: '졸음, 운전 주의')\"\n" +
                "    },\n" +
                "    { ... (다음 약) ... }\n" +
                "  ],\n" +
                "  \"common_instructions\": \"(모든 약에 공통되는 보관법이나 복약 시간)\"\n" +
                "}";

        Message userMessage = new Message("user", prompt);

        // 4. (모델 변경) gpt-4o-mini 사용
        GptRequest request = new GptRequest(
                "gpt-4o-mini",
                java.util.Collections.singletonList(userMessage)
        );
        // (JSON 모드를 쓰려면 GptRequest도 수정해야 하지만, 일단 프롬프트로 시도)

        apiService.getChatCompletion(apiKey, request).enqueue(new Callback<GptResponse>() {
            @Override
            public void onResponse(Call<GptResponse> call, Response<GptResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        // 5. (파싱 수정) GPT 응답을 'GptFullResponse' 틀에 맞게 파싱
                        String jsonResponse = response.body().choices.get(0).message.content;
                        GptFullResponse fullResponse =
                                gson.fromJson(jsonResponse, GptFullResponse.class);

                        if (fullResponse != null && fullResponse.getDrugs() != null) {
                            callback.onSuccess(fullResponse); // 6. (콜백 수정) 객체 통째로 전달
                        } else {
                            throw new Exception("JSON 파싱 실패 또는 drugs 필드 누락");
                        }
                    } catch (Exception e) {
                        Log.e("GptProcessor", "JSON Parsing Error", e);
                        callback.onError("GPT 응답을 분석하는 데 실패했습니다.");
                    }
                } else {
                    // (에러 처리)
                    callback.onError("API Call Failed: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<GptResponse> call, Throwable t) {
                // (에러 처리)
                callback.onError("API Connection Error: " + t.getMessage());
            }
        });
    }
}