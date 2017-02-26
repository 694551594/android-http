package cn.yhq.http.upload;

import android.content.Context;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Yanghuiqiang on 2016/12/23.
 */

public class FileUploader<UploadResponse, FileUploadInfo> {
    private Context mContext;
    private List<OnUploadResponseListener<FileUploadInfo>> responseListeners = new ArrayList<>();
    private UploadManager<UploadResponse, FileUploadInfo> mUploadManager;
    private OnUploadProgressListener mOnUploadProgressListener;

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

    public FileUploader(Context context) {
        this.mContext = context;
        this.mUploadManager = new UploadManager<>();
        this.setOnUploadProgressListener(new DefaultOnUploadProgressListener(context));
    }

    public final void upload(List<File> files) {
        onUpload(files);
    }

    public final void upload(File file) {
        List<File> files = new ArrayList<>();
        files.add(file);
        this.upload(files);
    }

    public void addOnUploadResponseListener(OnUploadResponseListener<FileUploadInfo> listener) {
        responseListeners.add(listener);
    }

    public void onFastUpload(final List<File> files, final Map<File, String> md5s, final Md5Strategy md5Strategy) {
        mUploadManager.checkMd5(files, md5s, new OnUploadResponseListener<FileUploadInfo>() {
            @Override
            public void onSuccess(FileUploadInfo response) {
                List<File> files = new ArrayList<>();

                for (File file : files) {
                    if (md5Strategy.isUploaded(file, md5s.get(file), response)) {
                        continue;
                    } else {
                        files.add(file);
                    }
                }

                // 秒传，直接返回
                if (files.isEmpty()) {
                    mUploadResponseListenerDispatcher.onSuccess(response);
                } else {
                    onUpload(files);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                mUploadResponseListenerDispatcher.onFailure(t);
            }
        });
    }

    public void onUpload(List<File> files) {
        mUploadManager.upload(files, mOnUploadProgressListener, new OnUploadResponseListener<FileUploadInfo>() {

            @Override
            public void onSuccess(FileUploadInfo response) {
                mUploadResponseListenerDispatcher.onSuccess(response);
            }

            @Override
            public void onFailure(Throwable t) {
                mUploadResponseListenerDispatcher.onFailure(t);
            }
        });
    }

    public void setOnUploadProgressListener(OnUploadProgressListener onUploadProgressListener) {
        this.mOnUploadProgressListener = onUploadProgressListener;
    }

    public void setUploadParser(UploadParser<UploadResponse, FileUploadInfo> uploadParser) {
        mUploadManager.setUploadParser(uploadParser);
    }

    public void setUploadRequest(UploadRequest<UploadResponse> uploadRequest) {
        mUploadManager.setUploadRequest(uploadRequest);
    }

    public void setMd5CheckRequest(Md5CheckRequest<UploadResponse> md5CheckRequest) {
        mUploadManager.setMd5CheckRequest(md5CheckRequest);
    }

    public void setMd5CheckRequest(Md5CheckCallCreator<UploadResponse> creator) {
        DefaultMd5CheckRequest<UploadResponse> request = new DefaultMd5CheckRequest<>(mContext);
        request.setMd5CheckCallCreator(creator);
        this.setMd5CheckRequest(request);
    }

    public void setUploadRequest(UploadCallCreator<UploadResponse> creator) {
        DefaultUploadRequest<UploadResponse> request = new DefaultUploadRequest<>(mContext);
        request.setUploadCallCreator(creator);
        this.setUploadRequest(request);
    }

}
