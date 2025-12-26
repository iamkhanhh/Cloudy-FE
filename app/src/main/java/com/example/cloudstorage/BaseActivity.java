package com.example.cloudstorage;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import androidx.annotation.LayoutRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public abstract class BaseActivity extends AppCompatActivity {

    protected SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Dùng layout cơ sở chứa SwipeRefreshLayout
        super.setContentView(R.layout.activity_base_with_refresh);

        swipeRefreshLayout = findViewById(R.id.base_swipe_refresh_layout);
        FrameLayout contentContainer = findViewById(R.id.base_content_container);

        // Lấy layout của Activity con và "bơm" nó vào FrameLayout
        LayoutInflater.from(this).inflate(getLayoutResourceId(), contentContainer, true);

        // Thiết lập listener chung
        setupRefreshLayout();

        // Gọi phương thức để Activity con có thể tìm các view của nó
        initViews();
    }

    private void setupRefreshLayout() {
        if (swipeRefreshLayout != null) {
            // Thiết lập màu sắc (tùy chọn)
            swipeRefreshLayout.setColorSchemeResources(
                    android.R.color.holo_blue_bright,
                    android.R.color.holo_green_light,
                    android.R.color.holo_orange_light
            );

            // Lắng nghe sự kiện refresh và gọi đến phương thức abstract
            swipeRefreshLayout.setOnRefreshListener(this::onRefreshData);
        }
    }

    /**
     * Ghi đè phương thức setContentView để đảm bảo layout con được thêm vào đúng chỗ.
     * Cần phải cẩn thận khi sử dụng, nhưng với cấu trúc này, chúng ta sẽ không gọi nó trực tiếp.
     */
    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        // Không dùng phương thức này trực tiếp, thay vào đó hãy dùng getLayoutResourceId()
        // Dòng này chỉ để đảm bảo nếu có lỡ gọi thì cũng không phá vỡ cấu trúc
        if (swipeRefreshLayout == null) {
            super.setContentView(layoutResID);
        }
    }

    // --- CÁC PHƯƠNG THỨC ABSTRACT MÀ ACTIVITY CON PHẢI CÀI ĐẶT ---

    /**
     * Activity con phải trả về ID của layout riêng của nó.
     * Ví dụ: R.layout.activity_home_page
     */
    @LayoutRes
    protected abstract int getLayoutResourceId();

    /**
     * Activity con cài đặt logic để tìm và khởi tạo các View của nó (findViewById).
     */
    protected abstract void initViews();

    /**
     * Activity con cài đặt logic để tải lại dữ liệu khi người dùng kéo xuống.
     */
    protected abstract void onRefreshData();
}
