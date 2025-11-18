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


// 1. 아이템 타입 인터페이스
interface DisplayableItem {}

// 2. 헤더 아이템 (e.g., "간 건강을 위한 추천")
class HeaderItem implements DisplayableItem {
    final String title;
    HeaderItem(String title) {
        this.title = title;
    }
}

// 3. 제품 아이템 (Product 객체)
class ProductItem implements DisplayableItem {
    final Product product;
    ProductItem(Product product) {
        this.product = product;
    }
}


// 멀티 뷰 타입을 지원하는 어댑터
public class ProductAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // 뷰 타입 정의
    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_ITEM = 1;

    // String/Product 대신 DisplayableItem 리스트 사용
    private final List<DisplayableItem> items = new ArrayList<>();

    // 1. 헤더를 위한 ViewHolder
    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        public TextView headerTitle;
        public HeaderViewHolder(View view) {
            super(view);
            // item_section_header.xml (새로 만든 파일)의 ID
            headerTitle = view.findViewById(R.id.tvSectionHeader);
        }
    }

    // 2. 제품 카드를 위한 ViewHolder (이전에 만든 것)
    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        public TextView productName;
        public TextView function;

        public ProductViewHolder(View view) {
            super(view);
            // item_product.xml (카드뷰로 수정한 파일)의 ID
            productName = view.findViewById(R.id.tvProductName);
            function = view.findViewById(R.id.tvFunction);
        }
    }

    // position에 따라 어떤 뷰 타입인지 반환
    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof HeaderItem) {
            return VIEW_TYPE_HEADER;
        } else {
            return VIEW_TYPE_ITEM;
        }
    }

    // 뷰 타입에 따라 다른 XML 레이아웃을 로드
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_HEADER) {
            // 헤더 뷰 로드 (새로 만든 item_section_header.xml)
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_section_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            // 제품 카드 뷰 로드 (기존 item_product.xml)
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_product, parent, false);
            return new ProductViewHolder(view);
        }
    }

    // 뷰 타입에 따라 데이터를 바인딩
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == VIEW_TYPE_HEADER) {
            // 헤더일 경우
            HeaderItem headerItem = (HeaderItem) items.get(position);
            HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
            headerViewHolder.headerTitle.setText(headerItem.title);
        } else {
            // 제품 카드일 경우
            ProductItem productItem = (ProductItem) items.get(position);
            ProductViewHolder productViewHolder = (ProductViewHolder) holder;
            Product product = productItem.product;

            if (product.productName != null) {
                productViewHolder.productName.setText(product.productName.trim());
            }
            if (product.mainFunction != null) {
                productViewHolder.function.setText(product.mainFunction.trim());
            }
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // 데이터를 통째로 교체하는 함수 (페이징 대신 그룹화된 전체 리스트 사용)
    public void setItems(List<DisplayableItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged(); // 전체 리스트가 바뀌었으므로 DataSetChanged 사용
    }
}