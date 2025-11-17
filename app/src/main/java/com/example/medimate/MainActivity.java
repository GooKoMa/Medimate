package com.example.medimate;

// ... (Android 기본 import: Bitmap, Uri, Button 등) ...
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

// ... (AndroidX import: AppCompatActivity, EdgeToEdge 등) ...
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

// ↓↓↓ ViewModelProvider import (새로 추가) ↓↓↓
import androidx.lifecycle.ViewModelProvider;

import java.io.IOException;

// ... (HealthInputActivity import) ...
import com.example.medimate.recommendation.HealthInputActivity;

// ↓↓↓ 전문가 import가 모두 제거됨 ↓↓↓
// import com.example.medimate.OCR.OcrProcessor; // (제거)
// import com.example.medimate.GPT.GptProcessor; // (제거)
// import com.example.medimate.TTS.TTSManager;    // (제거)
// import com.example.medimate.BuildConfig;       // (제거)

// ↓↓↓ 권한 관련 import (카메라 권한 체크용) ↓↓↓
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;


public class MainActivity extends AppCompatActivity {

    // --- 멤버 변수 선언 ---

    // 1. (새로 추가) '작업 반장' ViewModel
    private MainViewModel viewModel;

    // 2. UI 요소
    private Button cameraButton;
    private Button galleryButton;

    // 3. Activity 런처 (카메라/갤러리/권한)
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<String> galleryLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    // 4. 데이터 (이미지 Uri)
    private Uri imageUri;

    // (전문가 변수 및 API 키가 모두 제거됨)

    // --- 생명 주기 메소드 ---

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ... (EdgeToEdge, setContentView, setOnApplyWindowInsetsListener) ...
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // --- 1. '작업 반장' 고용 (ViewModel 초기화) ---
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // --- 2. 기존 영양제 추천 버튼 (유지) ---
        Button recommendButton = findViewById(R.id.recommendButton);
        recommendButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HealthInputActivity.class);
            startActivity(intent);
        });

        // (전문가들 '고용' 코드 모두 제거됨)

        // --- 3. 런처 '준비' (카메라, 갤러리, 권한) ---
        setupCameraLauncher();
        setupGalleryLauncher();
        setupPermissionLauncher(); // (권한 런처 이름 변경)

        // --- 4. UI '연결' 및 작업 지시 ---
        cameraButton = findViewById(R.id.camera_button);
        cameraButton.setOnClickListener(v -> checkCameraPermissionAndLaunch());

        galleryButton = findViewById(R.id.gallery_button);
        galleryButton.setOnClickListener(v -> launchGallery());
    }

    @Override
    protected void onDestroy() {
        // (TTSManager.shutdown() 코드 제거됨 - ViewModel이 알아서 함)
        super.onDestroy();
    }


    // --- 핵심 로직 (UI 담당) ---

    /**
     * 공통 이미지 처리 로직
     * Uri -> Bitmap 변환 후, '작업 반장'에게 작업 지시
     */
    private void processImageUri(Uri uri) {
        if (uri == null) { /* ... */ return; }

        try {
            // 1. Uri -> Bitmap 변환
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);

            // 2. '작업 반장'에게 비트맵을 넘기며 "작업 시작!" 지시
            viewModel.startImageProcessing(bitmap);

            Toast.makeText(this, "분석을 시작합니다...", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            Log.e("MAIN_ACTIVITY", "Failed to load bitmap from Uri", e);
            Toast.makeText(this, "사진을 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 1. 카메라 런처 설정 (결과를 processImageUri로 넘김)
     */
    private void setupCameraLauncher() {
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        processImageUri(imageUri);
                    } else {
                        Log.w("MAIN_ACTIVITY", "Camera capture cancelled");
                    }
                }
        );
    }

    /**
     * 2. 갤러리 런처 설정 (결과를 processImageUri로 넘김)
     */
    private void setupGalleryLauncher() {
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        processImageUri(uri);
                    } else {
                        Log.w("MAIN_ACTIVITY", "Gallery selection cancelled");
                    }
                }
        );
    }

    /**
     * 3. 권한 런처 설정
     */
    private void setupPermissionLauncher() {
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        launchCamera(); // 허락 받으면 카메라 실행
                    } else {
                        Toast.makeText(this, "카메라 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    // --- (runOcr, runGptPostProcessing 메소드 '완전 삭제') ---


    // --- 유틸리티 메소드 (헬퍼) ---

    /**
     * 카메라 권한 확인 및 실행
     */
    private void checkCameraPermissionAndLaunch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    /**
     * 카메라 앱을 실행합니다. (수정 없음)
     */
    private void launchCamera() {
        imageUri = createImageUri();
        if (imageUri == null) { /* ... */ return; }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraLauncher.launch(intent);
    }

    /**
     * 갤러리 앱을 실행합니다. (수정 없음)
     */
    private void launchGallery() {
        galleryLauncher.launch("image/*");
    }

    /**
     * 이미지 Uri를 생성합니다. (수정 없음)
     */
    private Uri createImageUri() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Medimate_Capture_" + System.currentTimeMillis());
        values.put(MediaStore.Images.Media.DESCRIPTION, "From Medimate App");
        return getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }
}