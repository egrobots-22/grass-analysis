package com.egrobots.grassanalysis.presentation.videos.swipeablevideos;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.devlomi.record_view.RecordView;
import com.egrobots.grassanalysis.R;
import com.egrobots.grassanalysis.adapters.VideosAdapter;
import com.egrobots.grassanalysis.data.model.VideoQuestionItem;
import com.egrobots.grassanalysis.managers.ExoPlayerVideoManager;
import com.egrobots.grassanalysis.utils.Constants;
import com.egrobots.grassanalysis.utils.RecordAudioImpl;
import com.egrobots.grassanalysis.utils.StateResource;
import com.egrobots.grassanalysis.utils.ViewModelProviderFactory;

import java.io.File;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;
import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.support.DaggerFragment;

import static androidx.core.content.PermissionChecker.PERMISSION_GRANTED;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SwipeableVideosFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SwipeableVideosFragment extends DaggerFragment
        implements RecordAudioImpl.RecordAudioCallback, ExoPlayerVideoManager.VideoManagerCallback {
    private static final String TAG = SwipeableVideosFragment.class.getSimpleName();
    private static final int AUDIO_REQUEST_PERMISSION_CODE = 0;

    @BindView(R.id.viewPagerVideos)
    ViewPager2 viewPagerVideos;
    @BindView(R.id.empty_view_textview)
    TextView emptyTextView;
    @BindView(R.id.emptyView)
    View emptyView;
    @Inject
    ViewModelProviderFactory providerFactory;
    @Inject
    VideosAdapter videosAdapter;

    private SwipeableVideosViewModel swipeableVideosViewModel;
    private boolean isCurrentUser;
    private boolean isScrolled = false;
    private ExoPlayerVideoManager exoPlayerVideoManagerPrev;
    private ExoPlayerVideoManager exoPlayerVideoManagerCur;
    private int prevPosition = -1;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_swipeably_videso, container, false);
        ButterKnife.bind(this, view);

        videosAdapter.setRecordAudioCallback(this);
        viewPagerVideos.setAdapter(videosAdapter);
        viewPagerVideos.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (prevPosition != -1) {
                    exoPlayerVideoManagerPrev = videosAdapter.getCurrentExoPlayerManager(prevPosition);
                    exoPlayerVideoManagerPrev.pausePlayer();
                }
                exoPlayerVideoManagerCur = videosAdapter.getCurrentExoPlayerManager(position);
                exoPlayerVideoManagerCur.setExoPlayerCallback(SwipeableVideosFragment.this);
                exoPlayerVideoManagerCur.play();
                prevPosition = position;
            }

        });
        swipeableVideosViewModel = new ViewModelProvider(getViewModelStore(), providerFactory).get(SwipeableVideosViewModel.class);
        observeVideosUris();
        observeUploadingRecordedAudio();
        observeExistVideosState();
        if (isCurrentUser) {
            swipeableVideosViewModel.getCurrentUserVideos();
        } else {
            swipeableVideosViewModel.getOtherUsersVideos();
        }
        return view;
    }

    private void observeExistVideosState() {
        swipeableVideosViewModel.observeExistVideosState().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isDataExists) {
                if (!isDataExists) {
                    viewPagerVideos.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                } else {
                    viewPagerVideos.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(View.GONE);
                }
            }
        });
    }

    private void observeUploadingRecordedAudio() {
        swipeableVideosViewModel.observeUploadAudioState().observe(getViewLifecycleOwner(), new Observer<StateResource>() {
            @Override
            public void onChanged(StateResource stateResource) {
                switch (stateResource.status) {
                    case SUCCESS:
                        Toast.makeText(getContext(), "تم تسجيل اجابتك بنجاح", Toast.LENGTH_SHORT).show();
                    case LOADING:
                        Toast.makeText(getContext(), "جاري تحميل التسجيل", Toast.LENGTH_SHORT).show();
                        break;
                    case ERROR:
                        Toast.makeText(getContext(), stateResource.message, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
    }

    private void observeVideosUris() {
        swipeableVideosViewModel.observeVideoUris().observe(getViewLifecycleOwner(), new Observer<VideoQuestionItem>() {
            @Override
            public void onChanged(VideoQuestionItem videoQuestionItem) {
                if (viewPagerVideos.getVisibility() != View.VISIBLE) {
                    viewPagerVideos.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(View.GONE);
                }
                videosAdapter.addNewVideo(getContext(), videoQuestionItem);
            }
        });

    }

    @Override
    public void uploadRecordedAudio(File recordFile, VideoQuestionItem questionItem) {
        swipeableVideosViewModel.uploadRecordedAudio(recordFile, questionItem);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == AUDIO_REQUEST_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getActivity(), "Audio Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Audio Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void requestAudioPermission(RecordView recordView) {
        recordView.setRecordPermissionHandler(() -> {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                return true;
            }
            boolean recordPermissionAvailable = ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.RECORD_AUDIO) == PERMISSION_GRANTED;
            if (recordPermissionAvailable) {
                return true;
            }
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, AUDIO_REQUEST_PERMISSION_CODE);
            return false;
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (exoPlayerVideoManagerCur != null) {
            exoPlayerVideoManagerCur.resumePlaying();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (exoPlayerVideoManagerCur != null) {
            exoPlayerVideoManagerCur.pausePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (exoPlayerVideoManagerCur != null) {
            exoPlayerVideoManagerCur.pausePlayer();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for (ExoPlayerVideoManager manager : videosAdapter.getCurrentExoPlayerManagerList()) {
            manager.stopPlayer();
            manager.releasePlayer();
        }
    }

    @Override
    public void onPrepare() {

    }

    @Override
    public void onError(String msg) {

    }

    @Override
    public void onEnd() {
        viewPagerVideos.setCurrentItem(prevPosition + 1, true);
    }
}