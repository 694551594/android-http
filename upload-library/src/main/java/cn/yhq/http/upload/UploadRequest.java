package cn.yhq.http.upload;

import java.io.File;
import java.util.List;

/**
 * Created by Administrator on 2017/2/26.
 */

public interface UploadRequest<T> {

    void upload(List<File> files, OnUploadProgressListener listener1, OnUploadResponseListener<T> listener2);

}
