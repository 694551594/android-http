package cn.yhq.http.core;

import android.content.Context;


public class HttpResponseListener<T> implements IHttpResponseListener<T> {

    @Override
    public void onResponse(Context context, int requestCode, T response, boolean isFromCache) {

    }

    @Override
    public void onException(Context context, Throwable t) {

    }
}
