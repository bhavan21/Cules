package com.example.drathod.targerian;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;



import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Handler;

import static android.accounts.AccountManager.KEY_PASSWORD;


public class ChatScreen extends AppCompatActivity {
    MyReceiver myReceiver;
    DBHelper mydb;
    UsersAdapter adapter;
    SharedPreferences sharedPreferences;
    public static final String MyPref = "Mypref";
    public static final String UserId= "userid";
    public static final String NameOfUser= "name_of_user";
    public static final String IP= "ip";
    public static final String NOT_CONNECTED="notConnected";

    Integer chatid;
    Integer is_group;
    ArrayList<JSONObject> arrayOfUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chatscreen);

        //ChatScreen.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);



        sharedPreferences = getSharedPreferences(MyPref, Context.MODE_PRIVATE);

        android.util.Log.e("shared preferences ----", "userid"+sharedPreferences.getInt(UserId,0) + "--------------------------");

        Bundle bundle = getIntent().getExtras();

        chatid = bundle.getInt("chatid");
        is_group=bundle.getInt("is_group");
        setTitle(bundle.getString("title"));
        //getActionBar().setDisplayHomeAsUpEnabled(true);
//
        //Toast.makeText(this, "Chat id " + chatid, Toast.LENGTH_SHORT).show();

        mydb = new DBHelper(this);
        //sync when chatscreen is open
        JSONObject jsonObject=new JSONObject();
        try {
            jsonObject.put("key","fetch_a_chat");
            jsonObject.put("id",sharedPreferences.getInt(UserId,0));
            jsonObject.put("chatid",chatid);
            jsonObject.put("threshhold",mydb.getLastUpdatedTime(chatid));
            new Sync_messages().execute(jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        arrayOfUsers=new ArrayList<>();

//        try {
//            arrayOfUsers=mydb.getAllMessages(chatid);
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }

// Create the adapter to convert the array to views
      adapter = new UsersAdapter(this, arrayOfUsers);
// Attach the adapter to a ListView
       final  ListView listView = (ListView) findViewById(R.id.lvItems);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //Log.d("item",listView.getSelectedItem().toString()+"@@@@@@@@@@@@@@");

            }
        });
       final EditText editText = (EditText) findViewById(R.id.messageBox);
        final ImageView button = (ImageView) findViewById(R.id.sendButton);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText.getText().length() != 0) {
               // arrayOfUsers.add(new Log(editText.getText().toString(), "12:23", 1));
                //adapter.add(new Log(editText.getText().toString(), "12:23", 1));
                    JSONObject jsonObject=new JSONObject();

                    try {
                        jsonObject.put("key","insert");
                        jsonObject.put("id",sharedPreferences.getInt(UserId,0));
                        jsonObject.put("chatid",chatid);
                        jsonObject.put("message",editText.getText().toString());
                        jsonObject.put("sender",sharedPreferences.getInt(UserId,0));
                        jsonObject.put("show",0);
                        new sendMessage().execute(jsonObject.toString());
                        editText.getText().clear();
                        arrayOfUsers.add(jsonObject);
                        adapter.notifyDataSetChanged();
                       // mydb.insert_message(jsonObject.toString());


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }



                }
            }
        });



//        BackgroundManager.getInstance().registerReceiver(
//                new BroadcastReceiver() {
//                    @Override
//                    public void onReceive(Context context, Intent intent) {
//
//                    }
//                }, new IntentFilter()
//        );

    }


    @Override
    protected void onStart() {
        // TODO Auto-generated method stub

       //Register BroadcastReceiver
        //to receive event from our service
       myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
       intentFilter.addAction(BackgroundManager.MY_ACTION);
        registerReceiver(myReceiver, intentFilter);

        //Start our own service
        Intent intent = new Intent(ChatScreen.this, BackgroundManager.class);
       startService(intent);

       super.onStart();
    }
    @Override
    protected void onStop() {
        //TODO Auto-generated method stub
        unregisterReceiver(myReceiver);
        super.onStop();
    }

    private class MyReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            // TODO Auto-generated method stub
            //int datapassed = arg1.getIntExtra("DATAPASSED", 0);
            Toast.makeText(ChatScreen.this,
                    "Triggered by Service!\n"
                   + "Data passed: ",
           Toast.LENGTH_LONG).show();
            arrayOfUsers.clear();
            try {
                arrayOfUsers.addAll(mydb.getAllMessages(chatid));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            adapter.notifyDataSetChanged();


            }

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
//            if (convertView == null) {
           // Log.d(".....yes.....",user.toString()+"iiiiiiiiiiiiiiiiiiiiiiiiii//////////////////");


                try {
                    if(user.getInt("sender")==sharedPreferences.getInt(UserId,0)) {
                       // Log.d(".....yes.....",chatid+"iiiiiiiiiiiiiiiiiiiiiiiiii//////////////////");
                        convertView = LayoutInflater.from(getContext()).inflate(R.layout.part, parent, false);


                    }else{
                        convertView = LayoutInflater.from(getContext()).inflate(R.layout.part2, parent, false);
                       TextView sender=(TextView) convertView.findViewById(R.id.sender);
                        if(is_group==1 && user.getInt("show_name")==1){
                            sender.setText(user.getString("sender_name"));

                        }else{
                            sender.setVisibility(View.GONE);
                        }

                    }
                  TextView  message_time=(TextView) convertView.findViewById(R.id.message_time);

                    if(user.getInt("show")==1){
                        message_time.setText(user.getString("time_of_msg"));
                    }else{
                        message_time.setVisibility(View.GONE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }






//            }
            // Lookup view for data population
            TextView Message = (TextView) convertView.findViewById(R.id.message);
            //TextView Time = (TextView) convertView.findViewById(R.id.time);
            // Populate the data into the template view using the data object
            try {
                Message.setText(user.getString("message"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return convertView;
        }
        @Override
        public boolean isEnabled(int position) {
            return false;
        }
    }



    class Sync_messages extends AsyncTask<String, Void, String> {

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
                android.util.Log.d("Status: ","connected to socket.................");

                Issent=socket.send(params[0]);
                if(Issent==1){
                    android.util.Log.d("Status: ","Data sent successfully.................");

                    String out= socket.recv();
                    android.util.Log.d("Server says: ",out+".................");

                    JSONArray jsonArray= null;
                    try {
                        JSONObject jsonObjectOut=new JSONObject(out);
                        String time=jsonObjectOut.getString("up_time");
                        jsonArray = jsonObjectOut.getJSONArray("messages");
                        for(Integer i=0;i<jsonArray.length();i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            jsonObject.put("chatid",chatid);
                            Integer primary_key= jsonObject.getInt("primary_key");
                            if(mydb.is_newMessage(primary_key)){
                                mydb.insert_message(jsonObject.toString());
                                mydb.setLastUpdatedTime(chatid,time);
                            }

                        }

                        } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }else {
                    android.util.Log.d("Status: ","Data not sent.................");
                    output=NOT_CONNECTED;
                }


            }else {
                android.util.Log.i("Status: ","NOT connected to socket.................");
                output=NOT_CONNECTED;
            }



            // mydb.insert(out);
            return output;
        }

        protected void onPostExecute(String output) {
            if(output.equals(NOT_CONNECTED)){
                Toast.makeText(ChatScreen.this, "No internet connection", Toast.LENGTH_SHORT).show();
            }
            arrayOfUsers.clear();
            try {
                arrayOfUsers.addAll(mydb.getAllMessages(chatid));
            } catch (JSONException e) {
                e.printStackTrace();
            }
           adapter.notifyDataSetChanged();
        }
    }


    class sendMessage extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... params) {
            Integer Isconnected;
            Integer Issent;

            SocketManager socket = SocketManager.getInstance();
            if (!socket.isConnected()) {
                Isconnected=socket.connectSocket("10.196.31.69", 1997);
            }else{
                Isconnected=1;
            }
            if (Isconnected==1){
                android.util.Log.d("Status: ","connected to socket.................");

                Issent=socket.send(params[0]);
                if(Issent==1){
                    android.util.Log.d("Status: ","Data sent successfully.................");

                    String out= socket.recv();
                    android.util.Log.d("Server says: ",out+".................");
                    if(out!=null){
                        try {
                            JSONObject jsonObject=new JSONObject(out);
                            if(jsonObject.getString("status").equals("True")){
                                JSONObject jsonObjectOut=new JSONObject(params[0]);
                                jsonObjectOut.put("time_of_msg",jsonObject.getString("time_of_msg"));
                                jsonObjectOut.put("sender_name",sharedPreferences.getString(NameOfUser,""));
                                mydb.insert_message(jsonObjectOut.toString());
                                
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }


                }else {
                    android.util.Log.d("Status: ","Data not sent.................");
                }


            }else {
                android.util.Log.i("Status: ","NOT connected to socket.................");
            }




            return "";
        }

        protected void onPostExecute(String output) {

        }
    }


    class Addperson extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... params) {
            Integer Isconnected;
            Integer Issent;

            SocketManager socket = SocketManager.getInstance();
            if (!socket.isConnected()) {
                Isconnected=socket.connectSocket("10.196.31.69", 1997);
            }else{
                Isconnected=1;
            }
            if (Isconnected==1){
                android.util.Log.d("Status: ","connected to socket.................");

                Issent=socket.send(params[0]);
                if(Issent==1){
                    android.util.Log.d("Status: ","Data sent successfully.................");

                    String out= socket.recv();
                    android.util.Log.d("Server says: ",out+".................");
                    if(out!=null){
                        try {
                            JSONObject jsonObject=new JSONObject(out);
                            if(jsonObject.getString("status").equals("True")){
                                JSONObject jsonObjectOut=new JSONObject(params[0]);
                                jsonObjectOut.put("time_of_msg",jsonObject.getString("time_of_msg"));
                                jsonObjectOut.put("sender_name",sharedPreferences.getString(NameOfUser,""));
                                mydb.insert_message(jsonObjectOut.toString());

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }


                }else {
                    android.util.Log.d("Status: ","Data not sent.................");
                }


            }else {
                android.util.Log.i("Status: ","NOT connected to socket.................");
            }




            return "";
        }

        protected void onPostExecute(String output) {

        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chatscreen_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.action_refresh:
                //oast.makeText(this, "Refresh selected", Toast.LENGTH_SHORT).show();
                JSONObject jsonObject=new JSONObject();

                try {
                    jsonObject.put("key","fetch_a_chat");
                    jsonObject.put("id",sharedPreferences.getInt(UserId,0));
                    jsonObject.put("chatid",chatid);
                    jsonObject.put("threshhold",mydb.getLastUpdatedTime(chatid));
                    new Sync_messages().execute(jsonObject.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            // action with ID action_settings was selected

            case R.id.action_logout:
                Toast.makeText(this, "Logout selected", Toast.LENGTH_SHORT)
                        .show();
                break;
            case R.id.action_changeIp:
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                final EditText edittext = new EditText(this);
                edittext.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                alert.setMessage("Enter new IP");
                alert.setTitle("Old IP: "+sharedPreferences.getString(IP,""));

                alert.setView(edittext);

                alert.setPositiveButton("Change IP", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //What ever you want to do with the value

                        String YouEditTextValue = edittext.getText().toString();
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("wish",YouEditTextValue);
                        editor.apply();
                        android.util.Log.d("------------------",YouEditTextValue);
                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // what ever you want to do with No option.
                    }
                });

                alert.show();

                break;

        }

        return true;
    }

}



