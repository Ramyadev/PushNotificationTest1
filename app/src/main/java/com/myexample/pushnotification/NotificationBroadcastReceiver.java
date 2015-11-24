package com.myexample.pushnotification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

import com.myexample.pushnotification.util.CommonClass;

/**
 * Created by Ramya.D on 30-09-2015.
 */
public class NotificationBroadcastReceiver extends BroadcastReceiver {
    public static long notifyId=0;
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        System.out.println("==== action ==="+action);
        if(action.equals("com.google.android.c2dm.intent.REGISTRATION")){
            String reg_id = intent.getStringExtra("registration_id");
            System.out.println("==== reg_id ==="+reg_id);
            String error = intent.getStringExtra("error");
            System.out.println("==== error ==="+error);
            String unregister = intent.getStringExtra("unregistered");
            System.out.println("==== unregister ==="+unregister);

            new CommonClass().displayToast(context,"Your device has been registered successfully.\n Please click \"Send reg id to server\" button to communicate with server.");
            SharedPreferences sharedpreferences = context.getSharedPreferences("Notification", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString("regId", reg_id);
            editor.commit();


        }else{

            String data1 = intent.getStringExtra("data1");
            String data2 = intent.getStringExtra("data2");
            Toast.makeText(context,"Data 1 ="+data1+";;data 2=="+data2, Toast.LENGTH_LONG).show();
            System.out.println("==== data1 ==="+data1);
            System.out.println("==== data2 ===" + data2);


/*            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
//            mBuilder.setSmallIcon(R.drawable.notification_icon);
            mBuilder.setContentTitle(data1);
            mBuilder.setContentText(data2);

            mBuilder.build();

            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            // notificationID allows you to update the notification later on.
            mNotificationManager.notify(1, mBuilder.build());*/
            notifyId++;
            System.out.println("=== Notify Id =="+notifyId);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            @SuppressWarnings("deprecation")

            Notification notification = new Notification(R.drawable.notification,"New Message", System.currentTimeMillis());
            Intent notificationIntent = new Intent(context,ViewNotificationActivity.class);
            notificationIntent.putExtra("data1",data1);
            notificationIntent.putExtra("data2",data2);
            notificationIntent.putExtra("id", "" + notifyId);


            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,notificationIntent, 0);
//            notification.defaults=Notification.FLAG_ONLY_ALERT_ONCE;//+Notification.FLAG_AUTO_CANCEL;
            notification.defaults=Notification.FLAG_AUTO_CANCEL;
//            notification.la
            notification.setLatestEventInfo(context, data1, data2, pendingIntent);
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            notificationManager.notify(999, notification);

//            notificationManager.cancel(9999);
        }
    }
}
