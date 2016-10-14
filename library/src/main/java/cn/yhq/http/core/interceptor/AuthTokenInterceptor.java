package cn.yhq.http.core.interceptor;

import java.io.IOException;

import cn.yhq.http.core.AuthTokenHandler;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Yanghuiqiang on 2016/10/14.
 */

public class AuthTokenInterceptor implements Interceptor {
    private AuthTokenHandler mAuthTokenHandler;

    public void setAuthTokenHandler(AuthTokenHandler authTokenHandler) {
        this.mAuthTokenHandler = authTokenHandler;
    }

    @Override
    public Response intercept(Interceptor.Chain chain) throws IOException {
        Request originalRequest = chain.request();
        if (mAuthTokenHandler == null) {
            return chain.proceed(originalRequest);
        }
        if (mAuthTokenHandler.isIgnoreUrl(originalRequest.url().toString())) {
            return chain.proceed(originalRequest);
        }
        String authValue = mAuthTokenHandler.getAuthValue(false);
        if (authValue == null || alreadyHasAuthorizationHeader(mAuthTokenHandler.getAuthName(), originalRequest)) {
            return chain.proceed(originalRequest);
        }
        Request authorised =
                originalRequest.newBuilder().header(mAuthTokenHandler.getAuthName(), authValue).build();
        return chain.proceed(authorised);
    }

    private static boolean alreadyHasAuthorizationHeader(String name, Request originalRequest) {
        return originalRequest.header(name) != null;
    }
}
