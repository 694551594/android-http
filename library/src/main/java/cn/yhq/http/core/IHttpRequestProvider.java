package cn.yhq.http.core;

import retrofit2.Call;

public interface IHttpRequestProvider<T> {

  int getRequestCode();

  Call<T> execute(int requestCode);

  CacheStrategy getCacheStrategy();
}
