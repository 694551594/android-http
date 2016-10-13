package cn.yhq.http.core;

import android.content.Context;

import retrofit2.Call;

public interface IHttpRequestListener<T> {

  void onStart(Context context, Call<T> call, int requestCode);

  void onException(int requestCode, Throwable t);

  void onComplete(int requestCode);

  void onRequestProgress(boolean multipart, long bytesRead, long contentLength, boolean done);

  void onResponseProgress(boolean multipart, long bytesRead, long contentLength, boolean done);
}
