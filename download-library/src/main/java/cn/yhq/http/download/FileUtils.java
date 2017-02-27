package cn.yhq.http.download;

public class FileUtils {
    public static String getFileSuffix(String name) {
        if (name == null || name.equals("")) {
            return "";
        }
        return name.substring(name.lastIndexOf(".") + 1, name.length());
    }
}
