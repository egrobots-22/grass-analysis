package com.egrobots.grassanalysis.adapters;

import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.egrobots.grassanalysis.R;
import com.egrobots.grassanalysis.data.DatabaseRepository;
import com.egrobots.grassanalysis.data.model.AudioAnswer;
import com.egrobots.grassanalysis.data.model.QuestionItem;
import com.egrobots.grassanalysis.managers.AudioPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class AudioAdapters extends RecyclerView.Adapter<AudioAdapters.AudioViewHolder> {

    private AudioPlayer.AudioPlayCallback audioPlayCallback;
    private List<AudioAnswer> audioAnswers = new ArrayList<>();
    private DatabaseRepository databaseRepository;
    private CompositeDisposable disposable = new CompositeDisposable();
    private boolean isJustUploaded;

    public AudioAdapters(DatabaseRepository databaseRepository, AudioPlayer.AudioPlayCallback audioPlayCallback) {
        this.databaseRepository = databaseRepository;
        this.audioPlayCallback = audioPlayCallback;
    }

    @NonNull
    @Override
    public AudioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.play_audio_item_layout, parent, false);
        return new AudioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AudioViewHolder holder, int position) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            AudioAnswer audioAnswer = audioAnswers.get(position);
            if (audioAnswer == null) {
                holder.progressBar.setVisibility(View.VISIBLE);
                holder.audioNameTextView.setVisibility(View.GONE);
                holder.audioLengthTextView.setVisibility(View.GONE);
                holder.playButton.setVisibility(View.GONE);
                holder.pauseButton.setVisibility(View.GONE);
            } else {
                holder.progressBar.setVisibility(View.GONE);
                holder.audioNameTextView.setVisibility(View.VISIBLE);
                holder.audioLengthTextView.setVisibility(View.VISIBLE);
                holder.playButton.setVisibility(View.VISIBLE);
                holder.audioNameTextView.setText(audioAnswer.getRecordedUser());
                holder.audioLengthTextView.setText(audioAnswer.getAudioLength());
                holder.setAudioUri(audioAnswer.getAudioUri().toString());
            }
            executorService.shutdown();
        });
    }

    @Override
    public int getItemCount() {
        return audioAnswers.size();
    }

    public void addNewAudio(AudioAnswer audioAnswer) {
        if (audioAnswer == null) {
            isJustUploaded = true;
            audioAnswers.add(0, null);
            notifyDataSetChanged();
        } else if (isJustUploaded) {
            isJustUploaded = false;
            audioAnswers.set(0, audioAnswer);
            notifyItemChanged(0);
        } else {
            audioAnswers.add(0, audioAnswer);
            notifyDataSetChanged();
        }
    }

    public void retrieveAudios(QuestionItem questionItem) {
        databaseRepository.getRecordedAudiosForQuestion(questionItem)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .toObservable()
                .subscribe(new Observer<AudioAnswer>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onNext(AudioAnswer audioAnswer) {
                        addNewAudio(audioAnswer);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    class AudioViewHolder extends RecyclerView.ViewHolder implements AudioPlayer.AudioPlayCallback {
        private AudioPlayer audioPlayer;

        @BindView(R.id.playButton)
        ImageButton playButton;
        @BindView(R.id.pauseButton)
        ImageButton pauseButton;
        @BindView(R.id.audioNameTextView)
        TextView audioNameTextView;
        @BindView(R.id.audio_length_textview)
        TextView audioLengthTextView;
        @BindView(R.id.audio_progress_bar)
        ProgressBar progressBar;

        public AudioViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        private void setAudioUri(String audioUri) {
            audioPlayer = new AudioPlayer();
            audioPlayer.setAudio(audioUri, this);
        }

        @OnClick(R.id.pauseButton)
        public void onPauseClicked() {
            playButton.setVisibility(View.VISIBLE);
            pauseButton.setVisibility(View.GONE);
            audioPlayer.stopAudio();
        }

        @OnClick(R.id.playButton)
        public void onPlayClicked() {
            pauseButton.setVisibility(View.VISIBLE);
            playButton.setVisibility(View.GONE);
            audioPlayer.playAudio();
        }

        @Override
        public void onStartPlayingAnswerAudio(AudioPlayer audioPlayer) {
            audioPlayCallback.onStartPlayingAnswerAudio(audioPlayer);
        }

        @Override
        public void onFinishPlayingAnswerAudio() {
            playButton.setVisibility(View.VISIBLE);
            pauseButton.setVisibility(View.GONE);
            audioPlayCallback.onFinishPlayingAnswerAudio();
        }
    }
}
