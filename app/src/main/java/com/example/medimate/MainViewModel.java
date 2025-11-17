package com.example.medimate;

import android.app.Application;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

// 전문가 클래스 import
import com.example.medimate.OCR.OcrProcessor;
import com.example.medimate.GPT.GptProcessor;
import com.example.medimate.TTS.TTSManager;

public class MainViewModel extends AndroidViewModel {

    // --- 1. 전문가들 (이제 ViewModel이 직접 소유) ---
    private OcrProcessor ocrProcessor;
    private GptProcessor gptProcessor;
    private TTSManager ttsManager;

    // --- 2. API 키 (ViewModel이 관리) ---
    private static final String OPENAI_API_KEY = "Bearer " + BuildConfig.OPENAI_API_KEY;

    // --- 3. 생성자 (전문가 고용) ---
    // (TTSManager는 Application Context가 필요해서 AndroidViewModel을 상속받음)
    public MainViewModel(@NonNull Application application) {
        super(application);

        // '작업 반장'이 생성될 때 전문가들을 고용
        ocrProcessor = new OcrProcessor();
        gptProcessor = new GptProcessor();
        ttsManager = new TTSManager(application.getApplicationContext());
    }

    // --- 4. 작업 흐름 (MainActivity에서 옮겨온 로직) ---

    /**
     * MainActivity가 호출할 유일한 작업 시작 메소드
     */
    public void startImageProcessing(Bitmap bitmap) {
        // 1단계: OCR 실행
        runOcr(bitmap);
    }

    /**
     * 1단계: OCR 전문가에게 작업 지시
     */
    private void runOcr(Bitmap bitmap) {
        ocrProcessor.processBitmap(bitmap, new OcrProcessor.OcrCallback() {
            @Override
            public void onSuccess(String rawText) {
                Log.d("MainViewModel", "OCR Success. Raw text: " + rawText);

                // 2단계: GPT 실행
                runGptPostProcessing(rawText);
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("MainViewModel", "OCR Error: " + errorMessage);
                ttsManager.speak("글자 인식에 실패했습니다. 다시 시도해 주세요.");
            }
        });
    }

    /**
     * 2단계: GPT 전문가에게 작업 지시
     */
    private void runGptPostProcessing(String rawText) {
        gptProcessor.processText(rawText, OPENAI_API_KEY, new GptProcessor.GptCallback() {
            @Override
            public void onSuccess(String processedText) {
                Log.d("MainViewModel", "GPT Success. Processed text: " + processedText);

                // 3단계: TTS 실행
                ttsManager.speak(processedText);

                // (만약 Toast 메시지 등을 MainActivity에 보내고 싶다면
                //  여기에 LiveData를 사용해서 값을 설정합니다.)
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("MainViewModel", "GPT Error: " + errorMessage);
                ttsManager.speak("내용을 요약하는 데 실패했습니다. 원본 텍스트를 읽어드립니다.");
                ttsManager.speak(rawText); // 원본 읽기
            }
        });
    }

    // --- 5. 앱 종료 시 자원 해제 ---
    @Override
    protected void onCleared() {
        super.onCleared();
        // ViewModel이 파괴될 때 (예: 앱 종료) TTS 자원을 안전하게 해제
        if (ttsManager != null) {
            ttsManager.shutdown();
        }
    }
}