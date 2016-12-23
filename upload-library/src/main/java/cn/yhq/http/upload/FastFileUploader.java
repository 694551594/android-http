package cn.yhq.http.upload;

import android.content.Context;
import android.support.v4.content.Loader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.yhq.http.core.HttpRequester;
import cn.yhq.http.core.HttpResponseListener;
import retrofit2.Call;

/**
 * Created by Yanghuiqiang on 2016/12/23.
 */

public abstract class FastFileUploader<UploadResponse, FileUploadInfo> extends FileUploader<UploadResponse, FileUploadInfo> {
    private OnMd5CheckListener md5CheckListener;

    public FastFileUploader(Context context) {
        super(context);
        this.setOnMd5CheckListener(new DefaultOnMd5CheckListener(context));
    }

    protected abstract Call<UploadResponse> createMd5CheckCall(List<String> files, Map<String, String> md5);

    protected abstract boolean isUploaded(String file, String md5, FileUploadInfo uploadInfo);

    public final void fastUpload(List<String> files) {
        onMd5Check(files);
        onUpload(files);
    }

    public void setOnMd5CheckListener(OnMd5CheckListener listener) {
        md5CheckListener = listener;
    }

    protected void onMd5Check(final List<String> files) {
        if (md5CheckListener != null) {
            md5CheckListener.start();
        }
        Md5Checker task = new Md5Checker(mContext, files);
        task.registerListener(1, new Loader.OnLoadCompleteListener<Map<String, String>>() {
            @Override
            public void onLoadComplete(Loader<Map<String, String>> loader,
                                       final Map<String, String> data) {
                if (md5CheckListener != null) {
                    md5CheckListener.complate();
                }
                // 获取到了文件的md5
                if (data.isEmpty()) {
                    onUpload(files);
                } else {
                    Call<UploadResponse> call = createMd5CheckCall(files, data);
                    // 检测对应的md5文件是否已经上传
                    new HttpRequester.Builder<UploadResponse>(mContext)
                            .call(call)
                            .listener(new HttpResponseListener<UploadResponse>() {
                                @Override
                                public void onResponse(Context context, int requestCode, UploadResponse response, boolean isFromCache) {
                                    super.onResponse(context, requestCode, response, isFromCache);
                                    FileUploadInfo fileUploadInfo = getFileUploadInfo(response);
                                    List<String> files = new ArrayList<>();

                                    for (String file : files) {
                                        if (isUploaded(file, data.get(file), fileUploadInfo)) {
                                            continue;
                                        } else {
                                            files.add(file);
                                        }
                                    }

                                    // 秒传，直接返回
                                    if (files.isEmpty()) {
                                        dispatchResponse(fileUploadInfo);
                                    } else {
                                        onUpload(files);
                                    }
                                }

                                @Override
                                public void onException(Context context, Throwable t) {
                                    super.onException(context, t);
                                    dispatchException(t);
                                }
                            }).request();
                }
            }
        });
        task.forceLoad();
    }
}
