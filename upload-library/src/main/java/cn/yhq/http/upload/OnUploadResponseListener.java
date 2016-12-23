package cn.yhq.http.upload;

/**
 * Created by Yanghuiqiang on 2016/12/23.
 */

public interface OnUploadResponseListener<T> {
    void onSuccess(T response);

    void onFailure(Throwable t);
}
