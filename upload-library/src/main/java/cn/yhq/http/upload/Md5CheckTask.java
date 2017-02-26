package cn.yhq.http.upload;

import android.os.AsyncTask;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Yanghuiqiang on 2016/12/23.
 */

public class Md5CheckTask extends AsyncTask<File, Integer, Map<File, String>> {
    private OnMd5CheckListener md5CheckListener;

    public Md5CheckTask(OnMd5CheckListener md5CheckListener) {
        this.md5CheckListener = md5CheckListener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        md5CheckListener.start();
    }

    private static Map<File, String> getFileMd5(File... files) {
        Map<File, String> md5s = new HashMap<>();
        for (File file : files) {
            try {
                String md5 = Md5Utils.getFileMD5String(file);
                md5s.put(file, md5);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return md5s;
    }

    @Override
    protected Map<File, String> doInBackground(File... params) {
        return getFileMd5(params);
    }

    @Override
    protected void onPostExecute(Map<File, String> fileStringMap) {
        super.onPostExecute(fileStringMap);
        md5CheckListener.complate();
    }

}
