package cn.yhq.http.core;

public interface ProgressListener {
    void onProgress(long bytesRead, long contentLength);
}
