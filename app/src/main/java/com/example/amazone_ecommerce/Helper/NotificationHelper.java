package com.example.amazone_ecommerce.Helper;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;
import android.os.Build;

import com.example.amazone_ecommerce.R;

public class NotificationHelper extends ContextWrapper {

    private static final String AMAZONE_CHANEL_ID = "com.example.amazone_ecommerce.AMAZONE";
    private static final String AMAZONE_CHANEL_NAME = "E commerce";

    private NotificationManager manager;

    public NotificationHelper(Context base) {
        super(base);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)  //only working with API 26 or higher....
            createChannel();
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createChannel() {
        NotificationChannel amazoneChannel=new NotificationChannel(AMAZONE_CHANEL_ID,
                AMAZONE_CHANEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT);
        amazoneChannel.enableLights(false);
        amazoneChannel.enableVibration(true);
        amazoneChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        getManager().createNotificationChannel(amazoneChannel);
    }

    public NotificationManager getManager() {
        if (manager==null)
            manager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        return manager;
    }

    @TargetApi(Build.VERSION_CODES.O)
    public Notification.Builder getAmazoneChannelNotiofication(String title, String body, PendingIntent contentIntent,
                                                               Uri soundUri)
    {
        return new Notification.Builder(getApplicationContext(),AMAZONE_CHANEL_ID)
                .setContentIntent(contentIntent)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setSound(soundUri)
                .setAutoCancel(false);
    }

    @TargetApi(Build.VERSION_CODES.O)
    public Notification.Builder getAmazoneChannelNotiofication(String title, String body,
                                                               Uri soundUri)
    {
        return new Notification.Builder(getApplicationContext(),AMAZONE_CHANEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setSound(soundUri)
                .setAutoCancel(false);
    }
}
