package cn.yhq.http.core.interceptor;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 对response进行进度监听的拦截器
 * <p>
 * Created by Yanghuiqiang on 2016/10/14.
 */

public class HttpResponseProgressInterceptor implements Interceptor {
    private ProgressListener progressListener;

    public void setProgressListener(ProgressListener progressListener) {
        this.progressListener = progressListener;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response originalResponse = chain.proceed(request);
        return originalResponse.newBuilder()
                .body(new ProgressResponseBody(request, originalResponse.body(), progressListener)).build();
    }
}
