package com.egrobots.grassanalysis.data.model;

import java.util.HashMap;
import java.util.List;

public class Reactions {

    private int count;
    private HashMap<String, Boolean> users;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public HashMap<String, Boolean> getUsers() {
        return users;
    }

    public void setUsers(HashMap<String, Boolean> users) {
        this.users = users;
    }

    public enum ReactType {
        LIKES,
        DISLIKES
    }

    public interface ReactionsCallback {
        void updateReactions(ReactType type, String questionId, String audioAnswerId, int newCount, boolean increase, int position);
    }
}
