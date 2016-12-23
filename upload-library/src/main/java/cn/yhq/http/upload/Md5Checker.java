package cn.yhq.http.upload;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Yanghuiqiang on 2016/12/23.
 */

public class Md5Checker extends AsyncTaskLoader<Map<String, String>> {
    private List<String> files;

    public Md5Checker(Context context, List<String> files) {
        super(context);
        this.files = files;
    }

    @Override
    public Map<String, String> loadInBackground() {
        return getFileMd5(files);
    }

    private static Map<String, String> getFileMd5(List<String> files) {
        Map<String, String> md5s = new HashMap<>();
        for (String file : files) {
            try {
                String md5 = Md5Utils.getFileMD5String(new File(file));
                md5s.put(file, md5);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return md5s;
    }
}
