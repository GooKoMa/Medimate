package com.example.medimate.OCR;

import android.app.Dialog;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medimate.R;
import com.example.medimate.GPT.models.Drug;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DrugDetailDialog extends DialogFragment {

    private Drug drug;
    private TextToSpeech tts;

    public DrugDetailDialog(Drug drug) {
        this.drug = drug;
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_NoActionBar);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.drug_detail_popup, container, false);

        // 뒤로가기 버튼
        ImageButton back = v.findViewById(R.id.btnBack);
        back.setOnClickListener(view -> dismiss());

        //  제목을 약 이름으로 자동 변경
        TextView title = v.findViewById(R.id.detailTitle);
        title.setText(safe(drug.getName()));

        //  Drug → DetailItem 변환 (RecyclerView에 표시될 내용)
        List<DetailItem> items = new ArrayList<>();
        items.add(new DetailItem("이름", safe(drug.getName())));
        items.add(new DetailItem("생김새", safe(drug.getAppearance())));
        items.add(new DetailItem("설명", safe(drug.getDescription())));
        items.add(new DetailItem("용법/복용", safe(drug.getDosage())));
        items.add(new DetailItem("보관방법", safe(drug.getStorage())));
        items.add(new DetailItem("주의사항", safe(drug.getWarning())));

        // RecyclerView 설정
        RecyclerView rv = v.findViewById(R.id.detailRecyclerView);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(new DetailAdapter(items, this::speak));

        // TTS 초기화
        tts = new TextToSpeech(getContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.KOREAN);
                tts.setSpeechRate(1.0f);
            }
        });

        return v;
    }

    // null-safe 핸들링
    private String safe(String s) {
        return s == null ? "정보 없음" : s;
    }

    // TTS 실행
    private void speak(String text) {
        if (tts != null && text != null) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    // 팝업 크기 조절 (화면 90%)
    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();

        if (dialog != null && dialog.getWindow() != null) {
            int width = (int) (getScreenWidth() * 0.90);
            int height = (int) (getScreenHeight() * 0.90);
            dialog.getWindow().setLayout(width, height);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    private int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    private int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
