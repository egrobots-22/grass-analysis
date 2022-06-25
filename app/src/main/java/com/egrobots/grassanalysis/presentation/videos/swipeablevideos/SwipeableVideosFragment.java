package com.egrobots.grassanalysis.presentation.videos.swipeablevideos;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.devlomi.record_view.RecordView;
import com.egrobots.grassanalysis.R;
import com.egrobots.grassanalysis.adapters.VideosAdapter;
import com.egrobots.grassanalysis.data.model.AudioAnswer;
import com.egrobots.grassanalysis.data.model.QuestionItem;
import com.egrobots.grassanalysis.managers.AudioPlayer;
import com.egrobots.grassanalysis.managers.ExoPlayerVideoManager;
import com.egrobots.grassanalysis.network.NetworkStateManager;
import com.egrobots.grassanalysis.services.MyUploadService;
import com.egrobots.grassanalysis.utils.Constants;
import com.egrobots.grassanalysis.utils.RecordAudioImpl;
import com.egrobots.grassanalysis.utils.ViewModelProviderFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
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
        implements RecordAudioImpl.RecordAudioCallback, ExoPlayerVideoManager.VideoManagerCallback, AudioPlayer.AudioPlayCallback {
    private static final String TAG = SwipeableVideosFragment.class.getSimpleName();
    private static final int AUDIO_REQUEST_PERMISSION_CODE = 0;
    private static final int RETRIEVED_VIDEOS_LIMIT = 3;

    @BindView(R.id.viewPagerVideos)
    ViewPager2 viewPagerVideos;
    @BindView(R.id.emptyView)
    View emptyView;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.no_network_view)
    View noNetworkView;
    @BindView(R.id.retry_loading_button)
    Button retryLoadingButton;
    @Inject
    ViewModelProviderFactory providerFactory;
    @Inject
    VideosAdapter videosAdapter;

    private List<QuestionItem> itemsList = new ArrayList<>();
    private SwipeableVideosViewModel swipeableVideosViewModel;
    private boolean isCurrentUser;
    private ExoPlayerVideoManager exoPlayerVideoManagerCur;
    private int prevPosition = -1;
    private long lastTimestamp;
    private BroadcastReceiver mBroadcastReceiver;
    private QuestionItem latestVideoItem;
    private Boolean networkState = null;
    private AudioPlayer audioPlayer;
    private String currentPlayingAnswerId;
    private int curVideosPage = 0;

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
        View view = inflater.inflate(R.layout.fragment_swipeably_videos, container, false);
        ButterKnife.bind(this, view);

        observeUploadingVideo();
        videosAdapter.setRecordAudioCallback(this);
        videosAdapter.setAudioPlayCallback(this);
        viewPagerVideos.setAdapter(videosAdapter);
        viewPagerVideos.registerOnPageChangeCallback(new OnVideoChangeCallback());
        swipeableVideosViewModel = new ViewModelProvider(getViewModelStore(), providerFactory).get(SwipeableVideosViewModel.class);
        observeExistVideosState();
        observeNetworkConnection();
        observeVideosUris();
        observeUploadingRecordedAudio();
        if (isCurrentUser) {
            swipeableVideosViewModel.isCurrentUserVideosFound();
        } else {
            swipeableVideosViewModel.isOtherVideosFound();
        }
        swipeableVideosViewModel.getNextVideos(null, isCurrentUser, false);
        return view;
    }

    private void observeNetworkConnection() {
        NetworkStateManager.getInstance().getNetworkConnectivityStatus()
                .observe(getViewLifecycleOwner(), curState -> {
                    if (networkState == null && !curState) {
                        //show the noNetworkView
                        noNetworkView.setVisibility(View.VISIBLE);
                    } else if (networkState != null && !networkState && curState) {
                        //hide the noNetworkView
                        noNetworkView.setVisibility(View.GONE);
                        //retrieve data again
                        swipeableVideosViewModel.getNextVideos(lastTimestamp + 1, isCurrentUser, false);
                    }
                    networkState = curState;
                });
    }

    /*
    observing videos
     */
    private void observeExistVideosState() {
        swipeableVideosViewModel.observeExistVideosState().observe(getViewLifecycleOwner(), isDataExists -> {
            if (!isDataExists) {
                showEmptyView(true);
            } else {
                showEmptyView(false);
            }
        });
    }

    private void observeVideosUris() {
        swipeableVideosViewModel.observeVideoUris().observe(getViewLifecycleOwner(), videoItems -> {
            if (videoItems == null) {
                Toast.makeText(getContext(), getString(R.string.no_intenet_connection), Toast.LENGTH_SHORT).show();
            } else if (videoItems.size() == 0 && videosAdapter.getItemCount() == 0) {
                //no data at all -
                showEmptyView(true);
            } else {
                if (videoItems.size() == 0) {
                    showEmptyView(false);
                    Log.i(TAG, "no more videos");
                } else if (videoItems.size() == 1 &&
                        videoItems.get(0).getFlag() != null &&
                        videoItems.get(0).getFlag().equals(Constants.LATEST)) {
                    //retrieve another data ==> videoItems.get(0) is the latest video sent, so we get it's timestamp
                    swipeableVideosViewModel.getNextVideos(
                            videoItems.get(0).getTimestamp() + 1,
                            isCurrentUser,
                            false);
                } else if (videoItems.size() == 1 &&
                        videoItems.get(0).getFlag() != null &&
                        videoItems.get(0).getFlag().equals(Constants.UPLOADED)) {
                    showEmptyView(false);
                    //uploaded video
                    videosAdapter.addNewVideo(getContext(), videoItems.get(0));
                } else {
                    showEmptyView(false);
                    lastTimestamp = videoItems.get(videoItems.size() - 1).getTimestamp();
                    videosAdapter.addAll(getContext(), videoItems);
//                    curVideosPage++;
                }
            }
        });
    }

    private void showEmptyView(boolean show) {
        if (show) {
            viewPagerVideos.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            viewPagerVideos.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            emptyView.setVisibility(View.GONE);
        }
    }

    /*
    recording audios
     */

    @Override
    public void onStartRecording() {
        if (exoPlayerVideoManagerCur != null) {
            exoPlayerVideoManagerCur.pausePlayer();
        }
        if (audioPlayer != null)
            audioPlayer.stopAudio();
    }

    @Override
    public void uploadRecordedAudio(AudioAnswer audioAnswer, QuestionItem questionItem) {
        if (networkState) {
            videosAdapter.loadAddingNewAudioAnswer(questionItem.getId());
            swipeableVideosViewModel.uploadRecordedAudio(audioAnswer, questionItem);
        } else {
            Toast.makeText(getContext(),
                    getString(R.string.no_intenet_connection) + ", " + getString(R.string.connect_try_again)
                    , Toast.LENGTH_SHORT).show();
        }
    }

    private void observeUploadingRecordedAudio() {
        swipeableVideosViewModel.observeUploadAudioState().observe(getViewLifecycleOwner(), stateResource -> {
            switch (stateResource.status) {
                case SUCCESS:
                    //set audio data
//                    Toast.makeText(getContext(), R.string.recorded_audio_saved_successfully, Toast.LENGTH_SHORT).show();
                case LOADING:
                    //add new audio to the audio adapter
//                    Toast.makeText(getContext(), R.string.uploading_audio, Toast.LENGTH_SHORT).show();
                    break;
                case ERROR:
                    Toast.makeText(getContext(), stateResource.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    private void observeUploadingVideo() {
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (MyUploadService.UPLOAD_COMPLETED.equals(Objects.requireNonNull(intent.getAction()))) {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), R.string.uploaded_done_successfully, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getContext());
        manager.registerReceiver(mBroadcastReceiver, MyUploadService.getIntentFilter());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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
        if (exoPlayerVideoManagerCur != null) exoPlayerVideoManagerCur.pausePlayer();
        if (audioPlayer != null) audioPlayer.stopAudio();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (exoPlayerVideoManagerCur != null) exoPlayerVideoManagerCur.pausePlayer();
        if (audioPlayer != null) audioPlayer.stopAudio();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (audioPlayer != null) audioPlayer.stopAudio();
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

    @Override
    public void onFinishPlayingAnswerAudio() {
//        if (exoPlayerVideoManagerCur != null) {
//            exoPlayerVideoManagerCur.play();
//        }
    }

    @Override
    public void onStartPlayingAnswerAudio(AudioPlayer audioPlayer) {
        if (this.audioPlayer != null && !currentPlayingAnswerId.equals(audioPlayer.getAnswerAudioId())) {
            this.audioPlayer.stopAudio();
        }
        this.audioPlayer = audioPlayer;
        currentPlayingAnswerId = audioPlayer.getAnswerAudioId();
        if (exoPlayerVideoManagerCur != null) {
            exoPlayerVideoManagerCur.pausePlayer();
            exoPlayerVideoManagerCur.getPlayerView().hideController();
        }
    }

    class OnVideoChangeCallback extends ViewPager2.OnPageChangeCallback {
        @Override
        public void onPageSelected(int position) {
            if (audioPlayer != null)
                audioPlayer.stopAudio();
            if (prevPosition != -1) {
                ExoPlayerVideoManager exoPlayerVideoManagerPrev = videosAdapter.getCurrentExoPlayerManager(prevPosition);
                if (exoPlayerVideoManagerPrev != null) {
                    exoPlayerVideoManagerPrev.pausePlayer();
                }
            }
            exoPlayerVideoManagerCur = videosAdapter.getCurrentExoPlayerManager(position);
            if (exoPlayerVideoManagerCur != null) {
                exoPlayerVideoManagerCur.setExoPlayerCallback(SwipeableVideosFragment.this);
                exoPlayerVideoManagerCur.getPlayerView().hideController();
                exoPlayerVideoManagerCur.play();
            }
            int curAdapterSize = videosAdapter.questionItems.size();
            if (prevPosition < position && position == curAdapterSize - 1) { //scroll forward
                //get next block of videos
                if (networkState) {
                    swipeableVideosViewModel.getNextVideos(lastTimestamp + 1, isCurrentUser, false);
                } else {
                    Toast.makeText(getContext(),
                            getString(R.string.no_intenet_connection) + ", " + getString(R.string.connect_try_again)
                            , Toast.LENGTH_SHORT).show();
                }
                //release old videos
                if (curVideosPage > 1) {
                    releasePrevOfCurPage();
                }
            } else if (prevPosition < position && position == (curVideosPage * RETRIEVED_VIDEOS_LIMIT) && position < curAdapterSize) {
                //enters next page (scroll forward
                curVideosPage++;
                //reinitialize next page videos
                reinitializeNextOfCurPage(curAdapterSize);

            } else if (prevPosition > position && curVideosPage > 2) { //scroll backward
                Toast.makeText(getContext(), "Scroll backward", Toast.LENGTH_SHORT).show();
                //decrease current page
                if ((position + 1) % RETRIEVED_VIDEOS_LIMIT == 0) {
                    curVideosPage--;
//                    int lastPositionOfPrevPage = (curVideosPage * RETRIEVED_VIDEOS_LIMIT) - 1;
//                    if (position == lastPositionOfPrevPage) {
                    Toast.makeText(getContext(), "enters previous page", Toast.LENGTH_SHORT).show();
                    //release cur page + 1 videos if found
                    releaseNextOfCurPage(curAdapterSize);
                    //enters prev page ==> reinitialize prev prev page
                    reinitializePrevOfCurPage();
                    Toast.makeText(getContext(), "Current Page = " + curVideosPage, Toast.LENGTH_SHORT).show();
                }
            }
            if (videosAdapter.getAudioAnswersRecyclerViewForQuestion(position) != null) {
                //to show audios answers recycler view if it's hidden by swipe gesture
                videosAdapter.getAudioAnswersRecyclerViewForQuestion(position).setVisibility(View.VISIBLE);
            }
            prevPosition = position;
        }

        private void releasePrevOfCurPage() {
            //get positions of videos that will be released
            int to = RETRIEVED_VIDEOS_LIMIT * (curVideosPage - 1) - 1;
            int from = to - (RETRIEVED_VIDEOS_LIMIT - 1);
            for (int i = from; i <= to; i++) {
                videosAdapter.getCurrentExoPlayerManagerList().get(i).stopPlayer();
                Log.e(TAG, "Scroll forward - Stop video position " + i);
            }
            Toast.makeText(getContext(), "Scroll forward - Release page: " + (curVideosPage - 1)
                    + ", from: " + from + " to: " + to, Toast.LENGTH_SHORT).show();
        }

        private void reinitializeNextOfCurPage(int curAdapterSize) {
            int firstPositionOfNextPage = (curVideosPage + 1) * RETRIEVED_VIDEOS_LIMIT;
            if (firstPositionOfNextPage < curAdapterSize) {     //if not last page of retrieved videos
                int lastPositionOfNextPage = firstPositionOfNextPage + (RETRIEVED_VIDEOS_LIMIT - 1);
                for (int i = firstPositionOfNextPage; i <= lastPositionOfNextPage; i++) {
                    videosAdapter.getCurrentExoPlayerManagerList().get(i).play();
                    videosAdapter.getCurrentExoPlayerManagerList().get(i).pausePlayer();
                }
                Toast.makeText(getContext(), "Scroll forward - Reinitialize page: " + (curVideosPage + 1)
                        + ", from: " + firstPositionOfNextPage + " to: " + lastPositionOfNextPage, Toast.LENGTH_SHORT).show();
            }
        }

        private void releaseNextOfCurPage(int curAdapterSize) {
            int lastPositionOfNextPage = ((curVideosPage + 2) * RETRIEVED_VIDEOS_LIMIT) - 1;
            int firstPositionOfNextPage = lastPositionOfNextPage - (RETRIEVED_VIDEOS_LIMIT - 1);
            if (firstPositionOfNextPage < curAdapterSize) {
                lastPositionOfNextPage = Math.min(lastPositionOfNextPage, curAdapterSize);
                for (int i = firstPositionOfNextPage; i <= lastPositionOfNextPage; i++) {
                    videosAdapter.getCurrentExoPlayerManagerList().get(i).stopPlayer();
                    Log.e(TAG, "Scroll backward - Stop video position " + i);
                }
                Toast.makeText(getContext(), "Scroll bacward - release page: " + (curVideosPage + 1)
                        + ", from: " + firstPositionOfNextPage + " to: " + lastPositionOfNextPage, Toast.LENGTH_SHORT).show();
            }
        }

        private void reinitializePrevOfCurPage() {
            Log.i(TAG, "onPageSelected: reinitialize videos");
            int to = (RETRIEVED_VIDEOS_LIMIT * (curVideosPage - 1)) - 1;
            int from = to - (RETRIEVED_VIDEOS_LIMIT - 1);
            for (int i = from; i <= to; i++) {
                videosAdapter.getCurrentExoPlayerManagerList().get(i).play();
                videosAdapter.getCurrentExoPlayerManagerList().get(i).pausePlayer();
                Log.e(TAG, "Scroll backward - reinitialize video position " + i);
            }
            Toast.makeText(getContext(), "Scroll backward - Reinitialize page: " + (curVideosPage - 1)
                    + ", from: " + from + " to: " + to, Toast.LENGTH_SHORT).show();
        }
    }
}