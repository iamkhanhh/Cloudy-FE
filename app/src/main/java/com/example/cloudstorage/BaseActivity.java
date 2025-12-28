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
        super.setContentView(R.layout.activity_base_with_refresh);

        swipeRefreshLayout = findViewById(R.id.base_swipe_refresh_layout);
        FrameLayout contentContainer = findViewById(R.id.base_content_container);

        LayoutInflater.from(this).inflate(getLayoutResourceId(), contentContainer, true);

        setupRefreshLayout();

        initViews();
    }

    private void setupRefreshLayout() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setColorSchemeResources(
                    android.R.color.holo_blue_bright,
                    android.R.color.holo_green_light,
                    android.R.color.holo_orange_light
            );

            swipeRefreshLayout.setOnRefreshListener(this::onRefreshData);
        }
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        if (swipeRefreshLayout == null) {
            super.setContentView(layoutResID);
        }
    }

    @LayoutRes
    protected abstract int getLayoutResourceId();

    protected abstract void initViews();

    protected abstract void onRefreshData();
}
