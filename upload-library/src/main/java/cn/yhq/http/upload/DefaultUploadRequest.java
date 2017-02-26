package cn.yhq.http.upload;

import android.content.Context;

import java.io.File;
import java.util.List;

import cn.yhq.http.core.HttpRequestListener;
import cn.yhq.http.core.HttpResponseListener;
import cn.yhq.http.core.ICall;
import cn.yhq.http.core.ICancelable;
import cn.yhq.http.core.ProgressRequestBody;
import cn.yhq.http.core.RequestBodyUtils;
import cn.yhq.http.core.interceptor.ProgressListener;
import okhttp3.RequestBody;

/**
 * Created by Administrator on 2017/2/26.
 */

public class DefaultUploadRequest<UploadResponse> implements UploadRequest<UploadResponse> {
    private Context mContext;
    private UploadCallCreator<UploadResponse> mUploadCallCreator;

    public DefaultUploadRequest(Context context) {
        this.mContext = context;
    }

    public void setUploadCallCreator(UploadCallCreator<UploadResponse> uploadCallCreator) {
        this.mUploadCallCreator = uploadCallCreator;
    }

    private ICall<UploadResponse> createUploadCall(RequestBody requestBody) {
        return this.mUploadCallCreator.createUploadCall(requestBody);
    }

    @Override
    public void upload(List<File> files, final OnUploadProgressListener listener1, final OnUploadResponseListener<UploadResponse> listener2) {
        ICall<UploadResponse> call = this.createUploadCall(new ProgressRequestBody(RequestBodyUtils.createFileRequestBody(files), new ProgressListener() {
            @Override
            public void onProgress(long bytesRead, long contentLength) {
                int progress = (int) ((bytesRead * 1.0 / contentLength) * 100);
                if (progress < 0) {
                    progress = 0;
                } else if (progress > 100) {
                    progress = 100;
                }
                listener1.onProgress(progress);
            }
        }));
        call.execute(mContext, new HttpRequestListener() {
            @Override
            public void onComplete(int requestCode) {
                super.onComplete(requestCode);
                listener1.onComplete();
            }

            @Override
            public void onStart(Context context, ICancelable cancelable, int requestCode) {
                super.onStart(context, cancelable, requestCode);
                listener1.onStart();
            }
        }, new HttpResponseListener<UploadResponse>() {
            @Override
            public void onResponse(Context context, int requestCode, UploadResponse response, boolean isFromCache) {
                super.onResponse(context, requestCode, response, isFromCache);
                listener2.onSuccess(response);
            }

            @Override
            public void onException(Context context, Throwable t) {
                super.onException(context, t);
                listener2.onFailure(t);
            }
        });

    }
}