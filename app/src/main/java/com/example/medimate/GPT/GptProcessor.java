package com.example.medimate.GPT;

import android.util.Log;

import com.example.medimate.GPT.models.GptRequest;
import com.example.medimate.GPT.models.GptResponse;
import com.example.medimate.GPT.models.Message;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GptProcessor {

    // 1. 작업 완료 후 MainActivity에 알릴 '설계도' 정의
    public interface GptCallback {
        void onSuccess(String processedText);
        void onError(String errorMessage);
    }

    private GptApiService apiService;

    public GptProcessor() {
        // 2. 생성될 때 API 공장을 통해 서비스 인스턴스를 가져옴
        this.apiService = ApiClient.getApiService();
    }

    // 3. '텍스트 처리' 기능 (외부에서 호출할 함수)
    public void processText(String rawText, String apiKey, GptCallback callback) {
        Log.d("GptProcessor", "Sending text to GPT: " + rawText);

        // 4. GPT에게 보낼 프롬프트(지시어) 구성
        String prompt = "다음은 약봉투에서 OCR로 추출한 텍스트입니다. " +
                "이 텍스트를 바탕으로 [약 이름], [복용법], [주의사항] 항목으로 " +
                "명확하게 구분하여 요약해 주세요. 만약 인식이 불명확한 부분이 있다면 " +
                "'인식이 불명확합니다'라고 표기해 주세요. \n\n" +
                "추출된 텍스트: \n" + rawText;

        // 5. API 요청 객체 생성
        Message userMessage = new Message("user", prompt);
        GptRequest request = new GptRequest("gpt-3.5-turbo", java.util.Collections.singletonList(userMessage));

        // 6. API 호출 (비동기 방식)
        apiService.getChatCompletion(apiKey, request).enqueue(new Callback<GptResponse>() {
            @Override
            public void onResponse(Call<GptResponse> call, Response<GptResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    GptResponse gptResponse = response.body();
                    if (gptResponse.choices != null && !gptResponse.choices.isEmpty()) {
                        // 7. 성공 시 콜백으로 결과 전달
                        String processedText = gptResponse.choices.get(0).message.content;
                        Log.d("GptProcessor", "Processed text: " + processedText);
                        callback.onSuccess(processedText);
                    } else {
                        Log.e("GptProcessor", "Response successful, but no choices found.");
                        callback.onError("No response choices found.");
                    }
                } else {
                    Log.e("GptProcessor", "API Call Failed: " + response.code() + " " + response.message());
                    callback.onError("API Call Failed: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<GptResponse> call, Throwable t) {
                // 8. 실패 시 콜백으로 에러 전달
                Log.e("GptProcessor", "API Call Error: ", t);
                callback.onError("API Connection Error: " + t.getMessage());
            }
        });
    }
}
