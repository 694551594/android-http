package cn.yhq.http.upload;

/**
 * Created by Administrator on 2017/2/26.
 */

public interface UploadParser<Response, Result> {
    Result getUploadResult(Response response);
}
