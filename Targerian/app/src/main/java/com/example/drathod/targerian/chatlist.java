package com.example.drathod.targerian;


import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class chatlist extends AppCompatActivity {
    DBHelper mydb;
    UsersAdapter adapter;
    SharedPreferences sharedPreferences;
    public static final String MyPref = "Mypref";
    public static final String UserId = "userid";
    public static final String NameOfUser= "name_of_user";
    public static final String Login_status = "active";
    public static final String LAST_UPDATED_TIME_CHATLIST = "counter_for_chatlist";
    public static final String IP= "ip";
    public static final String NOT_CONNECTED="notConnected";
    ArrayList<JSONObject> arrayOfUsersdb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chatlist);
        sharedPreferences = getSharedPreferences(MyPref, Context.MODE_PRIVATE);
        mydb = new DBHelper(this);
        String s1="{\"key\":\"chat_details\",\"chatid\":\"1\",\"name\":\"REMO\",\"is_group\":\"0\",\"latest_msg\":\"Hi Bhavan\",\"sender\":\"akhil\",\"time_of_latest_msg\":\"2017-04-16 07:53:35\"}";
        String s2="{\"key\":\"chat_details\",\"chatid\":\"2\",\"name\":\"ROMEO\",\"is_group\":\"1\",\"latest_msg\":\"Hi Bhavan!\",\"sender\":\"akhil orsu\",\"time_of_latest_msg\":\"2017-04-16 06:55:35\"}";

       // mydb.insert_chat_details(s1);
        //mydb.insert_chat_details(s2);
//        new JSONAsyncTask().execute("http://"+sharedPreferences.getString(IP,"")+"/targerian/chatbox.php", "userid=" + sharedPreferences.getInt(UserId, 0) + "&counter=" + sharedPreferences.getString(CounterForChatlist, "0000-00-00 00:00:00"));

        try {
            arrayOfUsersdb=mydb.getAllUsers();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // Create the adapter to convert the array to views
        adapter = new UsersAdapter(this, arrayOfUsersdb);
        // Attach the adapter to a ListView
        final ListView listView = (ListView) findViewById(R.id.chatItems);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(chatlist.this, ChatScreen.class);
                try {
                    JSONObject jsonObject=arrayOfUsersdb.get(position);
                    intent.putExtra("chatid", jsonObject.getInt("chatid"));//loook at  this once
                    intent.putExtra("is_group",jsonObject.getInt("is_group"));
                    intent.putExtra("title",jsonObject.getString("name"));


                } catch (JSONException e) {
                    e.printStackTrace();
                }
                chatlist.this.startActivity(intent);
            }
        });

        //Start our own service
        Intent intent = new Intent(chatlist.this, BackgroundManager.class);
        startService(intent);

        JSONObject jsonObject=new JSONObject();

        try {
            jsonObject.put("key","fetch_all_chats");
            jsonObject.put("id",sharedPreferences.getInt(UserId,0));
            jsonObject.put("threshhold",sharedPreferences.getString(LAST_UPDATED_TIME_CHATLIST,"1970-01-01 00:00:01"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        new Sync_chat_details().execute(jsonObject.toString());
//        Intent service = new Intent(chatlist.this, NotifyService.class);
//        chatlist.this.startService(service);
//
//        Intent intent = new Intent();
//        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
//        intent.setAction("com.example.Broadcast");
//        intent.putExtra("HighScore", 1000);
////        sendBroadcast(intent);
//        Intent service = new Intent(this, BackgroundManager.class);
//        this.startService(service);
////
//       if(! isMyServiceRunning(BackgroundManager.class)) {
//
//           Intent service2 = new Intent(this, BackgroundManager.class);
//           this.startService(service2);
//
//       }

    }


    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public class UsersAdapter extends ArrayAdapter<JSONObject> {
        public UsersAdapter(Context context, ArrayList<JSONObject> users) {
            super(context, 0, users);
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            JSONObject user = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.chatuser_template, parent, false);
            }
            // Lookup view for data population
            TextView Name = (TextView) convertView.findViewById(R.id.name);
            TextView Message = (TextView) convertView.findViewById(R.id.number);
            TextView Time = (TextView) convertView.findViewById(R.id.time);
            // Populate the data into the template view using the data object
            Log.d("yhjjjjjjj",user.toString());
            try {
                Name.setText(user.getString("name"));
                if(user.getInt("sender")==sharedPreferences.getInt(UserId,0)){
                    Message.setText("You: "+user.getString("latest_msg"));
                }else{
                    if(user.getInt("is_group")==1){

                        Message.setText(user.getString("sender_name")+": "+user.getString("latest_msg"));
                    }else{
                        Message.setText(user.getString("latest_msg"));
                    }
                }

                Time.setText(user.getString("time_of_latest_msg"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // Return the completed view to render on screen

            return convertView;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
       return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.action_refresh:
               // Toast.makeText(this, "Refreshing", Toast.LENGTH_SHORT).show();
                JSONObject jsonObject=new JSONObject();

                try {
                    jsonObject.put("key","fetch_all_chats");
                    jsonObject.put("id",sharedPreferences.getInt(UserId,0));
                    jsonObject.put("threshhold",sharedPreferences.getString(LAST_UPDATED_TIME_CHATLIST,"1970-01-01 00:00:01"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                new Sync_chat_details().execute(jsonObject.toString());
               // new JSONAsyncTask().execute("http://"+sharedPreferences.getString(IP,"")+"/targerian/chatbox.php", "userid=" + sharedPreferences.getInt(UserId, 0) + "&counter=" + sharedPreferences.getString(CounterForChatlist, "0000-00-00 00:00:00"));
                break;
            // action with ID action_settings was selected

            case R.id.action_logout:
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.apply();
//                try {
//                    // clearing app data
//                    String packageName = getApplicationContext().getPackageName();
//                    Runtime runtime = Runtime.getRuntime();
//                    runtime.exec("pm clear "+packageName);
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
                //((ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE)).clearApplicationUserData();
                mydb.onLogout();
                Intent intent = new Intent(chatlist.this, LoginActivity.class);
                chatlist.this.startActivity(intent);
                chatlist.this.finish();
                break;
            case R.id.action_changeIp:
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                final EditText edittext = new EditText(this);
                alert.setMessage("Enter new IP");
                alert.setTitle("Old IP: "+sharedPreferences.getString(IP,""));

                alert.setView(edittext);

                alert.setPositiveButton("Change IP", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //What ever you want to do with the value

                        String YouEditTextValue = edittext.getText().toString();
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(IP,YouEditTextValue);
                        editor.apply();
                        Log.d("------------------",YouEditTextValue);
                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // what ever you want to do with No option.
                    }
                });

                alert.show();

                break;
            case R.id.search:
                Toast.makeText(this, "fav selected", Toast.LENGTH_SHORT)
                        .show();
                Intent intent2=new Intent(this,searchActivity.class);
                this.startActivity(intent2);
                break;

            default:
                break;
        }

        return true;
    }

    class Sync_chat_details extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... params) {
            String output="";
            Integer Isconnected;
            Integer Issent;

            SocketManager socket = SocketManager.getInstance();
            if (!socket.isConnected()) {
                Isconnected=socket.connectSocket("10.196.31.69", 1997);
            }else{
                Isconnected=1;
            }
            if (Isconnected==1){
                Log.d("Status: ","connected to socket.................");

                Issent=socket.send(params[0]);
                if(Issent==1){
                    Log.d("Status: ","Data sent successfully.................");

                    String out= socket.recv();

                    if(out!=null){
                        JSONArray jsonArray= null;
                        try {
                            JSONObject jsonObjectOut=new JSONObject(out);
                            SharedPreferences.Editor editor=sharedPreferences.edit();
                            editor.putString(LAST_UPDATED_TIME_CHATLIST,jsonObjectOut.getString("up_time"));
                            editor.apply();
                            jsonArray =jsonObjectOut.getJSONArray("chatlist");
                            for(Integer i=0;i<jsonArray.length();i++){
                                JSONObject jsonObject=jsonArray.getJSONObject(i);
                                if(mydb.is_newChat(jsonObject.getInt("chatid"))){
                                    mydb.insert_chat_details(jsonObject.toString());
                                }else {
                                    mydb.update_chat_details(jsonObject.toString());
                                }

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }



                }else {
                    Log.d("Status: ","Data not sent.................");
                    output=NOT_CONNECTED;
                }


            }else {
                Log.i("Status: ","NOT connected to socket.................");
                output=NOT_CONNECTED;
            }



           // mydb.insert(out);
        return output;
        }

        protected void onPostExecute(String output) {
            if(output.equals(NOT_CONNECTED)){
                Toast.makeText(chatlist.this, "No internet connection", Toast.LENGTH_SHORT).show();
            }
            arrayOfUsersdb.clear();
            try {
                arrayOfUsersdb.addAll(mydb.getAllUsers());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            adapter.notifyDataSetChanged();
        }
    }

    //class JSONAsyncTask extends AsyncTask<String, Void, Boolean> {

//        ProgressDialog dialog;
//
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            dialog = new ProgressDialog(chatlist.this);
//            dialog.setMessage("Loading, please wait");
//            dialog.setTitle("Connecting server");
//            dialog.show();
//            dialog.setCancelable(false);
//
//        }
//
//        @Override
//        protected Boolean doInBackground(String... params) {
//            URL url = null;
//            HttpURLConnection conn = null;
//            BufferedReader reader = null;
//
//            try {
//
//
//                url = new URL(params[0]);
//                conn = (HttpURLConnection) url.openConnection();
//
//                conn.setRequestMethod("POST");
//                conn.setDoOutput(true);
//                conn.connect();
//                DataOutputStream writer = new DataOutputStream(conn.getOutputStream());
//                Log.d(".................", params[1]);
//                writer.writeBytes(params[1]);
//                writer.flush();
//                writer.close();
//
//                int responsecode = conn.getResponseCode();
//
//                //Toast.makeText(JSONMain.this, "response " + responsecode, Toast.LENGTH_SHORT ).show();
//                if (responsecode == 200) {
//
//                    InputStream stream = conn.getInputStream();
//                    reader = new BufferedReader(new InputStreamReader(stream));
//                    StringBuffer buffer = new StringBuffer();
//                    String line = "";
//
//                    while ((line = reader.readLine()) != null) {
//                        buffer.append(line + "\n");
//                        android.util.Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)
//
//                    }
//                    String data = buffer.toString();
//                    JSONObject jsono = new JSONObject(data);
//                    String counter = jsono.getString("counter");
//                    SharedPreferences.Editor editor = sharedPreferences.edit();
//                    editor.putString(CounterForChatlist, counter);
//                    editor.commit();
//
//                    JSONArray jarray = jsono.getJSONArray("response");
//
//                    for (int i = 0; i < jarray.length(); i++) {
//                        JSONObject object = jarray.getJSONObject(i);
//
//                        ChatUser chatUser = new ChatUser();
//                        String sender_name = object.getString("sender_firstname") + " " + object.getString("sender_lastname");
//
//                        if (object.getInt("is_group") == 1) {
//
//                            chatUser.setName(object.getString("name"));
//                            chatUser.setMessage(sender_name + ": " + object.getString("previousmessage"));
//                        } else {
//                            chatUser.setName(sender_name);
//                            chatUser.setMessage(object.getString("previousmessage"));
//                        }
//                        chatUser.setCreated_at(object.getString("time"));
//                        chatUser.setId(object.getInt("chatid"));
//                        if(mydb.is_newUser(chatUser.getId())) {
//                            mydb.addUser(chatUser);
//                        }else{
//                            mydb.updateUser(chatUser);
//                        }
//                    }
//
//                    return true;
//                }
//            } catch (MalformedURLException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            } catch (JSONException e) {
//                e.printStackTrace();
//            } finally {
//                if (conn != null) {
//                    conn.disconnect();
//                }
//                try {
//                    if (reader != null) {
//                        reader.close();
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            return false;
//        }
//
//        protected void onPostExecute(Boolean result) {
//            dialog.hide();
//            arrayOfUsersdb.clear();
//            arrayOfUsersdb.addAll(mydb.getAllUsers());
//            adapter.notifyDataSetChanged();
//            if (result == false)
//                Toast.makeText(getApplicationContext(), "Unable to fetch data from server", Toast.LENGTH_LONG).show();
//        }
//    }
}


