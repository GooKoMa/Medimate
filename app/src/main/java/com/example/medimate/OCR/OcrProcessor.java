package com.example.medimate.OCR;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions;

public class OcrProcessor {

    // 1. 작업 완료 후 MainActivity에 알릴 '설계도' 정의
    public interface OcrCallback {
        void onSuccess(String rawText); // 성공 시 텍스트 전달
        void onError(String errorMessage); // 실패 시 메시지 전달
    }

    private TextRecognizer recognizer;

    public OcrProcessor() {
        // 2. 생성될 때 한글 인식기 초기화
        recognizer = TextRecognition.getClient(new KoreanTextRecognizerOptions.Builder().build());
    }

    // 3. '이미지 처리' 기능 (외부에서 호출할 함수)
    public void processBitmap(Bitmap bitmap, OcrCallback callback) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);

        recognizer.process(image)
                .addOnSuccessListener(visionText -> {
                    // 4. 성공하면 콜백으로 텍스트 전달
                    callback.onSuccess(visionText.getText());
                })
                .addOnFailureListener(e -> {
                    // 5. 실패하면 콜백으로 에러 전달
                    Log.e("OCR_PROC", "Text recognition failed", e);
                    callback.onError(e.getMessage());
                });
    }
}
