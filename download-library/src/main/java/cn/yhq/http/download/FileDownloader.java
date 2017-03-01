package cn.yhq.http.download;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.yhq.dialog.core.DialogBuilder;
import cn.yhq.http.core.ICall;
import cn.yhq.utils.ToastUtils;
import okhttp3.ResponseBody;

/**
 * 文件下载器
 *
 * @author Yanghuiqiang
 *         <p>
 *         2016-1-22
 */
public final class FileDownloader {
    private final static String TAG = "FileDownloader";
    private final Map<String, DownloadTask> mDownloadTask = new HashMap<>();
    private final Map<String, String> mUrlMapper = new HashMap<>();
    private final List<IDownloadInterceptor> mDownloadInterceptors = new ArrayList<>();
    private static FileDownloader instance;

    public static FileDownloader getDownloader() {
        if (instance == null) {
            instance = new FileDownloader();
        }
        return instance;
    }

    public void register(IDownloadInterceptor downloadInterceptor) {
        this.mDownloadInterceptors.add(downloadInterceptor);
    }

    public void unregister(IDownloadInterceptor downloadInterceptor) {
        this.mDownloadInterceptors.remove(downloadInterceptor);
    }

    void removeDownloadTask(String id) {
        mDownloadTask.remove(id);
        mUrlMapper.remove(mUrlMapper.get(id));
    }

    static class DownloadResponseListener implements IDownloadResponseListener {
        private Context context;
        private boolean autoOpen;

        DownloadResponseListener(Context context, boolean autoOpen) {
            this.context = context;
            this.autoOpen = autoOpen;
        }

        @Override
        public void onException(String taskId, Throwable e) {
        }

        @Override
        public void onSuccess(String taskId, File file) {
            if (autoOpen) {
                openFile(context, file.getPath());
            }
        }
    }

    static class DownloaderInterceptorDispatcher implements IDownloaderListener {
        List<IDownloadInterceptor> downloadInterceptors;

        DownloaderInterceptorDispatcher() {
            this.downloadInterceptors = FileDownloader.getDownloader().mDownloadInterceptors;
        }

        @Override
        public void onException(String taskId, Throwable e) {
            for (IDownloadInterceptor listener : downloadInterceptors) {
                listener.onException(taskId, e);
            }
        }

        @Override
        public void onSuccess(String taskId, File file) {
            for (IDownloadInterceptor listener : downloadInterceptors) {
                listener.onSuccess(taskId, file);
            }
        }

        @Override
        public void onStart(String taskId) {
            for (IDownloadInterceptor listener : downloadInterceptors) {
                listener.onStart(taskId);
            }
        }

        @Override
        public void onCancel(String taskId) {
            for (IDownloadInterceptor listener : downloadInterceptors) {
                listener.onCancel(taskId);
            }
        }

        @Override
        public void onPause(String taskId) {
            for (IDownloadInterceptor listener : downloadInterceptors) {
                listener.onPause(taskId);
            }
        }

        @Override
        public void onResume(String taskId) {
            for (IDownloadInterceptor listener : downloadInterceptors) {
                listener.onResume(taskId);
            }
        }

    }

    static class DownloaderListener implements IDownloaderListener {

        private String downloadSuccessText;
        private String downloadFailureText;

        public DownloaderListener(String downloadSuccessText, String downloadFailureText) {
            this.downloadFailureText = downloadFailureText;
            this.downloadSuccessText = downloadSuccessText;
        }

        @Override
        public void onException(String taskId, Throwable e) {
            DownloadTask downloadTask = FileDownloader.getDownloader().getDownloadTaskById(taskId);
            if (downloadTask == null) {
                return;
            }
            if (!TextUtils.isEmpty(downloadFailureText)) {
                ToastUtils.showToast(downloadTask.getContext(), downloadFailureText);
            }
            FileDownloader.getDownloader().removeDownloadTask(taskId);
        }

        @Override
        public void onSuccess(String taskId, File file) {
            DownloadTask downloadTask = FileDownloader.getDownloader().getDownloadTaskById(taskId);
            if (downloadTask == null) {
                return;
            }
            if (!TextUtils.isEmpty(downloadSuccessText)) {
                ToastUtils.showToast(downloadTask.getContext(), downloadSuccessText);
            }
            FileDownloader.getDownloader().removeDownloadTask(taskId);
        }

        @Override
        public void onStart(String taskId) {
        }

        @Override
        public void onCancel(String taskId) {
            FileDownloader.getDownloader().removeDownloadTask(taskId);
        }

        @Override
        public void onPause(String taskId) {
        }

        @Override
        public void onResume(String taskId) {
        }

    }

    public static class Builder {
        private Context context;
        private String downloadDir;
        private File localFile;
        // 通知栏显示的标题
        private String notificationTitle;
        // 请求头
        private Map<String, String> requestHeader = new HashMap<>();
        private List<IDownloadResponseListener> downloadResponseListeners;
        private List<IDownloadProgressListener> downloadProgressListeners;
        private List<IDownloaderListener> downloaderListeners;
        // 如果文件存在的处理方式
        private FileExistHandler mFileExistHandler;
        private boolean autoOpenDownloadSuccess = true;
        private IHttpDownloader httpDownloader;
        private ICall<ResponseBody> call;

        private String downloadSuccessText = "文件下载成功";
        private String downloadFailureText = "文件下载失败，请稍后重试";

        public enum FileExistHandler {
            // 重新下载、询问、打开
            REDOWNLOAD, ASK, OPEN, RENAME, NONE;
        }

        // 进度显示的形式
        public interface ProgressStyle {
            int NOTIFACTION = 1;
            int PROGRESS_DIALOG = 2;
            int LOADING_DIALOG = 4;
        }

        public Builder(Context context) {
            this.context = context;
            this.requestHeader = new HashMap<>();
            this.downloaderListeners = new ArrayList<>();
            this.downloadProgressListeners = new ArrayList<>();
            this.downloadResponseListeners = new ArrayList<>();
            this.dir(cn.yhq.utils.FileUtils.getDownloadPath(getContext()));
        }

        public Builder build() {
            this.httpDownloader(new RetrofitHttpDownloader(call));
            this.listener(new DownloaderInterceptorDispatcher());
            this.listener(new DownloaderListener(downloadSuccessText, downloadFailureText));
            this.listener(new DownloadResponseListener(this.context, autoOpenDownloadSuccess));
            return this;
        }

        public String download() {
            build();
            return FileDownloader.getDownloader().download(this);
        }

        public Builder call(ICall<ResponseBody> call) {
            this.call = call;
            return this;
        }

        public Builder httpDownloader(IHttpDownloader httpDownloader) {
            this.httpDownloader = httpDownloader;
            return this;
        }

        public Builder localName(String localName) {
            this.localFile = new File(downloadDir, localName);
            return this;
        }

        public Builder dir(String downloadDir) {
            this.downloadDir = downloadDir;
            return this;
        }

        public Builder localPath(String localFilePath) {
            this.localFile = new File(localFilePath);
            return this;
        }

        public Builder localFile(File localFile) {
            this.localFile = localFile;
            return this;
        }

        public Builder notificationTitle(String notificationTitle) {
            this.notificationTitle = notificationTitle;
            return this;
        }

        public Builder header(String key, String value) {
            this.requestHeader.put(key, value);
            return this;
        }

        public Builder header(Map<String, String> requestHeader) {
            this.requestHeader = requestHeader;
            return this;
        }

        public Builder listener(IDownloadResponseListener listener) {
            this.downloadResponseListeners.add(listener);
            return this;
        }

        public Builder listener(IDownloadProgressListener listener) {
            this.downloadProgressListeners.add(listener);
            return this;
        }

        public Builder listener(IDownloaderListener listener) {
            this.downloaderListeners.add(listener);
            return this;
        }

        public Builder progressStyle(int progressStyle) {
            this.notificationTitle(this.localFile.getName());
            NotificationProgressListener notificationProgressListener =
                    new NotificationProgressListener(this);
            DialogProgressListener dialogProgressListener = new DialogProgressListener(this);
            DialogLoadingListener dialogLoadingListener = new DialogLoadingListener(this);
            if ((progressStyle & ProgressStyle.NOTIFACTION) == ProgressStyle.NOTIFACTION) {
                this.listener((IDownloadProgressListener) notificationProgressListener);
                this.listener((IDownloaderListener) notificationProgressListener);
            }
            if ((progressStyle & ProgressStyle.PROGRESS_DIALOG) == ProgressStyle.PROGRESS_DIALOG) {
                this.listener((IDownloadProgressListener) dialogProgressListener);
                this.listener((IDownloaderListener) dialogProgressListener);
            } else if ((progressStyle & ProgressStyle.LOADING_DIALOG) == ProgressStyle.LOADING_DIALOG) {
                this.listener((IDownloadProgressListener) dialogLoadingListener);
                this.listener((IDownloaderListener) dialogLoadingListener);
            }
            return this;
        }

        public Builder fileExistHandler(FileExistHandler mFileExistHandler) {
            this.mFileExistHandler = mFileExistHandler;
            return this;
        }

        public Builder autoOpenDownloadSuccess(boolean autoOpenDownloadSuccess) {
            this.autoOpenDownloadSuccess = autoOpenDownloadSuccess;
            return this;
        }

        public Builder downloadFailureText(int downloadFailureText) {
            this.downloadFailureText = context.getString(downloadFailureText);
            return this;
        }

        public Builder downloadSuccessText(int downloadSuccessText) {
            this.downloadSuccessText = context.getString(downloadSuccessText);
            return this;
        }

        public Builder downloadFailureText(String downloadFailureText) {
            this.downloadFailureText = downloadFailureText;
            return this;
        }

        public Builder downloadSuccessText(String downloadSuccessText) {
            this.downloadSuccessText = downloadSuccessText;
            return this;
        }

        public Context getContext() {
            return context;
        }

        public String getDownloadDir() {
            return downloadDir;
        }

        public File getLocalFile() {
            return localFile;
        }

        public String getNotificationTitle() {
            return notificationTitle;
        }

        public Map<String, String> getRequestHeader() {
            return requestHeader;
        }

        public List<IDownloadResponseListener> getDownloadResponseListeners() {
            return downloadResponseListeners;
        }

        public List<IDownloadProgressListener> getDownloadProgressListeners() {
            return downloadProgressListeners;
        }

        public List<IDownloaderListener> getDownloaderListeners() {
            return downloaderListeners;
        }

        public FileExistHandler getFileExistHandler() {
            return mFileExistHandler;
        }

        public boolean isAutoOpenDownloadSuccess() {
            return autoOpenDownloadSuccess;
        }

        public IHttpDownloader getHttpDownloader() {
            return httpDownloader;
        }

    }

    public DownloadTask getDownloadTaskById(String id) {
        return mDownloadTask.get(id);
    }

    /**
     * 如果下载前检测到文件存在了，该怎么处理
     *
     * @param builder
     * @return 返回true就不会继续下面的代码了。
     */
    private boolean handleFileExists(final Builder builder) {
        Context context = builder.context;
        switch (builder.mFileExistHandler) {
            default:
            case ASK:
                // 询问
                DialogBuilder.alertDialog(context).setMessage("该文件已经下载完成，您需要重新下载吗？")
                        .setOnPositiveButtonClickListener(new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                builder.localFile.delete();
                                buildAndStart(builder);
                            }
                        }).create().show();
                return true;
            case OPEN:
                // 打开
                openFile(context, builder.localFile.getPath());
                return true;
            case REDOWNLOAD:
                // 重新下载，会覆盖
                builder.localFile.delete();
                return false;
            case RENAME:
                File newFile = getNextFilePath(builder.localFile.getPath());
                builder.localFile = newFile;
                return false;
            case NONE:
                for (IDownloadResponseListener listener : builder.downloadResponseListeners) {
                    listener.onSuccess("-1", builder.localFile);
                }
                return true;
        }
    }

    private static File getNextFilePath(String filePath) {
        int index = 0;
        while (true) {
            index++;
            String suffix = FileUtils.getFileSuffix(filePath);
            String path = filePath.substring(0, filePath.lastIndexOf(".")) + "(" + index + ")." + suffix;
            File file = new File(path);
            if (!file.exists()) {
                return file;
            }
        }
    }

    private boolean check(Builder builder) {
        if (builder.localFile == null) {
            return false;
        }
        if (builder.localFile.exists()) {
            if (handleFileExists(builder)) {
                return false;
            }
        }

        return true;
    }

    private DownloadTask buildDownloadTask(Builder builder) {
        DownloadTask downloadTask =
                new DownloadTask.Builder(builder.context).downloader(builder.httpDownloader)
                        .file(builder.localFile).header(builder.requestHeader)
                        .listener(builder.downloaderListeners).progress(builder.downloadProgressListeners)
                        .response(builder.downloadResponseListeners).build();

        mDownloadTask.put(downloadTask.getId(), downloadTask);

        return downloadTask;
    }

    String download(Builder builder) {
        if (!check(builder)) {
            return null;
        }
        String url = null;
        if (builder.call != null) {
            url = builder.call.getRaw().request().url().toString();
        }
        if (url != null) {
            String taskId = this.mUrlMapper.get(url);
            if (taskId != null) {
                DownloadTask downloadTask = this.getDownloadTaskById(taskId);
                if (downloadTask != null) {
                    if (downloadTask.getStatus() == DownloadTask.DownloadStatus.DOWNLOADING) {
                        ToastUtils.showToast(builder.getContext(), "该文件正在下载中");
                        return taskId;
                    } else {
                        downloadTask.resume();
                        return taskId;
                    }
                }
            }
        }

        String taskId = buildAndStart(builder);
        this.mUrlMapper.put(url, taskId);
        return taskId;
    }

    /**
     * 使用Intent打开文件
     *
     * @param context
     * @param path
     */
    public static void openFile(Context context, String path) {
        try {
            Intent intent = IntentFactory.getFileIntent(path);
            context.startActivity(intent);
        } catch (Exception e) {
            ToastUtils.showToast(context, "没有找到打开此文件的客户端");
        }
    }

    public String buildAndStart(Builder builder) {
        DownloadTask downloadTask = buildDownloadTask(builder);
        start(downloadTask.getId());
        return downloadTask.getId();
    }

    public void start(String id) {
        DownloadTask downloadTask = getDownloadTaskById(id);
        if (downloadTask != null) {
            downloadTask.start();
        }
    }

    public void pause(String id) {
        DownloadTask downloadTask = getDownloadTaskById(id);
        if (downloadTask != null) {
            downloadTask.pause();
        }
    }

    public void cancel(String id) {
        DownloadTask downloadTask = getDownloadTaskById(id);
        if (downloadTask != null) {
            downloadTask.cancel();
        }
    }

    public void resume(String id) {
        DownloadTask downloadTask = getDownloadTaskById(id);
        if (downloadTask != null) {
            downloadTask.resume();
        }
    }
}
