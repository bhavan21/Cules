//package com.example.drathod.targerian;
//
///**
// * Created by drathod on 8/1/17.
// */
//
//import android.app.AlertDialog;
//import android.content.Context;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.TextView;
//
//import com.android.volley.RequestQueue;
//import com.android.volley.Response;
//import com.android.volley.toolbox.Volley;
//
//import org.json.JSONException;
//import org.json.JSONObject;
//
//public class LoginActivity extends AppCompatActivity {
//
//    SharedPreferences sharedPreferences;
//    public static final String MyPref = "Mypref";
//    public static final String Login_status= "active";
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        sharedPreferences = getSharedPreferences(MyPref, Context.MODE_PRIVATE);
//
//
//        setContentView(R.layout.activity_login);
//
//
//        final EditText etUsername = (EditText) findViewById(R.id.etUsername);
//        final EditText etPassword = (EditText) findViewById(R.id.etPassword);
//        final TextView tvRegisterLink = (TextView) findViewById(R.id.tvRegisterLink);
//        final Button bLogin = (Button) findViewById(R.id.bSignIn);
//
//        tvRegisterLink.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
//                LoginActivity.this.startActivity(registerIntent);
//            }
//        });
//
//
//        bLogin.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                final String username = etUsername.getText().toString();
//                final String password = etPassword.getText().toString();
//
//                // Response received from the server
//                Response.Listener<String> responseListener = new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//                        try {
//                            JSONObject jsonResponse = new JSONObject(response);
//                            boolean success = jsonResponse.getBoolean("success");
//                            //Log.e("shared preferences ----", sharedPreferences.getBoolean(Login_status, true) + "--------------------------");
//                            if (success ) {
//
//                                SharedPreferences.Editor editor = sharedPreferences.edit();
//                                editor.putBoolean(Login_status, true);
//                                editor.commit();
//
//
//                                String name = jsonResponse.getString("name");
//                                int age = jsonResponse.getInt("age");
//
//                                Intent intent = new Intent(LoginActivity.this, chatlist.class);
//                                intent.putExtra("name", name);
//                                intent.putExtra("age", age);
//                                intent.putExtra("username", username);
//                                LoginActivity.this.startActivity(intent);
//                                LoginActivity.this.finish();
//                               //  finish();
//                                // MainActivity.finish();
//
//                            } else {
//                                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
//                                builder.setMessage("Login Failed")
//                                        .setNegativeButton("Retry", null)
//                                        .create()
//                                        .show();
//                            }
//
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                };
//
//                LoginRequest loginRequest = new LoginRequest(username, password, responseListener);
//                RequestQueue queue = Volley.newRequestQueue(LoginActivity.this);
//                queue.add(loginRequest);
//            }
//        });
//    }
//}

package com.example.drathod.targerian;

/**
 * Created by drathod on 8/1/17.
 */

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    public static final String MyPREF = "Mypref";
    public static final String Login_status= "active";
    public static final String UserId= "userid";
    public static final String NameOfUser= "name_of_user";
    public static final String IP= "ip";
    public static final String NOT_CONNECTED="notConnected";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getSharedPreferences(MyPREF, Context.MODE_PRIVATE);

        if(sharedPreferences.getBoolean(Login_status,false)){
            finish();
            Intent intent=new Intent(LoginActivity.this,chatlist.class);
            LoginActivity.this.startActivity(intent);
        }

        setContentView(R.layout.activity_login);

//
//        Intent intent = new Intent();
//        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
//        intent.setAction("com.example.Broadcast");
//        intent.putExtra("HighScore", 1000);
//        sendBroadcast(intent);

        final EditText etUsername = (EditText) findViewById(R.id.etUsername);
        final EditText etPassword = (EditText) findViewById(R.id.etPassword);
        final TextView tvRegisterLink = (TextView) findViewById(R.id.tvRegisterLink);
        final Button bLogin = (Button) findViewById(R.id.bSignIn);



        tvRegisterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                LoginActivity.this.startActivity(registerIntent);
            }
        });




        bLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = etUsername.getText().toString();
                final String password = etPassword.getText().toString();
                JSONObject jsonObject=new JSONObject();
                try {
                    jsonObject.put("key","login");
                    jsonObject.put("username",username);
                    jsonObject.put("password",password);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Log.d("ddfsd",jsonObject.toString());
                new Network().execute(jsonObject.toString());

            }
        });
    }


    class Network extends AsyncTask<String,Void,String> {

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
                        JSONObject jsonObject= null;
                        try {
                            jsonObject = new JSONObject(out);
                            Boolean status=jsonObject.getBoolean("status");
                            Integer id=jsonObject.getInt("id");
                            SharedPreferences.Editor editor=sharedPreferences.edit();
                            if(status){
                                editor.putBoolean(Login_status,true);
                                editor.putInt(UserId,id);
                                editor.putString(NameOfUser,jsonObject.getString("name"));
                                editor.apply();
                                output="1";
                            }else {
                                output="0";

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                        return  output;
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

            if(output.equals(NOT_CONNECTED)){
                Toast.makeText(LoginActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
            }
            if(output.equals("1")){
                Intent intent=new Intent(LoginActivity.this,chatlist.class);
                LoginActivity.this.startActivity(intent);
                LoginActivity.this.finish();
                Log.d("Credidentials correct: ",output+"..............");

            }else if(output.equals("0")) {
                android.support.v7.app.AlertDialog.Builder alertDialogBuilder = new android.support.v7.app.AlertDialog.Builder(LoginActivity.this);
                alertDialogBuilder.setMessage("Username or Password is incorrect");
                alertDialogBuilder.setCancelable(true);
                alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                });
                android.support.v7.app.AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();

            }

        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.login_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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
                        editor.commit();
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
            default:
                break;
        }

        return true;
    }



}