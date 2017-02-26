package cn.yhq.http.upload;

import cn.yhq.http.core.ICall;
import okhttp3.RequestBody;

/**
 * Created by Administrator on 2017/2/26.
 */

public interface UploadCallCreator<Response> {

    ICall<Response> createUploadCall(RequestBody requestBody);

}
