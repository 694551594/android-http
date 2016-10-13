package cn.yhq.http.core;

import android.content.Context;


public interface IHttpResponseListener<T> {

    void onResponse(Context context, int requestCode, T response, boolean isFromCache);

    void onException(Context context, Throwable t);
}
