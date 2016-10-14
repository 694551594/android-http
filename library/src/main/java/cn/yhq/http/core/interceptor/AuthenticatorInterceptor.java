package cn.yhq.http.core.interceptor;

import java.io.IOException;

import cn.yhq.http.core.AuthTokenHandler;
import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

/**
 * 当验证失败的时候处理的一个拦截器
 * <p>
 * Created by Yanghuiqiang on 2016/10/14.
 */

public class AuthenticatorInterceptor implements Authenticator {
    private AuthTokenHandler mAuthTokenHandler;

    public void setAuthTokenHandler(AuthTokenHandler authTokenHandler) {
        this.mAuthTokenHandler = authTokenHandler;
    }

    @Override
    public Request authenticate(Route route, final Response response) throws IOException {
        if (mAuthTokenHandler == null) {
            return null;
        }
        String authValue = mAuthTokenHandler.getAuthValue(true);
        if (authValue == null) {
            return null;
        }
        return response.request().newBuilder().addHeader(mAuthTokenHandler.getAuthName(), authValue)
                .build();
    }

}
