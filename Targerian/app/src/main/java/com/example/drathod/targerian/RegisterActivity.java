package com.example.drathod.targerian;

/**
 * Created by drathod on 8/1/17.
 */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;


public class RegisterActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    public static final String MyPref = "Mypref";
    public static final String IP = "ip";
    public static final String NOT_CONNECTED="notConnected";
    Intent loginIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences(MyPref, Context.MODE_PRIVATE);

        setContentView(R.layout.activity_register);

        final EditText etFirstame = (EditText) findViewById(R.id.etFirstame);
        final EditText etLastname = (EditText) findViewById(R.id.etLastname);
        final EditText etAge = (EditText) findViewById(R.id.etAge);
        final EditText etUsername = (EditText) findViewById(R.id.etUsername);
        final EditText etPassword = (EditText) findViewById(R.id.etPassword);
        final Button bRegister = (Button) findViewById(R.id.bRegister);

        loginIntent=new Intent(RegisterActivity.this,LoginActivity.class);

        bRegister.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String firstname=etFirstame.getText().toString();
                String lastname=etLastname.getText().toString();
                String age=etAge.getText().toString();
                String username=etUsername.getText().toString();
                String password=etPassword.getText().toString();

                JSONObject jsonObject=new JSONObject();
                try {
                    jsonObject.put("key","register");
                    jsonObject.put("firstname",firstname);
                    jsonObject.put("lastname",lastname);
                    jsonObject.put("age",age);
                    jsonObject.put("username",username);
                    jsonObject.put("password",password);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d("json",jsonObject.toString()+"............");
                new Network().execute(jsonObject.toString());

            }
        });
    }

    class Network extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... params) {
            String output="0";
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
                       output=out;
                    }


                }else {
                    android.util.Log.d("Status: ","Data not sent.................");
                    output=NOT_CONNECTED;
                }


            }else {
                android.util.Log.i("Status: ","NOT connected to socket.................");
                output=NOT_CONNECTED;
            }




            return output;
        }



        protected void onPostExecute(String output) {
            // TODO: check this.exception
            // TODO: do something with the feed
            Log.d("Server says: ", output + "..............");
            if(output.equals(NOT_CONNECTED)){
                Toast.makeText(RegisterActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
            }
            if(output.equals("True")) {
                RegisterActivity.this.startActivity(loginIntent);
            }else if(output.equals("False")){
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(RegisterActivity.this);
                alertDialogBuilder.setMessage("Username already exists.Try another username");
                alertDialogBuilder.setCancelable(true);
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();

            }
        }
    }

}

