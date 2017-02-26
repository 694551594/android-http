package cn.yhq.http.upload;

import android.content.Context;

/**
 * Created by Yanghuiqiang on 2016/12/23.
 */

public interface OnUploadResponseListener<T> {
    void onSuccess(T response);

    void onFailure(Context context, Throwable t);
}
