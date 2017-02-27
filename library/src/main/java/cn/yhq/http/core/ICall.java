package cn.yhq.http.core;

import retrofit2.Call;

/**
 * Created by Yanghuiqiang on 2016/10/14.
 */

public interface ICall<T> extends ICallExecutor<T> {

    Call<T> getRaw();

}
