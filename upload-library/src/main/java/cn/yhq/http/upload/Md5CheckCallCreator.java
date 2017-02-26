package cn.yhq.http.upload;

import java.io.File;
import java.util.List;
import java.util.Map;

import cn.yhq.http.core.ICall;

/**
 * Created by Administrator on 2017/2/26.
 */

public interface Md5CheckCallCreator<Response> {

    ICall<Response> createMd5CheckCall(List<File> files, Map<File, String> md5s);

}
