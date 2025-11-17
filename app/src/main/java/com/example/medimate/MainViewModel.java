package com.example.medimate;

import android.app.Application;
import android.graphics.Bitmap;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.medimate.GPT.GptProcessor;
import com.example.medimate.GPT.models.GptFullResponse; // (새 '틀' import)
import com.example.medimate.OCR.OcrProcessor;
import com.example.medimate.TTS.TTSManager;

public class MainViewModel extends AndroidViewModel {

    // --- 1. 전문가들 ---
    private OcrProcessor ocrProcessor;
    private GptProcessor gptProcessor;
    private TTSManager ttsManager;
    private static final String OPENAI_API_KEY = "Bearer " + BuildConfig.OPENAI_API_KEY;

    // --- 2. LiveData (UI 상태) ---
    // (수정) '대용량 JSON' 전체를 보관할 LiveData
    private MutableLiveData<GptFullResponse> drugDataLiveData = new MutableLiveData<>();
    // (새로 추가) 로딩 중인지 상태를 알릴 LiveData
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    // --- 3. LiveData Getters (MainActivity가 '관찰'할) ---
    public LiveData<GptFullResponse> getDrugDataLiveData() {
        return drugDataLiveData;
    }
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    // --- 4. 생성자 ---
    public MainViewModel(@NonNull Application application) {
        super(application);
        ocrProcessor = new OcrProcessor();
        gptProcessor = new GptProcessor();
        ttsManager = new TTSManager(application.getApplicationContext());
    }

    // --- 5. 작업 흐름 ---
    public void startImageProcessing(Bitmap bitmap) {
        isLoading.postValue(true); // 1. 로딩 시작
        drugDataLiveData.postValue(null); // (선택) 이전 결과 지우기

        ocrProcessor.processBitmap(bitmap, new OcrProcessor.OcrCallback() {
            @Override
            public void onSuccess(String rawText) {
                Log.d("MainViewModel", "OCR Success.");
                // 2. OCR 성공 -> GPT 전문가에게 텍스트 전달
                gptProcessor.processText(rawText, OPENAI_API_KEY, new GptProcessor.GptCallback() {

                    // 3. (콜백 수정) '대용량 JSON' 객체를 통째로 받음
                    @Override
                    public void onSuccess(GptFullResponse responseData) {
                        Log.d("MainViewModel", "GPT Success. Drugs found: " + responseData.getDrugs().size());
                        drugDataLiveData.postValue(responseData); // 4. LiveData에 데이터 저장
                        isLoading.postValue(false); // 5. 로딩 끝
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Log.e("MainViewModel", "GPT Error: " + errorMessage);
                        isLoading.postValue(false); // 5. 로딩 끝 (실패)
                        // (에러 처리 LiveData를 만들 수도 있음)
                        ttsManager.speak("분석에 실패했습니다. " + errorMessage);
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("MainViewModel", "OCR Error: " + errorMessage);
                isLoading.postValue(false); // 5. 로딩 끝 (실패)
                ttsManager.speak("글자 인식에 실패했습니다. " + errorMessage);
            }
        });
    }

    // --- 6. (새 함수) 팝업 버튼 클릭 시 TTS 실행 ---
    /**
     * MainActivity의 팝업 버튼이 이 함수를 호출합니다.
     * @param textToSpeak (예: drug.getDosage())
     */
    public void speakText(String textToSpeak) {
        if (textToSpeak != null && !textToSpeak.isEmpty()) {
            ttsManager.speak(textToSpeak);
        } else {
            ttsManager.speak("정보가 없습니다.");
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (ttsManager != null) {
            ttsManager.shutdown();
        }
    }
}