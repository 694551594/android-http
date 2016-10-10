package cn.yhq.http.core;

import retrofit2.Call;

public class HttpRequestListener<T> implements IHttpRequestListener<T> {

  @Override
  public void onStart(Call<T> call, int requestCode) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onException(int requestCode, Throwable t) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onComplete(int requestCode) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onRequestProgress(boolean multipart, long bytesRead, long contentLength, boolean done) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onResponseProgress(boolean multipart, long bytesRead, long contentLength, boolean done) {
    // TODO Auto-generated method stub

  }

}
