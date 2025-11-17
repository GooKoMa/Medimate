package com.example.medimate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medimate.GPT.models.Drug;
import com.example.medimate.OCR.DrugDetailDialog;

import java.util.ArrayList;

public class DrugListActivity extends AppCompatActivity {

    private ArrayList<Drug> drugList;
    private RecyclerView recyclerView;
    private DrugListAdapter adapter;
    private ImageButton backButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drug_list);

        recyclerView = findViewById(R.id.drug_list_recycler);
        backButton = findViewById(R.id.back_button);

        // MedimateActivity에서 전달받은 데이터
        drugList = (ArrayList<Drug>) getIntent().getSerializableExtra("drugList");

        adapter = new DrugListAdapter(drugList, this::openDetailDialog);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));  // 2열
        recyclerView.setAdapter(adapter);

        backButton.setOnClickListener(v -> finish());
    }

    private void openDetailDialog(Drug drug) {
        DrugDetailDialog dialog = new DrugDetailDialog(drug);
        dialog.show(getSupportFragmentManager(), "DrugDetailDialog");
    }
}
