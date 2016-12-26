package cn.yhq.http.core;

import android.content.Context;

/**
 * Created by Yanghuiqiang on 2016/12/26.
 */

public interface ICallExecutor<T> {

    ICallResponse<T> execute(Context context, IHttpResponseListener<T> listener);

    ICallResponse<T> execute(Context context, IHttpRequestListener requestListener, IHttpResponseListener<T> responseListener);

}
