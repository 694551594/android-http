package cn.yhq.http.upload;

import java.io.File;

/**
 * Created by Administrator on 2017/2/26.
 */

public interface Md5Strategy<FileUploadInfo> {
    boolean isUploaded(File file, String md5, FileUploadInfo uploadInfo);
}
