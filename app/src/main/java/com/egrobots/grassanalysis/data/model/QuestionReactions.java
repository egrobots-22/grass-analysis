package com.egrobots.grassanalysis.data.model;

import java.util.HashMap;
import java.util.List;

public class QuestionReactions {

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

    public interface QuestionReactionsCallback {
        void updateReactions(ReactType type, String questionId, int newCount, boolean increase, int position);
    }
}
