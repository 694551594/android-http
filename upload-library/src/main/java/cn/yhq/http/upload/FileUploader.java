package cn.yhq.http.upload;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import cn.yhq.http.core.HttpRequestListener;
import cn.yhq.http.core.HttpRequester;
import cn.yhq.http.core.HttpResponseListener;
import retrofit2.Call;

/**
 * Created by Yanghuiqiang on 2016/12/23.
 */

public abstract class FileUploader<UploadResponse, FileUploadInfo> {
    protected Context mContext;
    private List<OnUploadResponseListener<FileUploadInfo>> responseListeners = new ArrayList<>();
    private OnUploadProgressListener progressListener;

    // 单个文件上传的回调
    private OnUploadResponseListener<FileUploadInfo> mUploadResponseListenerDispatcher = new OnUploadResponseListener<FileUploadInfo>() {
        @Override
        public void onSuccess(FileUploadInfo response) {
            for (OnUploadResponseListener<FileUploadInfo> listener : responseListeners) {
                listener.onSuccess(response);
            }
        }

        @Override
        public void onFailure(Throwable t) {
            for (OnUploadResponseListener<FileUploadInfo> listener : responseListeners) {
                listener.onFailure(t);
            }
        }
    };
    // 上传进度监听
    private OnUploadProgressListener mUploadProgressListenerDispatcher = new OnUploadProgressListener() {
        @Override
        public void onProgress(int progress) {
            progressListener.onProgress(progress);
        }
    };

    public FileUploader(Context context) {
        this.mContext = context;
        this.setOnUploadProgressListener(new DefaultOnUploadProgressListener(context));
    }

    protected abstract Call<UploadResponse> createUploadCall(List<String> files);

    protected abstract FileUploadInfo getFileUploadInfo(UploadResponse response);

    public final void upload(List<String> files) {
        onUpload(files);
    }

    protected void dispatchProgress(int progress) {
        mUploadProgressListenerDispatcher.onProgress(progress);
    }

    protected void dispatchResponse(FileUploadInfo response) {
        mUploadResponseListenerDispatcher.onSuccess(response);
    }

    protected void dispatchException(Throwable t) {
        mUploadResponseListenerDispatcher.onFailure(t);
    }

    public void setOnUploadProgressListener(OnUploadProgressListener listener) {
        progressListener = listener;
    }

    public void addOnUploadResponseListener(OnUploadResponseListener<FileUploadInfo> listener) {
        responseListeners.add(listener);
    }

    protected void onUpload(List<String> files) {
        Call<UploadResponse> call = this.createUploadCall(files);
        new HttpRequester.Builder<UploadResponse>(mContext)
                .call(call)
                .listener(new HttpRequestListener() {
                    @Override
                    public void onRequestProgress(boolean multipart, long bytesRead, long contentLength, boolean done) {
                        super.onRequestProgress(multipart, bytesRead, contentLength, done);
                        int progress = (int) ((bytesRead * 1.0 / contentLength) * 100);
                        if (progress < 0) {
                            progress = 0;
                        } else if (progress > 100) {
                            progress = 100;
                        }
                        dispatchProgress(progress);
                    }
                })
                .listener(new HttpResponseListener<UploadResponse>() {
                    @Override
                    public void onResponse(Context context, int requestCode, UploadResponse response, boolean isFromCache) {
                        super.onResponse(context, requestCode, response, isFromCache);
                        FileUploadInfo fileUploadInfo = getFileUploadInfo(response);
                        dispatchResponse(fileUploadInfo);
                    }

                    @Override
                    public void onException(Context context, Throwable t) {
                        super.onException(context, t);
                        dispatchException(t);
                    }
                }).request();
    }

}
