package com.egrobots.grassanalysis.data.model;

import java.util.List;

public class QuestionItem {
    private String id;
    private String deviceToken;
    private String questionMediaUri;
    private String questionAudioUri;
    private String username;
    private List<String> audioAnswerUri;
    private long timestamp;
    private String type;
    private boolean justUploaded;
    private String flag;

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

    public String getQuestionMediaUri() {
        return questionMediaUri;
    }

    public void setQuestionMediaUri(String questionMediaUri) {
        this.questionMediaUri = questionMediaUri;
    }

    public String getQuestionAudioUri() {
        return questionAudioUri;
    }

    public void setQuestionAudioUri(String questionAudioUri) {
        this.questionAudioUri = questionAudioUri;
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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }


    public void setIsJustUploaded(boolean isJustUploaded) {
        this.justUploaded = isJustUploaded;
    }

    public boolean isJustUploaded() {
        return justUploaded;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public enum RecordType {
        VIDEO,
        IMAGE;
    }
}
