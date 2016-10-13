package cn.yhq.http.core;

import android.content.Context;

/**
 * Created by Yanghuiqiang on 2016/10/13.
 */

public interface IHttpResponseExceptionHandler {

    void onResponseException(Context context, Throwable t);
}
