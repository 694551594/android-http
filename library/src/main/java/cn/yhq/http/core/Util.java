package cn.yhq.http.core;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;

/**
 * Created by Yanghuiqiang on 2016/10/14.
 */

class Util {

    static String getDiskFileDir(Context context) {
        String cachePath = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalFilesDir(null).getPath();
        } else {
            cachePath = context.getFilesDir().getPath();
        }
        return cachePath;
    }

    static boolean isNetworkConnected(Context context) {
        NetworkInfo ni = getActiveNetwork(context);
        return ni != null && ni.isConnectedOrConnecting();
    }

    static NetworkInfo getActiveNetwork(Context context) {
        if (context == null) return null;
        ConnectivityManager mConnMgr =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (mConnMgr == null) return null;
        NetworkInfo aActiveInfo = mConnMgr.getActiveNetworkInfo();
        return aActiveInfo;
    }
}
