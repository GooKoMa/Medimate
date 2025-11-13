package com.example.medimate.recommendation;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.medimate.R;
import java.util.ArrayList;

public class HealthInputActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        //1. 이 화면의 UI를 activity_health_input으로 지정
        setContentView(R.layout.activity_health_input);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.input), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //2. xml에 있는 checkbox와 button을 변수에 연결
        CheckBox fatigueCheckBox = findViewById(R.id.cb_fatigue);
        CheckBox sleepCheckBox = findViewById(R.id.cb_sleep);
        CheckBox digestionCheckBox = findViewById(R.id.cb_digestion);
        CheckBox immunityCheckBox = findViewById(R.id.cb_immunity);
        CheckBox focusCheckBox = findViewById(R.id.cb_focus);
        Button submitButton = findViewById(R.id.btn_submit);

        //3. 추천받기 버튼 클릭됐을 때 동작
        submitButton.setOnClickListener(v -> {
            ArrayList<String> selectedSymptoms = new ArrayList<>();

            //4. 각 체크박스가 선택되었는지 확인하고 리스트에 키워드 추가
            if (fatigueCheckBox.isChecked()) {
                selectedSymptoms.add("피로");
            }
            if (sleepCheckBox.isChecked()) {
                selectedSymptoms.add("수면");
            }
            if (digestionCheckBox.isChecked()) {
                selectedSymptoms.add("장");
                selectedSymptoms.add("배변");
            }
            if (immunityCheckBox.isChecked()) {
                selectedSymptoms.add("면역");
            }
            if (focusCheckBox.isChecked()) {
                selectedSymptoms.add("기억력");
            }

            //5. 키워드가 하나라도 리스트에 추가되었다면
            if (!selectedSymptoms.isEmpty()) {
                //6. RecommendActivity로 이동
                Intent intent = new Intent(HealthInputActivity.this, RecommendActivity.class);
                intent.putStringArrayListExtra("health_status", selectedSymptoms);
                startActivity(intent);
            } else {
                //아무것도 선택 안했을 때
                Toast.makeText(this, "하나 이상의 건강 상태를 선택해주세요.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}