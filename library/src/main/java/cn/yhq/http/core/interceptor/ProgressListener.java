package cn.yhq.http.core.interceptor;

public interface ProgressListener {
    void onProgress(long bytesRead, long contentLength);
}
