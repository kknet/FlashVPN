package com.polestar.clone.client.stub;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import com.polestar.clone.R;
import com.polestar.clone.client.core.VirtualCore;
import com.polestar.clone.helper.utils.VLog;

import static android.os.Build.VERSION_CODES.O;


/**
 * @author Lody
 */
public class DaemonService extends Service {

    public static final int NOTIFY_ID = 1001;
	private static final String WAKE_ACTION = "com.polestar.messaging.wake";
    private final static int ALARM_INTERVAL = 30 * 60 * 1000;
    private final static int FIRST_WAKE_DELAY = 2000;

    public static String NOTIFICATION_CHANNEL_NAME = "Clone App Messaging";
    public static String NOTIFICATION_CHANNEL_ID = "_id_service_";
    public static String NOTIFICATION_CHANNEL_DESCRIPTION = "Clone App Messaging & Notification";
    public static String NOTIFICATION_TITLE = "Receiving messages for clones";
    public static String NOTIFICATION_DETAIL = "Keep running in background to receive messages";

    public static void startup(Context context) {
		try {
			context.startService(new Intent(context, DaemonService.class));
		}catch (Exception ex) {
			VLog.e("DaemonService",  ex.toString());
		}
    }

    public static void updateNotification(Context context) {
        try {
            context.startService(new Intent(context, InnerService.class));
        }catch (Exception ex) {
            VLog.e("DaemonService",  ex.toString());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        startup(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            if (Build.VERSION.SDK_INT >= O) {
                startForegroundService(new Intent(this, InnerService.class));
            } else {
                startService(new Intent(this, InnerService.class));
            }
            startForeground(NOTIFY_ID, new Notification());

            //发送唤醒广播来促使挂掉的UI进程重新启动起来
            String hostPackage = VirtualCore.get().getHostPkg();
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent alarmIntent = new Intent();
            alarmIntent.setAction(WAKE_ACTION);
            alarmIntent.setPackage(hostPackage);
            PendingIntent operation = PendingIntent.getBroadcast(this, 1111, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 2000, ALARM_INTERVAL, operation);
		}catch (Exception e) {
			VLog.logbug("Alarm", VLog.getStackTraceString(e));
		}

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public static final class InnerService extends Service {

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            //  remoteViews = new RemoteViews(this.getPackageName(), R.layout.quick_switch_notification);
            Notification notification;
            String pkg = VirtualCore.get().getHostPkg();
            if (Build.VERSION.SDK_INT >= O && !pkg.endsWith(".arm64")) {
                Intent start = getPackageManager().getLaunchIntentForPackage(pkg);
                start.addCategory(Intent.CATEGORY_LAUNCHER);
                start.setAction(Intent.ACTION_MAIN);
                //start.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //start.setClass(this, )


                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                String channel_id = NOTIFICATION_CHANNEL_ID;
                if (notificationManager.getNotificationChannel(channel_id) == null) {
                    int importance = NotificationManager.IMPORTANCE_HIGH;
                    NotificationChannel notificationChannel = new NotificationChannel(channel_id, NOTIFICATION_CHANNEL_NAME, importance);
//                notificationChannel.enableVibration(false);
                    notificationChannel.enableLights(false);
//                notificationChannel.setVibrationPattern(new long[]{0});
                    notificationChannel.setSound(null, null);
                    notificationChannel.setDescription(NOTIFICATION_CHANNEL_DESCRIPTION);
                    notificationChannel.setShowBadge(false);
                    //notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                    notificationManager.createNotificationChannel(notificationChannel);
                }
                Notification.Builder mBuilder =  new Notification.Builder(this, channel_id);
                mBuilder.setContentTitle(NOTIFICATION_TITLE)
                        .setContentText(NOTIFICATION_DETAIL)
                        .setSmallIcon(this.getResources().getIdentifier("ic_launcher", "mipmap", this.getPackageName()))
                        .setContentIntent(PendingIntent.getActivity(this,0, start, 0));
                notification = mBuilder.build();
                notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
            } else {
                notification = new Notification();
            }
            VLog.e("DaemonService", "Start foreground");
            startForeground(NOTIFY_ID , notification);
            if (Build.VERSION.SDK_INT < O) {
                stopForeground(true);
                stopSelf();
            }
            return super.onStartCommand(intent, flags, startId);
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    }


}
