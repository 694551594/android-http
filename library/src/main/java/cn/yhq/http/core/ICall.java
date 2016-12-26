package cn.yhq.http.core;

import android.content.Context;

import retrofit2.Response;

/**
 * Created by Yanghuiqiang on 2016/10/14.
 */

public interface ICall<T> extends ICancelable {

    ICall<T> requestCode(int requestCode);

    ICall<T> cacheStrategy(CacheStrategy cacheStrategy);

    ICall<T> execute(Context context, IHttpResponseListener<T> listener);

    ICall<T> execute(Context context, IHttpRequestListener requestListener, IHttpResponseListener<T> responseListener);

    ICall<T> async(boolean isAsync);

    ICall<T> cacheStale(int cacheStale);

    ICall<T> exceptionProxy(boolean exceptionProxy);

    T getResponseBody();

    Response getResponse();

}
