package cn.yhq.http.core;

import android.content.Context;

public interface IHttpRequestListener<T> {

    void onStart(Context context, ICancelable cancelable, int requestCode);

    void onException(int requestCode, Throwable t);

    void onComplete(int requestCode);

    void onRequestProgress(boolean multipart, long bytesRead, long contentLength, boolean done);

    void onResponseProgress(boolean multipart, long bytesRead, long contentLength, boolean done);
}
