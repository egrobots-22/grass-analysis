package com.egrobots.grassanalysis.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.devlomi.record_view.RecordButton;
import com.devlomi.record_view.RecordView;
import com.egrobots.grassanalysis.R;
import com.egrobots.grassanalysis.data.DatabaseRepository;
import com.egrobots.grassanalysis.data.LocalDataRepository;
import com.egrobots.grassanalysis.data.model.QuestionItem;
import com.egrobots.grassanalysis.data.model.Reactions;
import com.egrobots.grassanalysis.managers.AudioPlayer;
import com.egrobots.grassanalysis.managers.ExoPlayerVideoManager;
import com.egrobots.grassanalysis.utils.RecordAudioImpl;
import com.egrobots.grassanalysis.utils.SwipeLayoutToHideAndShow;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class VideosAdapter extends RecyclerView.Adapter<VideosAdapter.VideoViewHolder> {

    public List<QuestionItem> questionItems = new ArrayList<>();
    private RecordAudioImpl.RecordAudioCallback recordAudioCallback;
    private AudioPlayer.AudioPlayCallback audioPlayCallback;
    private Reactions.ReactionsCallback reactionsCallback;
    private DatabaseRepository databaseRepository;
    private LocalDataRepository localDataRepository;
    private List<ExoPlayerVideoManager> managers = new ArrayList<>();
    private HashMap<String, AudioAdapters> audioAnswersAdaptersForQuestionMap = new HashMap<>();
    private HashMap<Integer, RecyclerView> audioAnswersRecyclerForQuestionMap = new HashMap<>();

    public VideosAdapter(DatabaseRepository databaseRepository, LocalDataRepository localDataRepository) {
        this.databaseRepository = databaseRepository;
        this.localDataRepository = localDataRepository;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_screen_layout, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        QuestionItem questionItem = questionItems.get(position);
        holder.setQuestion(questionItem, position);
        if (questionItem.getType() == null || questionItem.getType().contains("mp4")) {
            holder.exoThumbnail.setVisibility(View.GONE);
        } else {
            holder.exoThumbnail.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(questionItem.getQuestionMediaUri())
                    .into(holder.exoThumbnail);
        }
        managers.get(position).initializePlayer(holder.playerView);

        //set likes & dislikes count
        holder.toggleLikeDislikeButtonsGroup.clearOnButtonCheckedListeners();
        holder.likeButton.setText(String.valueOf(questionItem.getLIKES() == null ? 0 : questionItem.getLIKES().getCount()));
        holder.dislikeButton.setText(String.valueOf(questionItem.getDISLIKES() == null ? 0 : questionItem.getDISLIKES().getCount()));
        holder.likeButton.setChecked(questionItem.isLikedByCurrentUser());
        holder.dislikeButton.setChecked(questionItem.isDislikedByCurrentUser());
        holder.toggleLikeDislikeButtonsGroup.addOnButtonCheckedListener(holder);

        //get audio files for current video question
        holder.setRecordAudioView(questionItem);
        holder.setupAudioFilesRecyclerView(questionItem);

        SwipeLayoutToHideAndShow swipeLayout = new SwipeLayoutToHideAndShow();
        swipeLayout.initialize(holder.audiosLayout,
                holder.audioFilesRecyclerView,
                Arrays.asList(SwipeLayoutToHideAndShow.SwipeDirection.leftToRight,
                        SwipeLayoutToHideAndShow.SwipeDirection.rightToLeft));
    }

    @Override
    public int getItemCount() {
        return questionItems.size();
    }

    public void addNewVideo(Context context, QuestionItem questionItem) {
        questionItems.add(0, questionItem);
        notifyItemInserted(0);
        if (questionItem.getType() == null || questionItem.getType().contains("mp4")) {
            ExoPlayerVideoManager exoPlayerVideoManager = new ExoPlayerVideoManager();
            exoPlayerVideoManager.initializeExoPlayer(context, questionItem.getQuestionMediaUri());
            managers.add(0, exoPlayerVideoManager);
        } else {
            ExoPlayerVideoManager exoPlayerAudioManager = new ExoPlayerVideoManager();
            exoPlayerAudioManager.initializeAudioExoPlayer(context, questionItem.getQuestionAudioUri(), false);
            managers.add(0, exoPlayerAudioManager);
        }
    }

    public void addAll(Context context, List<QuestionItem> itemsList) {
        questionItems.addAll(itemsList);
        for (QuestionItem item : itemsList) {
            if (item.getType() == null || item.getType().contains("mp4")) {
                ExoPlayerVideoManager exoPlayerVideoManager = new ExoPlayerVideoManager();
                exoPlayerVideoManager.initializeExoPlayer(context, item.getQuestionMediaUri());
                managers.add(exoPlayerVideoManager);
            } else {
                ExoPlayerVideoManager exoPlayerAudioManager = new ExoPlayerVideoManager();
                exoPlayerAudioManager.initializeAudioExoPlayer(context, item.getQuestionAudioUri(), false);
                managers.add(exoPlayerAudioManager);
            }
        }
        notifyDataSetChanged();
    }

    public void updateQuestionItem(QuestionItem questionItem, int updatedQuestionItemPosition) {
        questionItems.set(updatedQuestionItemPosition, questionItem);
        notifyItemChanged(updatedQuestionItemPosition);
    }

    public ExoPlayerVideoManager getCurrentExoPlayerManager(int position) {
        return managers.get(position);
    }

    public List<ExoPlayerVideoManager> getCurrentExoPlayerManagerList() {
        return managers;
    }

    public void setRecordAudioCallback(RecordAudioImpl.RecordAudioCallback recordAudioCallback) {
        this.recordAudioCallback = recordAudioCallback;
    }

    public void setAudioPlayCallback(AudioPlayer.AudioPlayCallback audioPlayCallback) {
        this.audioPlayCallback = audioPlayCallback;
    }

    public void setReactionsCallback(Reactions.ReactionsCallback reactionsCallback) {
        this.reactionsCallback = reactionsCallback;
    }

    public void loadAddingNewAudioAnswer(String id) {
        audioAnswersAdaptersForQuestionMap.get(id).addNewAudio(null);
    }

    public RecyclerView getAudioAnswersRecyclerViewForQuestion(int position) {
        return audioAnswersRecyclerForQuestionMap.get(position);
    }

    class VideoViewHolder extends RecyclerView.ViewHolder implements MaterialButtonToggleGroup.OnButtonCheckedListener {

        @BindView(R.id.audiosLayout)
        ConstraintLayout audiosLayout;
        @BindView(R.id.record_view)
        RecordView recordView;
        @BindView(R.id.record_button)
        RecordButton recordButton;
        @BindView(R.id.progressBar)
        ProgressBar progressBar;
        @BindView(R.id.audioFilesRecyclerView)
        RecyclerView audioFilesRecyclerView;
        @BindView(R.id.videoView)
        PlayerView playerView;
        @BindView(R.id.exo_thumbnail)
        ImageView exoThumbnail;
        @BindView(R.id.toggle_buttons)
        MaterialButtonToggleGroup toggleLikeDislikeButtonsGroup;
        @BindView(R.id.like_button)
        MaterialButton likeButton;
        @BindView(R.id.dislike_button)
        MaterialButton dislikeButton;
        AudioAdapters audioAdapters;
        private QuestionItem questionItem;
        private int currentPosition;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        private void decreaseDislikesCount() {
            String dislikes = dislikeButton.getText().toString().trim();
            int dislikesCount = dislikes.equals("0") ? 0 : Integer.parseInt(dislikes);
            dislikeButton.setText(String.valueOf(--dislikesCount));
            if (questionItem.getDISLIKES() == null) {
                Reactions dislikesReact = new Reactions();
                dislikesReact.setCount(dislikesCount);
                questionItem.setDISLIKES(dislikesReact);
            } else {
                questionItem.getDISLIKES().setCount(dislikesCount);
            }
            questionItem.setDislikedByCurrentUser(false);
            reactionsCallback.updateReactions(Reactions.ReactType.DISLIKES, questionItem.getId(), null, dislikesCount, false, currentPosition);
            questionItems.set(currentPosition, questionItem);
            notifyItemChanged(currentPosition, questionItem);
        }

        private void decreaseLikesCount() {
            String likes = likeButton.getText().toString().trim();
            int likesCount = likes.equals("0") ? 0 : Integer.parseInt(likes);
            likeButton.setText(String.valueOf(--likesCount));
            if (questionItem.getLIKES() == null) {
                Reactions likesReact = new Reactions();
                likesReact.setCount(likesCount);
                questionItem.setLIKES(likesReact);
            } else {
                questionItem.getLIKES().setCount(likesCount);
            }
            questionItem.setLikedByCurrentUser(false);
            reactionsCallback.updateReactions(Reactions.ReactType.LIKES, questionItem.getId(), null, likesCount, false, currentPosition);
            questionItems.set(currentPosition, questionItem);
            notifyItemChanged(currentPosition, questionItem);
        }

        private void increaseDislikesCount() {
            String dislikes = dislikeButton.getText().toString().trim();
            int dislikesCount = dislikes.equals("0") ? 0 : Integer.parseInt(dislikes);
            dislikeButton.setText(String.valueOf(++dislikesCount));
            if (questionItem.getDISLIKES() == null) {
                Reactions dislikesReact = new Reactions();
                dislikesReact.setCount(dislikesCount);
                questionItem.setDISLIKES(dislikesReact);
            } else {
                questionItem.getDISLIKES().setCount(dislikesCount);
            }
            questionItem.setDislikedByCurrentUser(true);
            reactionsCallback.updateReactions(Reactions.ReactType.DISLIKES, questionItem.getId(), null, dislikesCount, true, currentPosition);
            questionItems.set(currentPosition, questionItem);
            notifyItemChanged(currentPosition, questionItem);
        }

        private void increaseLikesCount() {
            String likes = likeButton.getText().toString().trim();
            int likesCount = likes.equals("0") ? 0 : Integer.parseInt(likes);
            likeButton.setText(String.valueOf(++likesCount));
            if (questionItem.getLIKES() == null) {
                Reactions likesReact = new Reactions();
                likesReact.setCount(likesCount);
                questionItem.setLIKES(likesReact);
            } else {
                questionItem.getLIKES().setCount(likesCount);
            }
            questionItem.setLikedByCurrentUser(true);
            reactionsCallback.updateReactions(Reactions.ReactType.LIKES, questionItem.getId(), null, likesCount, true, currentPosition);
            questionItems.set(currentPosition, questionItem);
            notifyItemChanged(currentPosition, questionItem);
        }

        private void setRecordAudioView(QuestionItem questionItem) {
            RecordAudioImpl recordAudioImpl = new RecordAudioImpl(itemView.getContext(), recordView, recordButton, questionItem, recordAudioCallback);
            recordAudioImpl.setupRecordAudio();
        }

        private void setupAudioFilesRecyclerView(QuestionItem questionItem) {
            audioAdapters = new AudioAdapters(databaseRepository, localDataRepository, audioPlayCallback, reactionsCallback);
            audioFilesRecyclerView.setAdapter(audioAdapters);
            audioFilesRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            audioAdapters.retrieveAudios(questionItem);
            audioAnswersAdaptersForQuestionMap.put(questionItem.getId(), audioAdapters);
            audioAnswersRecyclerForQuestionMap.put(getAbsoluteAdapterPosition(), audioFilesRecyclerView);
        }

        @Override
        public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
            if (isChecked) {
                switch (checkedId) {
                    case R.id.like_button:
                        increaseLikesCount();
                        break;
                    case R.id.dislike_button:
                        increaseDislikesCount();
                        break;
                }
            } else {
                switch (checkedId) {
                    case R.id.like_button:
                        decreaseLikesCount();
                        break;
                    case R.id.dislike_button:
                        decreaseDislikesCount();
                        break;
                }
            }
        }

        public void setQuestion(QuestionItem questionItem, int position) {
            currentPosition = position;
            this.questionItem = questionItem;
        }
    }

}
