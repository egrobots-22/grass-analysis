package com.egrobots.grassanalysis.data.model;

import java.util.List;

public class VideoQuestionItem {
    private String id;
    private String deviceToken;
    private String videoQuestionUri;
    private String username;
    private List<String> audioAnswerUri;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public String getVideoQuestionUri() {
        return videoQuestionUri;
    }

    public void setVideoQuestionUri(String videoQuestionUri) {
        this.videoQuestionUri = videoQuestionUri;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getAudioAnswerUri() {
        return audioAnswerUri;
    }

    public void setAudioAnswerUri(List<String> audioAnswerUri) {
        this.audioAnswerUri = audioAnswerUri;
    }
}
