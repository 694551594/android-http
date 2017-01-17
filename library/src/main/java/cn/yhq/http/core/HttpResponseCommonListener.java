package cn.yhq.http.core;

import android.content.Context;

/**
 * Created by Yanghuiqiang on 2016/12/26.
 */

public class HttpResponseCommonListener implements IHttpResponseCommonListener {
    private Object response;

    @Override
    public void onResponse(Context context, int requestCode, Object response, boolean isFromCache) {
        this.response = response;
    }

    @Override
    public void onException(Context context, Throwable t) {

    }

    protected <T> T obtainValue() {
        return obtainValue(response);
    }

    public static <T> T obtainValue(Object o) {
        return (T) o;
    }
}
