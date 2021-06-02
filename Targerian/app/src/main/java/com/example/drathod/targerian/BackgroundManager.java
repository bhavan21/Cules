package com.example.drathod.targerian;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by bhavan on 16/4/17.
 */

public class BackgroundManager extends Service{

    private static BackgroundManager instance;
    final static String MY_ACTION = "MY_ACTION";

    private final IBinder mBinder = new MyBinder();
    private ArrayList<String> list = new ArrayList<String>();
    DBHelper mydb;
    SharedPreferences sharedPreferences;
    public static final String MyPref = "Mypref";
    public static final String UserId = "userid";
    public static final String Login_status = "active";
    public static final String CounterForChatlist = "counter_for_chatlist";
    public static final String IP= "ip";
    public static final String NOT_CONNECTED="notConnected";

    @Override
    public void onCreate() {
        super.onCreate();
       //new Network().execute();
        MyThread myThread = new MyThread();
        myThread.start();
    }

    public synchronized static BackgroundManager getInstance() {
        if (instance == null) {
            instance = new BackgroundManager();
        }
        return instance;
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        mydb = new DBHelper(this);

        sharedPreferences = getSharedPreferences(MyPref, Context.MODE_PRIVATE);
        mydb = new DBHelper(this);
        Log.d("hi","--------------------connected----------------yo");

        //`new NotifyService.JSONAsyncTask().execute("http://" + sharedPreferences.getString(IP, "") + "/targerian/chatbox.php", "userid=" + sharedPreferences.getInt(UserId, 0) + "&counter=" + sharedPreferences.getString(CounterForChatlist, "0000-00-00 00:00:00"));
       // new Network().execute("{\"key\":\"login\",\"username\":\"lakshman\",\"password\":\"lakshman@157\"}");

        Log.d("hi","--------------------connected----------------yepppppp");
//        MyThread myThread = new MyThread();
//        myThread.start();
//        new Thread(new Runnable() {
//            public void run() {
//
//                Integer Isconnected;
//                Integer Issent;
//
//                SocketManager socket = SocketManager.getInstance();
//                if (!socket.isConnected()) {
//                    Isconnected=socket.connectSocket("10.9.64.71", 1997);
//                }else{
//                    Isconnected=1;
//                }
//                if (Isconnected==1){
//                    android.util.Log.d("Status: ","connected to socket.................");
//
//                    Issent=socket.send("{\"key\":\"background\",\"id\":1}");
//                    if(Issent==1){
//                        Log.d("Status: ","Data sent successfully.................");
//
//                        for(Integer i=0;i<100;i++){
//                            String out= socket.recv();
//                            android.util.Log.d("Server says: ",out+".................");
//                            if(out!=null){
//
//                            }
//                            try {
//                                mydb.getAllMessages(1);
//                                Log.d("DB ACCESS", "................");
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//                        }
//
//
//                    }else {
//                        android.util.Log.d("Status: ","Data not sent.................");
//                    }
//
//
//                }else {
//                    android.util.Log.i("Status: ","NOT connected to socket.................");
//                }
//
//
//            }
//        }).start();


        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

    public class MyBinder extends Binder {
        BackgroundManager getService() {
            return BackgroundManager.this;
        }
    }


    public void onDestroy() {
        super.onDestroy();
        Log.e("STatus","SERVICE STOPPED................. ");
        Intent service = new Intent(this, BackgroundManager.class);
        //if (SocketManager.getInstance()!=null)this.startService(service);
    }

    public class MyThread extends Thread{
        @Override public void run() {
            // TODO Auto-generated method stub
            String output="";
            Integer Isconnected;
            Integer Issent;

            SocketManagerBackground socket = SocketManagerBackground.getInstance();
            if (!socket.isConnected()) {
                Isconnected = socket.connectSocket("10.196.31.69", 1997);
            } else {
                Isconnected = 1;
            }
            if (Isconnected == 1) {
                Log.d("Status: ", "connected to Background socket.................");
                JSONObject jsonObject=new JSONObject();
                try {
                    jsonObject.put("key","background");
                    jsonObject.put("id",sharedPreferences.getInt(UserId,0));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Issent = socket.send(jsonObject.toString());
                if (Issent == 1) {
                    Log.d("Status: ", "Data sent successfully.................");

                    for(Integer i=0;i<10;i++){
                        String out = socket.recv();

                        if (out != null) {
                            try {
                                JSONObject jsonObjectOut=new JSONObject(out);
                                if(jsonObjectOut.getString("key").equals("input_msg")){
                                    mydb.insert_message(jsonObjectOut.toString());

                                    addNotification(jsonObjectOut.getString("name"),jsonObjectOut.getString("sender_name")+": "+jsonObjectOut.getString("message"));

                                }
                                Intent intent = new Intent();
                                intent.setAction(MY_ACTION);
                                //intent.putExtra("DATAPASSED", i);
                                sendBroadcast(intent);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    String out = socket.recv();




                } else {
                    Log.d("Status: ", "Data not sent.................");
                    output = NOT_CONNECTED;
                }


            } else {
                Log.i("Status: ", "NOT connected to socket.................");
                output = NOT_CONNECTED;
            }

//            for(int i=0; i<10; i++){
//                try {
//                    Thread.sleep(5000);
//                    Intent intent = new Intent();
//                   intent.setAction(MY_ACTION);
//                   intent.putExtra("DATAPASSED", i);
//                    sendBroadcast(intent);
//                    Log.d("iterator",i+",..............");
//                    } catch (InterruptedException e) {
//                // TODO Auto-generated catch block
//                    e.printStackTrace();
//                   }
//                }
           stopSelf();
           }
    }
    class Network extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... params) {
            String output="";
            Integer Isconnected;
                Integer Issent;

                SocketManager socket = SocketManager.getInstance();
                if (!socket.isConnected()) {
                    Isconnected=socket.connectSocket("10.9.64.71", 1997);
                }else{
                    Isconnected=1;
                }
                if (Isconnected==1){
                    android.util.Log.d("Status: ","connected to socket.................");

                    Issent=socket.send("{\"key\":\"background\",\"id\":1}");
                    if(Issent==1){
                        Log.d("Status: ","Data sent successfully.................");


                            String out= socket.recv();
                            android.util.Log.d("Server says: ",out+".................");
                            if(out!=null){

                            }
                            try {
                                mydb.getAllMessages(1);
                                Log.d("DB ACCESS", "................");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }



                    }else {
                        android.util.Log.d("Status: ","Data not sent.................");
                    }


                }else {
                    android.util.Log.i("Status: ","NOT connected to socket.................");
                }


            // mydb.insert(out);
            return output;
        }

        protected void onPostExecute(String output) {
            new Network().execute();

        }
    }


    private void addNotification(String name,String message) {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(BackgroundManager.this)
                        .setSmallIcon(R.drawable.targerian_logo)
                        .setContentTitle(name)
                        .setContentText(message);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        builder.setSound(alarmSound);

         Intent notificationIntent = new Intent(this, chatlist.class);
         PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }

}
