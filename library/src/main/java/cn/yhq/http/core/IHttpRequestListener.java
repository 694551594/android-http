package cn.yhq.http.core;

import android.content.Context;

public interface IHttpRequestListener {

    void onStart(Context context, ICancelable cancelable, int requestCode);

    void onException(Context context, int requestCode, Throwable t);

    void onComplete(int requestCode);

}
