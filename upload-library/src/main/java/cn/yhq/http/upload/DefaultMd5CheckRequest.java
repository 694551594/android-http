package cn.yhq.http.upload;

import android.content.Context;

import java.io.File;
import java.util.List;
import java.util.Map;

import cn.yhq.http.core.HttpResponseListener;
import cn.yhq.http.core.ICall;

/**
 * Created by Administrator on 2017/2/26.
 */

public class DefaultMd5CheckRequest<UploadResponse> implements Md5CheckRequest<UploadResponse> {
    private Context mContext;
    private Md5CheckCallCreator<UploadResponse> mMd5CheckCallCreator;

    public DefaultMd5CheckRequest(Context context) {
        this.mContext = context;
    }

    public void setMd5CheckCallCreator(Md5CheckCallCreator<UploadResponse> md5CheckCallCreator) {
        this.mMd5CheckCallCreator = md5CheckCallCreator;
    }

    private ICall<UploadResponse> createUploadCall(List<File> files, Map<File, String> md5s) {
        return this.mMd5CheckCallCreator.createMd5CheckCall(files, md5s);
    }

    @Override
    public void checkMd5(List<File> files, Map<File, String> md5s, final OnUploadResponseListener<UploadResponse> listener) {
        ICall<UploadResponse> call = mMd5CheckCallCreator.createMd5CheckCall(files, md5s);
        // 检测对应的md5文件是否已经上传
        call.execute(mContext, new HttpResponseListener<UploadResponse>() {
            @Override
            public void onResponse(Context context, int requestCode, UploadResponse response, boolean isFromCache) {
                super.onResponse(context, requestCode, response, isFromCache);
                listener.onSuccess(response);
            }

            @Override
            public void onException(Context context, Throwable t) {
                super.onException(context, t);
                listener.onFailure(t);
            }
        });
    }

}
