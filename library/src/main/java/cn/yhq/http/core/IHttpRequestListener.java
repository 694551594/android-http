package cn.yhq.http.core;

import retrofit2.Call;

public interface IHttpRequestListener<T> {

  public void onStart(Call<T> call, int requestCode);

  public void onException(int requestCode, Throwable t);

  public void onComplete(int requestCode);

  public void onRequestProgress(boolean multipart, long bytesRead, long contentLength, boolean done);

  public void onResponseProgress(boolean multipart, long bytesRead, long contentLength, boolean done);
}
