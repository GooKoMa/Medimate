package com.example.medimate.recommendation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medimate.R;
import com.example.medimate.recommendation.api.Product;
import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    //어댑터가 관리할 제품 데이터 목록
    private final List<Product> productList = new ArrayList<>();

    //리스트 한 칸에 포함된 UI 요소들을 보관 (static inner class로 변경)
    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        public TextView productName;
        public TextView company;
        public TextView function;

        public ProductViewHolder(View view) {
            super(view);
            productName = view.findViewById(R.id.tvProductName);
            company = view.findViewById(R.id.tvCompany);
            function = view.findViewById(R.id.tvFunction);
        }
    }

    //RecyclerView가 새로운 '칸'이 필요할 때 호출하는 함수(붕어빵 틀을 만듦)
    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    //RecyclerView가 특정 위치의 '칸'에 데이터를 표시해야 할 때 호출하는 함수(붕어빵 안에 팥을 넣음)
    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        // 널 체크 추가
        if (product.productName != null) {
            holder.productName.setText(product.productName.trim());
        }
        if (product.company != null) {
            holder.company.setText(product.company.trim());
        }
        if (product.mainFunction != null) {
            holder.function.setText(product.mainFunction.trim());
        }
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    //페이징을 통해 새로운 데이터를 가져왔을 때 호출하는 함수
    public void addProducts(List<Product> newProducts) {
        int startPosition = productList.size();
        productList.addAll(newProducts);
        notifyItemRangeInserted(startPosition, newProducts.size());
    }
}
