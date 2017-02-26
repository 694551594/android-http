package cn.yhq.http.upload;

import android.content.Context;

import cn.yhq.utils.ToastUtils;

/**
 * Created by Yanghuiqiang on 2016/12/23.
 */

public abstract class DefaultOnUploadResponseListener<T> implements OnUploadResponseListener<T> {

    @Override
    public void onFailure(Context context, Throwable t) {
        ToastUtils.showToast(context, "文件上传失败");
    }
}
