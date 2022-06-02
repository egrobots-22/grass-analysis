package com.egrobots.grassanalysis.presentation.videos.swipeablevideos;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.egrobots.grassanalysis.R;
import com.egrobots.grassanalysis.adapters.VideosAdapter;
import com.egrobots.grassanalysis.data.model.VideoQuestionItem;
import com.egrobots.grassanalysis.presentation.temp.SwipeableVideosActivity;
import com.egrobots.grassanalysis.utils.Constants;
import com.egrobots.grassanalysis.utils.ViewModelProviderFactory;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;
import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.support.DaggerFragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SwipeableVideosFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SwipeableVideosFragment extends DaggerFragment {

    private static final String TAG = SwipeableVideosActivity.class.getSimpleName();
    @BindView(R.id.viewPagerVideos)
    ViewPager2 viewPagerVideos;
    @Inject
    ViewModelProviderFactory providerFactory;
    private List<VideoQuestionItem> videoItems = new ArrayList<>();
    private VideosAdapter videosAdapter;
    private SwipeableVideosViewModel swipeableVideosViewModel;
    private boolean isCurrentUser;

    public SwipeableVideosFragment() {
        // Required empty public constructor
    }

    public static SwipeableVideosFragment newInstance(boolean isCurrentUser) {
        SwipeableVideosFragment fragment = new SwipeableVideosFragment();
        Bundle args = new Bundle();
        args.putBoolean(Constants.IS_CURRENT_USER, isCurrentUser);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isCurrentUser = getArguments().getBoolean(Constants.IS_CURRENT_USER);
        }
    }

    private void observeVideosUris() {
        swipeableVideosViewModel.observeVideoUris().observe(getViewLifecycleOwner(), new Observer<VideoQuestionItem>() {
            @Override
            public void onChanged(VideoQuestionItem videoQuestionItem) {
                videoItems.add(videoQuestionItem);
                videosAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getActivity(), "Audio Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Audio Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_swipeably_videso, container, false);
        ButterKnife.bind(this, view);
        swipeableVideosViewModel = new ViewModelProvider(getViewModelStore(), providerFactory).get(SwipeableVideosViewModel.class);
        if (isCurrentUser) {
            swipeableVideosViewModel.getCurrentUserVideos();
        } else {
            swipeableVideosViewModel.getOtherUsersVideos();
        }
        videosAdapter = new VideosAdapter(getActivity(), videoItems);
        viewPagerVideos.setAdapter(videosAdapter);
        observeVideosUris();
        return view;
    }
}