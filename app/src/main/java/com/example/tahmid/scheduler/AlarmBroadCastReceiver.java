package com.example.tahmid.scheduler;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

public class AlarmBroadCastReceiver extends BroadcastReceiver {



    @Override
    public void onReceive(Context context, Intent intent) {
        String channelId = "notification_channel";
        String channelName = "scheduler_channel";
        String notificationContentTitle = "Scheduled notification";
        String notificationContentDesc = "Notification description";

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build();

            NotificationChannel mChannel = new NotificationChannel(channelId,channelName,NotificationManager.IMPORTANCE_DEFAULT);
            mChannel.enableVibration(true);
            mChannel.setDescription("Channel description");
            mChannel.setShowBadge(true);
            mChannel.setSound(Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE+ "://"+context.getPackageName()+"/raw/uk_phone"),audioAttributes);

            if(manager!=null) {
                manager.createNotificationChannel(mChannel);
            }

        }
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context,channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(notificationContentTitle)
                .setContentText(notificationContentDesc)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSound(Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE+ "://"+context.getPackageName()+"/raw/uk_phone"));

        long mNotificationId = System.currentTimeMillis()/1000;
        manager.notify((int)mNotificationId,mBuilder.build());
    }
}
