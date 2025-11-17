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
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

// --- AndroidX import ---
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

// --- IO import ---
import java.io.IOException;

// --- Java 유틸 ---
import java.util.ArrayList;
import java.util.List;

// --- Project import ---
import com.example.medimate.GPT.models.Drug;

public class MedimateActivity extends AppCompatActivity {

    // ViewModel
    private MainViewModel viewModel;

    // UI
    private Button cameraButton;
    private Button galleryButton;
    private ProgressBar progressBar;

    // 런처들
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<String> galleryLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    // 촬영 이미지 Uri
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // EdgeToEdge + 레이아웃 설정
        EdgeToEdge.enable(this);
        setContentView(R.layout.ocr_main);   // 🔥 activity_main 말고 ocr_main 사용

        // 기존 템플릿이 쓰던 root id가 main 일 가능성이 높아서 그대로 사용
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.ocr_main_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ViewModel
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // UI 연결
        cameraButton = findViewById(R.id.camera_button);
        galleryButton = findViewById(R.id.gallery_button);
        progressBar = findViewById(R.id.progress_bar);

        // 런처 준비
        setupCameraLauncher();
        setupGalleryLauncher();
        setupPermissionLauncher();

        // 버튼 클릭
        cameraButton.setOnClickListener(v -> checkCameraPermissionAndLaunch());
        galleryButton.setOnClickListener(v -> launchGallery());

        // LiveData 관찰
        setupObservers();
    }

    // LiveData 관찰
    private void setupObservers() {

        // 로딩바 표시
        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null && isLoading) {
                progressBar.setVisibility(ProgressBar.VISIBLE);
            } else {
                progressBar.setVisibility(ProgressBar.GONE);
            }
        });

        // Drug 데이터 도착
        viewModel.getDrugDataLiveData().observe(this, responseData -> {
            if (responseData != null && responseData.getDrugs() != null) {
                List<Drug> drugs = responseData.getDrugs();
                if (!drugs.isEmpty()) {
                    goToDrugListScreen(drugs);   // 🔥 새 화면으로 넘김
                }
            }
        });
    }

    /**
     * OCR + GPT 결과로 만들어진 약 목록 화면으로 이동
     */
    private void goToDrugListScreen(List<Drug> drugs) {
        Intent intent = new Intent(MedimateActivity.this, DrugListActivity.class);
        intent.putExtra("drugList", new ArrayList<>(drugs)); // ArrayList로 감싸서 전달
        startActivity(intent);
    }

    /**
     * Uri → Bitmap → ViewModel 로 전달
     */
    private void processImageUri(Uri uri) {
        if (uri == null) {
            Log.e("MEDIMATE_ACTIVITY", "Received null Uri");
            Toast.makeText(this, "이미지를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            viewModel.startImageProcessing(bitmap);
        } catch (IOException e) {
            Log.e("MEDIMATE_ACTIVITY", "Failed to load bitmap from Uri", e);
            Toast.makeText(this, "사진을 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // --- 런처 설정 ---

    private void setupCameraLauncher() {
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        processImageUri(imageUri);
                    } else {
                        Log.w("MEDIMATE_ACTIVITY", "Camera capture cancelled");
                    }
                }
        );
    }

    private void setupGalleryLauncher() {
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        processImageUri(uri);
                    } else {
                        Log.w("MEDIMATE_ACTIVITY", "Gallery selection cancelled");
                    }
                }
        );
    }

    private void setupPermissionLauncher() {
        requestPermissionLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.RequestPermission(),
                        isGranted -> {
                            if (isGranted) {
                                launchCamera();
                            } else {
                                Toast.makeText(this, "카메라 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show();
                            }
                        });
    }

    // --- 권한/카메라/갤러리 유틸 ---

    private void checkCameraPermissionAndLaunch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            launchCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

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

    private void launchGallery() {
        galleryLauncher.launch("image/*");
    }

    private Uri createImageUri() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Medimate_Capture_" + System.currentTimeMillis());
        values.put(MediaStore.Images.Media.DESCRIPTION, "From Medimate App");
        return getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }
}
