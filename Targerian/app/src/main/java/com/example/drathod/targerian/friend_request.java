package com.example.drathod.targerian;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class friend_request extends AppCompatActivity {
    DBHelper mydb;
    SharedPreferences sharedPreferences;
    public static final String MyPref = "Mypref";
    public static final String UserId = "userid";
    public static final String NameOfUser = "name_of_user";
    public static final String Login_status = "active";
    public static final String LAST_UPDATED_TIME_CHATLIST = "counter_for_chatlist";
    public static final String IP = "ip";
    public static final String NOT_CONNECTED = "notConnected";
    TextView heading;
    TextView body;
    Integer status;
    String name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_request);
        Bundle bundle = getIntent().getExtras();
        status=bundle.getInt("status");
        name=bundle.getString("name");
       final Integer other_id=bundle.getInt("id");
        sharedPreferences = getSharedPreferences(MyPref, Context.MODE_PRIVATE);
        heading= (TextView) findViewById(R.id.heading_friend_request);
        body= (TextView) findViewById(R.id.body_friend_request);
        if(status==1){
            heading.setText("You and "+name+" are not connected on Targerian. Send request to chat with him/her");
            body.setText("ADD FRIEND");
            body.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    JSONObject jsonObject=new JSONObject();
                    try {
                        jsonObject.put("key","friend_request");
                        jsonObject.put("id",sharedPreferences.getInt(UserId,0));
                        jsonObject.put("other_id",other_id);
                        if(status==1){
                            new  Friend_request().execute(jsonObject.toString());
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        if(status==2){
            heading.setText("You already sent the request to "+name+".");
            body.setText("FRIEND REQUEST SENT");
        }
        if(status==3){
            heading.setText(name+" already sent you a request.Accept the request to chatwith him/her");
            body.setText("ACCEPT REQUEST");
            body.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    JSONObject jsonObject=new JSONObject();
                    try {
                        jsonObject.put("key","accept_request");
                        jsonObject.put("id",sharedPreferences.getInt(UserId,0));
                        jsonObject.put("other_id",other_id);

                        new  Accept_request().execute(jsonObject.toString());

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    class Friend_request extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... params) {
            String output="";
            Integer Isconnected;
            Integer Issent;

            SocketManager socket = SocketManager.getInstance();
            if (!socket.isConnected()) {
                Isconnected=socket.connectSocket("192.168.1.114", 1997);
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
                       if(out.equals("True")){
                           output="FRIEND REQUEST SENT";
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
                Toast.makeText(friend_request.this, "No internet connection", Toast.LENGTH_SHORT).show();
            }else{
                body.setText(output);
                status=2;
            }

        }
    }

    class Accept_request extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... params) {
            String output="";
            Integer Isconnected;
            Integer Issent;

            SocketManager socket = SocketManager.getInstance();
            if (!socket.isConnected()) {
                Isconnected=socket.connectSocket("192.168.1.114", 1997);
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
                        output=out;

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
                Toast.makeText(friend_request.this, "No internet connection", Toast.LENGTH_SHORT).show();
            }else{
                try {
                    JSONObject jsonObject=new JSONObject(output);
                    if(jsonObject.getString("is_true").equals("True")){
                        Integer chat_id=jsonObject.getInt("chat_id");
                        Intent intent=new Intent(friend_request.this,ChatScreen.class);
                        intent.putExtra("chat_id",chat_id);
                        intent.putExtra("is_group",0);
                        intent.putExtra("title",name);
                        friend_request.this.startActivity(intent);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}
