package com.egrobots.grassanalysis.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.egrobots.grassanalysis.R;
import com.egrobots.grassanalysis.data.DatabaseRepository;
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
    private List<String> audioUris = new ArrayList<>();
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
        String audioUri = audioUris.get(position);
        holder.audioNameTextView.setText("Audio Name: " + audioUri);
        holder.setAudioUri(audioUri);
    }

    @Override
    public int getItemCount() {
        return audioUris.size();
    }

    public void addNewAudio(String audioUri) {
        audioUris.add(audioUri);
        notifyDataSetChanged();
    }

    public void retrieveAudios(VideoQuestionItem questionItem) {
        databaseRepository.getRecordedAudiosForQuestion(questionItem)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .toObservable()
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onNext(String audioUri) {
                        addNewAudio(audioUri);
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
        private AudioPlayer audioPlayer;

        public AudioViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        private void setAudioUri(String audioUri) {
            audioPlayer = new AudioPlayer(audioUri, this);
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
