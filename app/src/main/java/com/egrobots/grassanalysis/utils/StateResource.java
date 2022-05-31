package com.egrobots.grassanalysis.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class StateResource<T> {

    @NonNull
    public final Status status;

    @Nullable
    public final String message;


    public StateResource(@NonNull Status status, @Nullable String message) {
        this.status = status;
        this.message = message;
    }

    public static <T> StateResource<T> success () {
        return new StateResource<>(Status.SUCCESS, null);
    }

    public static <T> StateResource<T> error(@NonNull String msg) {

        return new StateResource<>(Status.ERROR, msg);
    }

    public static <T> StateResource<T> loading() {
        return new StateResource<>(Status.LOADING, null);
    }

    public enum Status {
        SUCCESS,
        ERROR,
        LOADING
    }
}
