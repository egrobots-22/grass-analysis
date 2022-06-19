package com.egrobots.grassanalysis.data.model;

import android.net.Uri;

import com.egrobots.grassanalysis.utils.Utils;

public class AudioAnswer {

    private String id;
    private String audioUri;
    private long audioLength;
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

    public String getAudioLengthAsString() {
        return Utils.formatMilliSeconds(audioLength);
    }

    public int getAudioLength() {
        return (int) audioLength;
    }

    public void setAudioLength(long audioLength) {
        this.audioLength = audioLength;
    }

    public String getRecordedUser() {
        return recordedUser;
    }

    public void setRecordedUser(String recordedUser) {
        this.recordedUser = recordedUser;
    }
}
