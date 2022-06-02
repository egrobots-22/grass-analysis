package com.egrobots.grassanalysis.adapters;

import com.egrobots.grassanalysis.presentation.videos.swipeablevideos.SwipeableVideosFragment;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewPagerAdapter extends FragmentStateAdapter {
    private static final int CARD_ITEM_SIZE = 2;

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return SwipeableVideosFragment.newInstance(true);
        } else {
            return SwipeableVideosFragment.newInstance(false);
        }
    }

    @Override
    public int getItemCount() {
        return CARD_ITEM_SIZE;
    }
}