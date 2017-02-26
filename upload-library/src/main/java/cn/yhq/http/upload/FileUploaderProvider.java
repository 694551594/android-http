package cn.yhq.http.upload;

/**
 * Created by Administrator on 2017/2/26.
 */

public interface FileUploaderProvider<UploadResponse, FileUploadInfo> extends
        UploadCallCreator<UploadResponse>,
        Md5CheckCallCreator<UploadResponse>,
        Md5Strategy<FileUploadInfo>,
        UploadParser<UploadResponse, FileUploadInfo> {
}
