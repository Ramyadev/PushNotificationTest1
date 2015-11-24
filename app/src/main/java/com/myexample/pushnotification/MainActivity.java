package com.myexample.pushnotification;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.myexample.pushnotification.util.CommonClass;

import java.util.ArrayList;

public class MainActivity extends Activity {

    Button activate, deactivate,sendSMS;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activate = (Button)findViewById(R.id.button1);
        deactivate = (Button)findViewById(R.id.button2);
        sendSMS = (Button)findViewById(R.id.button3);

        SharedPreferences sharedpreferences = MainActivity.this.getSharedPreferences("Notification", Context.MODE_PRIVATE);
        final String regId= sharedpreferences.getString("regId","");
        activate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(new CommonClass().isInternetConnected(MainActivity.this)){
                    if(!regId.equals(""))
                            new CommonClass().displayToast(MainActivity.this, "Your device is already activated.Please click \"Send reg id to server\" button to communicate with server.");
                    else{
                        System.out.println("=== 111 = Activate");
                        System.out.println("=== 222 = Activate=" + PendingIntent.getBroadcast(view.getContext(), 0, new Intent(), 0));
                        Intent registerIntent = new Intent("com.google.android.c2dm.intent.REGISTER");
                        registerIntent.putExtra("app", PendingIntent.getBroadcast(view.getContext(),0,new Intent(),0));
                        registerIntent.putExtra("sender","649991313143");
                        registerIntent.setPackage("com.google.android.gms");
                        MainActivity.this.startService(registerIntent);
//                PendingIntent{14e1ab57: android.os.BinderProxy@294a644}
                    }
                }else
                    new CommonClass().displayToast(MainActivity.this, "Internet is required.");
            }
        });

        deactivate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedpreferences = MainActivity.this.getSharedPreferences("Notification", Context.MODE_PRIVATE);
                if (new CommonClass().isInternetConnected(MainActivity.this)) {
                    if (sharedpreferences.getString("regId", "").equals("")) {
                        new CommonClass().displayToast(MainActivity.this, "Please activate your device by clicking \"Activate\" button.");
                    } else {
                        Intent registerIntent = new Intent("com.google.android.c2dm.intent.UNREGISTER");
                        registerIntent.putExtra("app", PendingIntent.getBroadcast(view.getContext(), 0, new Intent(), 0));
                        MainActivity.this.startService(registerIntent);

                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putString("regid", "");
                        editor.commit();
                    }
                } else
                    new CommonClass().displayToast(MainActivity.this, "Internet is required.");
            }
        });
        sendSMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedpreferences = MainActivity.this.getSharedPreferences("Notification", Context.MODE_PRIVATE);
                if (sharedpreferences.getString("regId", "").equals("")) {
                    new CommonClass().displayToast(MainActivity.this, "Please activate your device by clicking \"Activate\" button.");
                } else {
                    callAlertBoxToSendSMS("+919865782625",sharedpreferences.getString("regId", ""));
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public void callAlertBoxToSendSMS(final String mobileNumber,final String msg) {

        String alertMessageStr = "";
        alertMessageStr = "Do you want to send SMS over network? Network may cost you, if you don't have free SMS.?";

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = MainActivity.this.getLayoutInflater();
        final View view = inflater.inflate(R.layout.create_group_alert, null);
        builder.setCancelable(false);
        builder.setView(view);

        final Dialog dialognew = builder.create();

        Button noButton = (Button) view.findViewById(R.id.noButton);
        Button yesButton = (Button) view.findViewById(R.id.yesButton);
        Button okButton = (Button) view.findViewById(R.id.okButton);
        okButton.setVisibility(View.GONE);
        TextView alertMessage = (TextView) view.findViewById(R.id.alertText);
        RelativeLayout titleLayout = (RelativeLayout) view.findViewById(R.id.titleLayout);
        titleLayout.setVisibility(View.GONE);
        alertMessage.setText(alertMessageStr);

        noButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                dialognew.dismiss();
            }
        });

        /**
         * Click yes button to confirm following process
         */
        yesButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                dialognew.dismiss();
                TelephonyManager tm = (TelephonyManager) MainActivity.this
                        .getSystemService(Context.TELEPHONY_SERVICE);
                if (tm.getSimState() != TelephonyManager.SIM_STATE_ABSENT) { // TO get the mobile number list
                    sendNetworkSMS(mobileNumber, msg);
                }else
                    new CommonClass().displayToast(MainActivity.this, "Please insert a SIM.");

            }
        });

        dialognew.show();
    }
    public synchronized void sendNetworkSMS(final String no, String msg) {
        try {

            String SENT = "sent";
            String DELIVERED = "delivered";

            Intent sentIntent = new Intent(SENT);
            /* Create Pending Intents */
            PendingIntent sentPI = PendingIntent.getBroadcast(MainActivity.this, 0,
                    sentIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent deliveryIntent = new Intent(DELIVERED);

            PendingIntent deliverPI = PendingIntent.getBroadcast(MainActivity.this, 0,
                    deliveryIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            /* Register for SMS send action */
            MainActivity.this.registerReceiver(new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {
                    String result = "";

                    switch (getResultCode()) {

                        case Activity.RESULT_OK:
                            result = "Transaction successful";
                            break;
                        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                            result = "Sending failed";
                            break;
                        case SmsManager.RESULT_ERROR_RADIO_OFF:
                            result = "Radio off";
                            break;
                        case SmsManager.RESULT_ERROR_NULL_PDU:
                            result = "No PDU defined";
                            break;
                        case SmsManager.RESULT_ERROR_NO_SERVICE:
                            result = "No service";
                            break;
                    }

                    Toast.makeText(context, result, Toast.LENGTH_LONG).show();
                }

            }, new IntentFilter(SENT));
            /* Register for Delivery event */
            MainActivity.this.registerReceiver(new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {
                    System.out.println("==Deliverd ;; number ==" + no);
                    Toast.makeText(context, "SMS Delivered to " + no,
                            Toast.LENGTH_LONG).show();
                }

            }, new IntentFilter(DELIVERED));

			/* Send SMS */
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(no, null, msg, sentPI, deliverPI);

        } catch (Exception ex) {
            Toast.makeText(MainActivity.this, ex.getMessage().toString(),
                    Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
    }

}

