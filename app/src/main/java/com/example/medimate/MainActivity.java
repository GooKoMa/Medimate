package com.example.medimate;

// --- Android 기본 import ---
import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;   // (오류 해결) LinearLayout
import android.widget.ProgressBar;  // (오류 해결) ProgressBar
import android.widget.Toast;

// --- AndroidX (UI, Activity, Lifecycle) import ---
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;  // (오류 해결) AlertDialog
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

// --- IO (파일 입출력) import ---
import java.io.IOException;

// --- Java 유틸리티 import ---
import java.util.List;  // (오류 해결) List

// --- 프로젝트의 다른 클래스 import ---
import com.example.medimate.GPT.models.Drug; // (오류 해결) Drug
// import com.example.medimate.recommendation.HealthInputActivity; // (삭제됨)


public class MainActivity extends AppCompatActivity {

    // --- 멤버 변수 선언 ---

    // 1. '작업 반장' ViewModel
    private MainViewModel viewModel;

    // 2. UI 요소
    private Button cameraButton;
    private Button galleryButton;
    // (recommendButton 변수 삭제됨)
    private ProgressBar progressBar;
    private LinearLayout drugIconLayout;

    // 3. Activity 런처 (카메라/갤러리/권한)
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<String> galleryLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    // 4. 데이터 (이미지 Uri)
    private Uri imageUri;


    // --- 생명 주기 메소드 ---

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- 기존 EdgeToEdge 코드 (유지) ---
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // --- 1. '작업 반장' 고용 (ViewModel 초기화) ---
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // --- 2. UI '연결' ---
        // (recommendButton 관련 코드 삭제됨)
        cameraButton = findViewById(R.id.camera_button);
        galleryButton = findViewById(R.id.gallery_button);
        progressBar = findViewById(R.id.progress_bar);
        drugIconLayout = findViewById(R.id.drug_icon_layout);

        // --- 3. 런처 '준비' (카메라, 갤러리, 권한) ---
        setupCameraLauncher();
        setupGalleryLauncher();
        setupPermissionLauncher();

        // --- 4. UI '연결' 및 작업 지시 ---
        // (recommendButton 리스너 삭제됨)
        cameraButton.setOnClickListener(v -> checkCameraPermissionAndLaunch());
        galleryButton.setOnClickListener(v -> launchGallery());

        // --- 5. '관찰' 시작 ---
        setupObservers();
    }

    // (onDestroy는 수정 없음 - ViewModel이 TTSManager.shutdown() 처리)


    // --- 핵심 로직 (UI 담당) ---

    /**
     * ViewModel의 LiveData를 '관찰'하는 함수
     */
    private void setupObservers() {
        // 1. 로딩 상태 관찰 (ProgressBar)
        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading) {
                progressBar.setVisibility(View.VISIBLE);
                drugIconLayout.setVisibility(View.GONE); // 아이콘 영역 숨기기
            } else {
                progressBar.setVisibility(View.GONE);
                drugIconLayout.setVisibility(View.VISIBLE); // 아이콘 영역 보이기
            }
        });

        // 2. '대용량 JSON' (약 목록) 관찰
        viewModel.getDrugDataLiveData().observe(this, responseData -> {
            if (responseData != null && responseData.getDrugs() != null) {
                updateDrugIcons(responseData.getDrugs()); // 아이콘(버튼) 생성

            } else {
                drugIconLayout.removeAllViews(); // 데이터가 null이면 아이콘 지우기
            }
        });
    }

    /**
     * 5. 약 목록을 받아 동적으로 버튼(아이콘)을 생성
     */
    private void updateDrugIcons(List<Drug> drugs) {
        drugIconLayout.removeAllViews(); // 기존 아이콘 모두 제거

        for (Drug drug : drugs) {
            Button drugButton = new Button(this);
            drugButton.setText(drug.getName()); // 버튼에 약 이름 설정

            drugButton.setOnClickListener(v -> {
                openDrugInfoDialog(drug); // 7. 팝업 띄우기
            });
            drugIconLayout.addView(drugButton); // 레이아웃에 버튼 추가
        }
    }

    /**
     * 7. 팝업(Dialog) 띄우기 (TTS 요청)
     */
    private void openDrugInfoDialog(Drug drug) {
        final CharSequence[] items = {"생김새", "약 설명", "복약안내", "보관방법", "주의사항"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(drug.getName()); // 팝업 제목: 약 이름

        builder.setItems(items, (dialog, index) -> {
            // 8. 팝업의 버튼을 누르면 -> ViewModel의 speakText 호출
            switch (index) {
                case 0: viewModel.speakText(drug.getAppearance()); break;
                case 1: viewModel.speakText(drug.getDescription()); break;
                case 2: viewModel.speakText(drug.getDosage()); break;
                case 3: viewModel.speakText(drug.getStorage()); break;
                case 4: viewModel.speakText(drug.getWarning()); break;
            }
        });
        builder.show();
    }


    /**
     * 공통 이미지 처리 로직 (Uri -> Bitmap -> ViewModel)
     */
    private void processImageUri(Uri uri) {
        if (uri == null) {
            Log.e("MAIN_ACTIVITY", "Received null Uri");
            Toast.makeText(this, "이미지를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // 1. Uri -> Bitmap 변환
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            // 2. '작업 반장'에게 비트맵을 넘기며 "작업 시작!" 지시
            viewModel.startImageProcessing(bitmap);

        } catch (IOException e) {
            Log.e("MAIN_ACTIVITY", "Failed to load bitmap from Uri", e);
            Toast.makeText(this, "사진을 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
        }
    }


    // --- 런처 설정 메소드들 (수정 없음) ---

    /**
     * 카메라 런처 설정
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
     * 갤러리 런처 설정
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
     * 권한 런처 설정
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


    // --- 유틸리티 메소드 (헬퍼) (수정 없음) ---

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
     * 카메라 앱을 실행합니다.
     */
    private void launchCamera() {
        imageUri = createImageUri();
        if (imageUri == null) {
            Toast.makeText(this, "저장 공간을 확보할 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraLauncher.launch(intent);
    }

    /**
     * 갤러리 앱을 실행합니다.
     */
    private void launchGallery() {
        galleryLauncher.launch("image/*");
    }

    /**
     * 촬영한 이미지를 저장할 빈 Uri를 생성합니다.
     */
    private Uri createImageUri() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Medimate_Capture_" + System.currentTimeMillis());
        values.put(MediaStore.Images.Media.DESCRIPTION, "From Medimate App");
        return getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }
}