package cn.yhq.http.upload;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/2/26.
 */

public interface Md5CheckRequest<T> {

    void checkMd5(List<File> files, Map<File, String> md5s, OnUploadResponseListener<T> listener);

}
