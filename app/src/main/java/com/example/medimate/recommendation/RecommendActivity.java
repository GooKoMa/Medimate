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
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecommendActivity extends AppCompatActivity {
    private final String apiKey = "5vdpC0fiXEBDtS8A/bOV5Ql5cWmDmsIKEcpv4bryubBdLpyXAnET8rszjBUPgqHL3uCOgQhz2GDc/aI3x1CHQg=="; //디코딩 서비스 키

    //페이징(Paging)기능 관리를 위한 변수들
    private int currentPage = 1;
    private int totalCount = 0;
    private boolean isLoading = false;
    private ProductAdapter productAdapter;
    private LinearLayoutManager layoutManager;

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
        loadProducts(currentPage);
    }

    //RecyclerView를 설정하고 스크롤 이벤트 감지하는 함수
    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager(this);
        productAdapter = new ProductAdapter(); //어댑터 객체 생성
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(productAdapter);

        //스크롤 리스너 추가 (스크롤이 맨 아래에 닿으면 다음 페이지 로드)
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                //로딩중이 아니고 스크롤이 맨 아래에 도달했다면
                if (!isLoading && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
                    //불러올 데이터가 남아있다면 (***원본 코드 버그 수정: productList.size -> productAdapter.getItemCount()***)
                    if (productAdapter.getItemCount() < totalCount) {
                        //다음 페이지를 로드하기 위해 페이지번호 증가시키고 데이터 로딩 함수 호출
                        currentPage++;
                        loadProducts(currentPage);
                    }
                }
            }
        });
    }

    //API를 통해 제품 데이터 로드하고 필러팅하는 함수 (Coroutines -> Callback)
    private void loadProducts(int pageNo) {
        if (isLoading) return;
        isLoading = true;

        // Retrofit 호출 방식 변경
        Call<FoodResponse> call = RetrofitClient.getInstance().getSupplements(apiKey, pageNo, 100, "xml");

        call.enqueue(new Callback<FoodResponse>() {
            @Override
            public void onResponse(@NonNull Call<FoodResponse> call, @NonNull Response<FoodResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    FoodResponse foodResponse = response.body();
                    List<Product> fetchedProducts = new ArrayList<>();
                    if (foodResponse.body != null && foodResponse.body.items != null && foodResponse.body.items.productList != null) {
                        fetchedProducts = foodResponse.body.items.productList;
                    }
                    totalCount = (foodResponse.body != null) ? foodResponse.body.totalCount : 0;

                    ArrayList<String> symptoms = getIntent().getStringArrayListExtra("health_status");
                    if (symptoms == null) {
                        symptoms = new ArrayList<>();
                    }
                    List<Product> recommendedProducts = filterProducts(fetchedProducts, symptoms);

                    if (!recommendedProducts.isEmpty()) {
                        productAdapter.addProducts(recommendedProducts); //어댑터에 제품 추가
                    }

                    // (***원본 코드 버그 수정: productList.isEmpty() -> productAdapter.getItemCount() == 0***)
                    if (productAdapter.getItemCount() == 0 && recommendedProducts.isEmpty() && productAdapter.getItemCount() < totalCount) {
                        //첫 페이지에 결과가 없으면 다음 페이지 자동 로드
                        currentPage++;
                        loadProducts(currentPage);
                    } else if (pageNo == 1 && productAdapter.getItemCount() == 0 && recommendedProducts.isEmpty()) {
                        Toast.makeText(RecommendActivity.this, "관련 제품을 찾지 못했습니다.", Toast.LENGTH_LONG).show();
                    }

                } else {
                    Log.e("RecommendActivity", "API 호출 실패 (응답 없음)");
                    Toast.makeText(RecommendActivity.this, "데이터 로딩 실패", Toast.LENGTH_SHORT).show();
                }
                isLoading = false; // finally 블록 대신 각 콜백의 끝에서 처리
            }

            @Override
            public void onFailure(@NonNull Call<FoodResponse> call, @NonNull Throwable t) {
                Log.e("RecommendActivity", "API 호출 실패", t);
                Toast.makeText(RecommendActivity.this, "데이터 로딩 실패", Toast.LENGTH_SHORT).show();
                isLoading = false;
            }
        });
    }

    //제품 리스트를 키워드로 필터링하는 함수 (filter/any -> for loop)
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
