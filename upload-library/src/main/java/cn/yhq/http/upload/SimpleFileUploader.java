package cn.yhq.http.upload;

import android.content.Context;

import java.util.List;

/**
 * Created by Yanghuiqiang on 2016/12/23.
 */

public abstract class SimpleFileUploader<UploadResponse> extends FastFileUploader<UploadResponse, List<FileUploadInfo>> {

    public SimpleFileUploader(Context context) {
        super(context);
    }

    public void addOnBatchUploadResponseListener(OnBatchUploadResponseListener listener) {
        this.addOnUploadResponseListener(listener);
    }

    @Override
    protected boolean isUploaded(String file, String md5, List<FileUploadInfo> fileUploadInfos) {
        for (FileUploadInfo fileUploadInfo : fileUploadInfos) {
            if (fileUploadInfo.getMd5().equals(md5)) {
                return true;
            }
        }
        return false;
    }
}
