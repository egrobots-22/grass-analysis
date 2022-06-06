package com.egrobots.grassanalysis.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.egrobots.grassanalysis.R;
import com.egrobots.grassanalysis.data.DatabaseRepository;
import com.egrobots.grassanalysis.data.model.AudioAnswer;
import com.egrobots.grassanalysis.data.model.VideoQuestionItem;
import com.egrobots.grassanalysis.managers.AudioPlayer;

import java.util.ArrayList;
import java.util.List;

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
    private List<AudioAnswer> audioAnswers = new ArrayList<>();
    private DatabaseRepository databaseRepository;
    private CompositeDisposable disposable = new CompositeDisposable();

    public AudioAdapters(DatabaseRepository databaseRepository) {
        this.databaseRepository = databaseRepository;
    }

    @NonNull
    @Override
    public AudioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.play_audio_item_layout, parent, false);
        return new AudioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AudioViewHolder holder, int position) {
        AudioAnswer audioAnswer = audioAnswers.get(position);
        holder.audioNameTextView.setText(audioAnswer.getRecordedUser());
        holder.audioLengthTextView.setText(holder.setAudioUri(audioAnswer.getAudioUri()));
    }

    @Override
    public int getItemCount() {
        return audioAnswers.size();
    }

    public void addNewAudio(AudioAnswer audioAnswer) {
        audioAnswers.add(0, audioAnswer);
        notifyDataSetChanged();
    }

    public void retrieveAudios(VideoQuestionItem questionItem) {
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

    static class AudioViewHolder extends RecyclerView.ViewHolder implements AudioPlayer.AudioPlayCallback {
        @BindView(R.id.playButton)
        ImageButton playButton;
        @BindView(R.id.pauseButton)
        ImageButton pauseButton;
        @BindView(R.id.audioNameTextView)
        TextView audioNameTextView;
        @BindView(R.id.audio_length_textview)
        TextView audioLengthTextView;
        private AudioPlayer audioPlayer;

        public AudioViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        private String setAudioUri(String audioUri) {
            audioPlayer = new AudioPlayer();
            audioPlayer.setAudio(audioUri, this);
            return audioPlayer.getAudioDuration();
        }

        private void getAudioDuration() {
            audioPlayer.getAudioDuration();
        }

        @OnClick(R.id.pauseButton)
        public void onPauseClicked() {
            playButton.setVisibility(View.VISIBLE);
            pauseButton.setVisibility(View.GONE);
            audioPlayer.pauseAudio();
        }

        @OnClick(R.id.playButton)
        public void onPlayClicked() {
            pauseButton.setVisibility(View.VISIBLE);
            playButton.setVisibility(View.GONE);
            audioPlayer.playAudio();
        }

        @Override
        public void onComplete() {
            playButton.setVisibility(View.VISIBLE);
            pauseButton.setVisibility(View.GONE);
        }
    }
}
