package com.egrobots.grassanalysis.data.model;

public class VideoQuestionItem {
    private String id;
    private String videoQuestionUri;
    private String audioAnswerUri;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVideoQuestionUri() {
        return videoQuestionUri;
    }

    public void setVideoQuestionUri(String videoQuestionUri) {
        this.videoQuestionUri = videoQuestionUri;
    }

    public String getAudioAnswerUri() {
        return audioAnswerUri;
    }

    public void setAudioAnswerUri(String audioAnswerUri) {
        this.audioAnswerUri = audioAnswerUri;
    }
}
