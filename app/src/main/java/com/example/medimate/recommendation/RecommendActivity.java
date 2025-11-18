package com.example.medimate.recommendation;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medimate.R;
import com.example.medimate.recommendation.api.FoodResponse;
import com.example.medimate.recommendation.api.Product;
import com.example.medimate.recommendation.api.RetrofitClient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecommendActivity extends AppCompatActivity {
    private final String apiKey = "5vdpC0fiXEBDtS8A/bOV5Ql5cWmDmsIKEcpv4bryubBdLpyXAnET8rszjBUPgqHL3uCOgQhz2GDc/aI3x1CHQg=="; //디코딩 서비스 키
    private ProductAdapter productAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommend);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.recommend), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //스크롤해도 위에 있던 데이터가 삭제되지 않는 목록
        setupRecyclerView();
        loadProductsAndGroupThem(1, 100);
    }

    //RecyclerView를 설정하고 스크롤 이벤트 감지하는 함수
    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this); // layoutManager를 지역 변수로
        productAdapter = new ProductAdapter(); //
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(productAdapter);
    }

    //API를 통해 제품 데이터 로드하고 필러팅하는 함수
    private void loadProductsAndGroupThem(int pageNo, int numOfRows) {

        Call<FoodResponse> call = RetrofitClient.getInstance().getSupplements(apiKey, pageNo, numOfRows, "xml");

        call.enqueue(new Callback<FoodResponse>() {
            @Override
            public void onResponse(@NonNull Call<FoodResponse> call, @NonNull Response<FoodResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    FoodResponse foodResponse = response.body();
                    List<Product> fetchedProducts = new ArrayList<>();
                    if (foodResponse.body != null && foodResponse.body.items != null && foodResponse.body.items.productList != null) {
                        fetchedProducts = foodResponse.body.items.productList;
                    }

                    // ▼▼▼▼▼ 그룹화 로직 ▼▼▼▼▼

                    // 1. HealthInputActivity에서 보낸 데이터 받기
                    ArrayList<String> chipTexts = getIntent().getStringArrayListExtra("selected_chip_texts");
                    HashMap<String, ArrayList<String>> keywordMap = (HashMap<String, ArrayList<String>>) getIntent().getSerializableExtra("keyword_map");

                    if (chipTexts == null || keywordMap == null) {
                        Toast.makeText(RecommendActivity.this, "데이터 로딩 실패", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 2. 어댑터에 보낼 최종 리스트 (헤더 + 제품 포함)
                    List<DisplayableItem> displayList = new ArrayList<>();

                    // 3. 선택한 칩 텍스트 (예: "간 건강") 순서대로 반복
                    for (String chipText : chipTexts) {
                        // 4. 이 칩에 해당하는 키워드 리스트 (예: ["간", "간 건강"])
                        List<String> keywords = keywordMap.get(chipText);
                        if (keywords == null) continue;

                        // 5. 이 그룹에 속한 제품을 찾기
                        List<Product> productsInGroup = new ArrayList<>();
                        for (Product product : fetchedProducts) {
                            if (product.mainFunction == null) continue;

                            boolean isMatched = false;
                            for (String symptom : keywords) {
                                if (product.mainFunction.contains(symptom)) {
                                    isMatched = true;
                                    break;
                                }
                            }
                            if (isMatched) {
                                productsInGroup.add(product);
                            }
                        }

                        // 6. 일치하는 제품이 있을 때만 헤더와 제품을 리스트에 추가
                        if (!productsInGroup.isEmpty()) {
                            // "간 건강"을 "간 건강을 위한 추천"으로 변경
                            displayList.add(new HeaderItem("'" + chipText + "'을 위한 추천"));
                            for (Product product : productsInGroup) {
                                displayList.add(new ProductItem(product));
                            }
                        }
                    }

                    // 7. 어댑터에 최종 리스트 설정
                    if (displayList.isEmpty()) {
                        Toast.makeText(RecommendActivity.this, "관련 제품을 찾지 못했습니다.", Toast.LENGTH_LONG).show();
                    } else {
                        productAdapter.setItems(displayList);
                    }

                } else {
                    Log.e("RecommendActivity", "API 호출 실패 (응답 없음)");
                    Toast.makeText(RecommendActivity.this, "데이터 로딩 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<FoodResponse> call, @NonNull Throwable t) {
                Log.e("RecommendActivity", "API 호출 실패", t);
                Toast.makeText(RecommendActivity.this, "데이터 로딩 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //제품 리스트를 키워드로 필터링하는 함수
    private List<Product> filterProducts(List<Product> products, List<String> symptoms) {
        if (symptoms.isEmpty()) return products; // 증상 선택이 없으면 모든 제품 반환

        List<Product> filteredList = new ArrayList<>();
        for (Product product : products) {
            boolean isMatched = false;
            if (product.mainFunction == null) continue;

            for (String symptom : symptoms) {
                if (product.mainFunction.contains(symptom)) {
                    isMatched = true;
                    break; //하나라도 일치하면 이 제품은 추가하고 다음 제품 검사
                }
            }
            if (isMatched) {
                filteredList.add(product);
            }
        }
        return filteredList;
    }
}
