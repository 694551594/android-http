package cn.yhq.http.download;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2016/7/26.
 */
class NotificationProgressListener implements IDownloadProgressListener, IDownloaderListener {
    private FileDownloader.Builder builder;
    // 状态栏通知管理类
    private NotificationManager mNotificationManager;
    private Handler mHandler = new Handler();
    // 通知栏的id
    private int mNotificationIds;
    // 当前通知栏的id
    private static int ID;
    private Context mContext;
    private Timer timer = new Timer();
    private int progress;

    public NotificationProgressListener(FileDownloader.Builder builder) {
        this.mContext = builder.getContext();
        this.builder = builder;
        this.mNotificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        this.getNotificationId();
    }

    private int getNotificationId() {
        return mNotificationIds = ID++;
    }

    private void handleTask() {
        TimerTask task = new TimerTask() {

            @Override
            public void run() {
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        refreshNotification();
                    }

                });
            }
        };
        timer.schedule(task, 500, 500);
    }

    private void refreshNotification() {
        // 循环更新所有的下载任务的通知栏显示
        String contentText = new StringBuffer().append(progress).append("%").toString();
        NotificationCompat.Builder notificationBuilder = getNotificationBuilder();
        notificationBuilder.setContentText(contentText);
        notificationBuilder.setContentTitle(builder.getNotificationTitle());
        notificationBuilder.setProgress(100, progress, false);
        notify(notificationBuilder);
    }

    private NotificationCompat.Builder getNotificationBuilder() {
        // int iconRes =
        // FileIconManager.getInstance().getFileIcon(mDownloadConfigs.get(url).localFileName);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.drawable.ic_launcher).setAutoCancel(true);
        return notificationBuilder;
    }

    private void notify(NotificationCompat.Builder notificationBuilder) {
        mNotificationManager.notify(mNotificationIds, notificationBuilder.build());
    }

    @Override
    public void onProgress(int progress) {
        this.progress = progress;
    }

    @Override
    public void onException(String taskId, Throwable e) {
        timer.cancel();
    }

    @Override
    public void onSuccess(String taskId, File file) {
        Intent intent = IntentFactory.getFileIntent(file.getPath());
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
        NotificationCompat.Builder notificationBuilder = getNotificationBuilder();
        notificationBuilder.setContentTitle(builder.getNotificationTitle());
        notificationBuilder.setContentText("文件下载完成，点击查看。");
        notificationBuilder.setContentIntent(pendingIntent);
        notify(notificationBuilder);

        timer.cancel();
    }

    @Override
    public void onStart(String taskId) {
        handleTask();
    }

    @Override
    public void onCancel(String taskId) {
        timer.cancel();
    }

    @Override
    public void onPause(String taskId) {
    }

    @Override
    public void onResume(String taskId) {
    }

}
