package dev.xuanran.codebook.callback;

import java.io.File;

public interface ExportCallback {
    void onSuccess(File file);

    void onError(Exception e);
}