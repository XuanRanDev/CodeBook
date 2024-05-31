package dev.xuanran.codebook.callback;

public interface ImportCallback {
        void onSuccess();
        void onError(Exception e);
    }