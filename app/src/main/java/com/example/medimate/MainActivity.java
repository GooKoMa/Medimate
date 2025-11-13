package com.example.medimate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.medimate.recommendation.HealthInputActivity;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //영양제추천
        Button recommendButton = findViewById(R.id.recommendButton);

        recommendButton.setOnClickListener(v -> {
            //다른화면으로 이동하기 위한 객체 생성, 현재 화면에서 healthinputactivity로 이동
            Intent intent = new Intent(MainActivity.this, HealthInputActivity.class);

            //intent를 시스템에 전달하여 새로운 화면 시작시킴
            startActivity(intent);
        });
    }
}