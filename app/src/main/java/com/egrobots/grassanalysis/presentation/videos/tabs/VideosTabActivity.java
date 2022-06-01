package com.egrobots.grassanalysis.presentation.videos.tabs;

import android.os.Bundle;

import com.egrobots.grassanalysis.R;
import com.egrobots.grassanalysis.adapters.ViewPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import dagger.android.support.DaggerAppCompatActivity;

public class VideosTabActivity extends DaggerAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videos_tab);
        initView();
    }

    private void initView() {
        ViewPager2 viewPager = findViewById(R.id.view_pager);
        TabLayout tabLayout = findViewById(R.id.tabs);
        viewPager.setAdapter(new ViewPagerAdapter(this));
        TabLayoutMediator tabLayoutMediator =  new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    if (position == 0) {
                        tab.setText(R.string.your_questions);
                    } else {
                        tab.setText(R.string.other_questions);
                    }
                });
        tabLayoutMediator.attach();
    }

}