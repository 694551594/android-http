package cn.yhq.http.core.interceptor;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 对request进度监听的拦截器
 *
 * Created by Yanghuiqiang on 2016/10/14.
 */

public class HttpRequestProgressInterceptor implements Interceptor {
    private ProgressListener progressListener;

    public void setProgressListener(ProgressListener progressListener) {
        this.progressListener = progressListener;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        if (request.body() != null) {
            request = request.newBuilder()
                    .post(new ProgressListener.ProgressRequestBody(request, request.body(), progressListener)).build();
        }
        return chain.proceed(request);
    }

}
