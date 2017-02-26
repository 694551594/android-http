package cn.yhq.http.upload;

import android.content.Context;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Yanghuiqiang on 2016/12/23.
 */

public class FastFileUploader<UploadResponse, FileUploadInfo> extends FileUploader<UploadResponse, FileUploadInfo> {
    private OnMd5CheckListener md5CheckListener;
    private Md5Strategy<FileUploadInfo> mMd5Strategy;

    public FastFileUploader(Context context) {
        super(context);
        this.setOnMd5CheckListener(new DefaultOnMd5CheckListener(context));
    }

    public void setMd5Strategy(Md5Strategy<FileUploadInfo> md5Strategy) {
        this.mMd5Strategy = md5Strategy;
    }

    public final void fastUpload(final List<File> files) {
        new Md5CheckTask(md5CheckListener) {
            @Override
            protected void onPostExecute(Map<File, String> data) {
                super.onPostExecute(data);
                //获取到了文件的md5
                if (data.isEmpty()) {
                    onUpload(files);
                } else {
                    onFastUpload(files, data, mMd5Strategy);
                }
            }
        }.execute(files.toArray(new File[files.size()]));
    }

    public final void fastUpload(File file) {
        List<File> files = new ArrayList<>();
        files.add(file);
        this.fastUpload(files);
    }

    public void setOnMd5CheckListener(OnMd5CheckListener listener) {
        md5CheckListener = listener;
    }

}
