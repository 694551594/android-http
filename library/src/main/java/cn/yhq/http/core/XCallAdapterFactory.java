package cn.yhq.http.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Retrofit;

/**
 * Created by Yanghuiqiang on 2016/10/14.
 */

public final class XCallAdapterFactory extends CallAdapter.Factory {

    XCallAdapterFactory() {
    }

    @Override
    public CallAdapter<Object, XCall<Object>> get(Type returnType, Annotation[] annotations, final Retrofit retrofit) {
        if (getRawType(returnType) != ICall.class) {
            return null;
        }
        final Type responseType = getParameterUpperBound(0, (ParameterizedType) returnType);
        return new CallAdapter<Object, XCall<Object>>() {
            @Override
            public Type responseType() {
                return responseType;
            }

            @Override
            public XCall<Object> adapt(Call<Object> call) {
                return new XCall<>(retrofit, call, responseType);
            }
        };

    }

}
