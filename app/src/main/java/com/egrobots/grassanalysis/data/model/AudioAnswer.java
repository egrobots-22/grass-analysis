package com.egrobots.grassanalysis.data.model;

public class AudioAnswer {

    private String id;
    private String audioUri;
    private String audioLength;
    private String recordedUser;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAudioUri() {
        return audioUri;
    }

    public void setAudioUri(String audioUri) {
        this.audioUri = audioUri;
    }

    public String getAudioLength() {
        return audioLength;
    }

    public void setAudioLength(String audioLength) {
        this.audioLength = audioLength;
    }

    public String getRecordedUser() {
        return recordedUser;
    }

    public void setRecordedUser(String recordedUser) {
        this.recordedUser = recordedUser;
    }
}
