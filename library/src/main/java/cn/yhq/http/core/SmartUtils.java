package cn.yhq.http.core;

import android.util.Log;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.Buffer;
import retrofit2.Converter;
import retrofit2.Retrofit;

public final class SmartUtils {
    /*
     * TODO: Do an inverse iteration instead so that the latest Factory that supports <T>
     * does the job?
     */
    @SuppressWarnings("unchecked")
    public static <T> byte[] requestToBytes(Retrofit retrofit, T data, Type dataType,
                                             Annotation[] parameterAnnotations, Annotation[] methodAnnotations) {
        for (Converter.Factory factory : retrofit.converterFactories()) {
            if (factory == null) continue;
            Converter<T, RequestBody> converter =
                    (Converter<T, RequestBody>) factory.requestBodyConverter(dataType, parameterAnnotations, methodAnnotations, retrofit);

            if (converter != null) {
                Buffer buff = new Buffer();
                try {
                    converter.convert(data).writeTo(buff);
                } catch (IOException ioException) {
                    continue;
                }

                return buff.readByteArray();
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T> T bytesToResponse(Retrofit retrofit, Type dataType, Annotation[] annotations,
                                        byte[] data) {
        for (Converter.Factory factory : retrofit.converterFactories()) {
            if (factory == null) continue;
            Converter<ResponseBody, T> converter =
                    (Converter<ResponseBody, T>) factory.responseBodyConverter(dataType, annotations, retrofit);

            if (converter != null) {
                try {
                    return converter.convert(ResponseBody.create(null, data));
                } catch (IOException | NullPointerException exc) {
                    Log.e("SmartCall", "", exc);
                }
            }
        }

        return null;
    }
}
