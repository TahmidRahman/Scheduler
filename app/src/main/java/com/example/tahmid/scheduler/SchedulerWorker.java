package com.example.tahmid.scheduler;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class SchedulerWorker extends Worker {
    private Context workerContext;
    public SchedulerWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        workerContext = context;
    }

    private void showNotification() {
        String channelId = "notification_channel";
        String channelName = "scheduler_channel";
        String notificationContentTitle = "Scheduled worker";
        String notificationContentDesc = "Notification description from worker";

        NotificationManager manager = (NotificationManager) workerContext.getSystemService(Context.NOTIFICATION_SERVICE);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(channelId,channelName,NotificationManager.IMPORTANCE_DEFAULT);
            mChannel.enableVibration(true);
            mChannel.setDescription("Channel description");
            mChannel.setShowBadge(true);

            if(manager!=null) {
                manager.createNotificationChannel(mChannel);
            }

        }
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(workerContext,channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(notificationContentTitle)
                .setContentText(notificationContentDesc)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        long mNotificationId = System.currentTimeMillis()/1000;
        manager.notify((int)mNotificationId,mBuilder.build());
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
           showNotification();
            return Result.success();
        } catch (Throwable error) {
            return Result.failure();
        }
    }
}
