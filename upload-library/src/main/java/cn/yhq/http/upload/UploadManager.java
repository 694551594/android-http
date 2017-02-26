package cn.yhq.http.upload;

import android.content.Context;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/2/26.
 */

public class UploadManager<Response, Result> {
    private UploadParser<Response, Result> mUploadParser;
    private UploadRequest<Response> mUploadRequest;
    private Md5CheckRequest<Response> mMd5CheckRequest;

    public UploadManager() {

    }

    public void setUploadParser(UploadParser<Response, Result> uploadParser) {
        this.mUploadParser = uploadParser;
    }

    public void setUploadRequest(UploadRequest<Response> uploadRequest) {
        this.mUploadRequest = uploadRequest;
    }

    public void setMd5CheckRequest(Md5CheckRequest<Response> md5CheckRequest) {
        this.mMd5CheckRequest = md5CheckRequest;
    }

    public void checkMd5(List<File> files, Map<File, String> md5s, final OnUploadResponseListener<Result> listener) {
        mMd5CheckRequest.checkMd5(files, md5s, new OnUploadResponseListener<Response>() {
            @Override
            public void onSuccess(Response response) {
                Result result = mUploadParser.getUploadResult(response);
                listener.onSuccess(result);
            }

            @Override
            public void onFailure(Context context, Throwable t) {
                listener.onFailure(context, t);
            }
        });
    }

    public void upload(final List<File> files, final OnUploadProgressListener listener1, final OnUploadResponseListener<Result> listener2) {
        mUploadRequest.upload(files, listener1, new OnUploadResponseListener<Response>() {
            @Override
            public void onSuccess(Response response) {
                Result result = mUploadParser.getUploadResult(response);
                listener2.onSuccess(result);
            }

            @Override
            public void onFailure(Context context, Throwable t) {
                listener2.onFailure(context, t);
            }
        });
    }

}
