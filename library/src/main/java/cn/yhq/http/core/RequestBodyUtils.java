package cn.yhq.http.core;

import android.webkit.MimeTypeMap;

import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;


public class RequestBodyUtils {

  public static RequestBody createFileRequestBody(File file) {
    List<File> files = new ArrayList<File>();
    files.add(file);
    return createFileRequestBody(files);
  }

  public static RequestBody createFileRequestBody(File file, String callbackUrl,
      Map<String, Object> callbackBody) {
    List<File> files = new ArrayList<File>();
    files.add(file);
    return createFileRequestBody(files, callbackUrl, callbackBody);
  }

  public static RequestBody createFileRequestBody(File file, String callbackUrl,
      String callbackBody) {
    List<File> files = new ArrayList<File>();
    files.add(file);
    return createFileRequestBody(files, callbackUrl, callbackBody);
  }

  public static RequestBody createFileRequestBody(List<File> files) {
    return createFileRequestBody(files, null);
  }

  public static RequestBody createFileRequestBody(List<File> files, String callbackUrl,
      Map<String, Object> callbackBody) {
    return createFileRequestBody(files, callbackUrl, new Gson().toJson(callbackBody));
  }

  public static RequestBody createFileRequestBody(List<File> files, String callbackUrl,
      String callbackBody) {
    Map<String, String> exts = new HashMap<String, String>();
    exts.put("callbackUrl", callbackUrl);
    exts.put("callbackBody", callbackBody);
    return createFileRequestBody(files, exts);
  }

  private static RequestBody createFileRequestBody(List<File> files, Map<String, String> exts) {
    MultipartBody.Builder multipartBuilder = new MultipartBody.Builder();
    multipartBuilder.setType(MultipartBody.FORM);
    for (File file : files) {
      multipartBuilder.addFormDataPart("file", file.getName(),
          RequestBody.create(MediaType.parse(getMimeType(file)), file));
    }
    if (exts != null) {
      for (Entry<String, String> entry : exts.entrySet()) {
        multipartBuilder.addFormDataPart(entry.getKey(), entry.getValue());
      }
    }
    return multipartBuilder.build();
  }

  public static String getMimeType(File file) {
    String suffix = getFileSuffix(file.getName());
    if (suffix == null) {
      return "file/*";
    }
    String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(suffix);
    if (type != null && !type.isEmpty()) {
      return type;
    }
    return "file/*";
  }

  public static String getFileSuffix(String name) {
    if (name == null || name.equals("")) {
      return "";
    }
    return name.substring(name.lastIndexOf(".") + 1, name.length());
  }
}
