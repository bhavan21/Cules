package com.example.drathod.targerian;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.example.drathod.targerian.searchActivity.NOT_CONNECTED;

/**
 * Created by bhavan on 25/4/17.
 */



public class searchActivity extends Activity {
    DBHelper mydb;
    UsersAdapter adapter;
    SharedPreferences sharedPreferences;
    public static final String MyPref = "Mypref";
    public static final String UserId = "userid";
    public static final String NameOfUser = "name_of_user";
    public static final String Login_status = "active";
    public static final String LAST_UPDATED_TIME_CHATLIST = "counter_for_chatlist";
    public static final String IP = "ip";
    public static final String NOT_CONNECTED = "notConnected";
    ArrayList<JSONObject> arrayOfPersons;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actionbarlayout);
        sharedPreferences = getSharedPreferences(MyPref, Context.MODE_PRIVATE);
        mydb = new DBHelper(this);
        Log.d("yo","Entered search Activity...................");
        Toast.makeText(searchActivity.this,"Entered search Activity", Toast.LENGTH_SHORT).show();
        EditText searchBox = (EditText) findViewById(R.id.action_bar_text);
        searchBox.onHoverChanged(true);
        searchBox.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {

                // you can call or do what you want with your EditText here

                Toast.makeText(searchActivity.this,s.toString(), Toast.LENGTH_SHORT).show();
                JSONObject jsonObject=new JSONObject();
                try {
                    jsonObject.put("key","search");
                    jsonObject.put("id",sharedPreferences.getInt(UserId,1));
                    jsonObject.put("search_word",s.toString());
                    new getFriendlist().execute(jsonObject.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
        arrayOfPersons=new ArrayList<>();

        // Create the adapter to convert the array to views
        adapter = new UsersAdapter(this, arrayOfPersons);
        // Attach the adapter to a ListView
        final ListView listView = (ListView) findViewById(R.id.friendlist);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(searchActivity.this, ChatScreen.class);
                Intent intent1=new Intent(searchActivity.this,friend_request.class);
                try {
                    JSONObject jsonObject=arrayOfPersons.get(position);
                    if(jsonObject.getInt("status")==4){
                        intent.putExtra("chatid", jsonObject.getInt("chatid"));//loook at  this once
                        intent.putExtra("is_group",jsonObject.getInt("is_group"));
                        if(jsonObject.getInt("is_group")==1){
                            intent.putExtra("title",jsonObject.getString("name"));
                        }else{
                            intent.putExtra("title",jsonObject.getString("firstname")+" "+jsonObject.getString("lastname"));
                        }
                        searchActivity.this.startActivity(intent);
                        searchActivity.this.finish();
                    }else{
                        intent1.putExtra("status",jsonObject.getInt("status"));
                        intent1.putExtra("name",jsonObject.getString("firstname")+" "+jsonObject.getString("lastname"));
                        intent1.putExtra("id",jsonObject.getInt("id"));
                        searchActivity.this.startActivity(intent1);
                        searchActivity.this.finish();
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });


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
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.searchuser_template, parent, false);
            }
            // Lookup view for data population
            TextView Name = (TextView) convertView.findViewById(R.id.search_element);
            // Populate the data into the template view using the data object
            Log.d("yhjjjjjjj", user.toString());
            try {
                String name=user.getString("firstname")+" "+user.getString("lastname");
                Name.setText(name);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // Return the completed view to render on screen

            return convertView;
        }
    }

    class getFriendlist extends AsyncTask<String, Void, String> {

        ProgressDialog dialog;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(searchActivity.this);
            //dialog.setMessage("Loading, please wait");
           // dialog.setTitle("Connecting server");
            //dialog.show();
            dialog.setCancelable(false);

        }
        protected String doInBackground(String... params) {
            String output = "";
            Integer Isconnected;
            Integer Issent;

            SocketManager socket = SocketManager.getInstance();
            if (!socket.isConnected()) {
                Isconnected = socket.connectSocket("192.168.1.114", 1997);
            } else {
                Isconnected = 1;
            }
            if (Isconnected == 1) {
                Log.d("Status: ", "connected to socket.................");

                Issent = socket.send(params[0]);
                if (Issent == 1) {
                    Log.d("Status: ", "Data sent successfully.................");

                    String out = socket.recv();

                    if (out != null) {
                        JSONArray jsonArray = null;
                        try {
                            JSONObject jsonObjectOut = new JSONObject(out);
                            jsonArray=jsonObjectOut.getJSONArray("persons_list");
                            arrayOfPersons.clear();
                            for (int i=0;i<jsonArray.length();i++){
                                arrayOfPersons.add(jsonArray.getJSONObject(i));
                            }
                            output="notify";


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }


                } else {
                    Log.d("Status: ", "Data not sent.................");
                    output = NOT_CONNECTED;
                }


            } else {
                Log.i("Status: ", "NOT connected to socket.................");
                output = NOT_CONNECTED;
            }


            // mydb.insert(out);
            return output;
        }

        protected void onPostExecute(String output) {
            //dialog.hide();
            if (output.equals(NOT_CONNECTED)) {
                Toast.makeText(searchActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
            }
           if(output.equals("notify")){
               adapter.notifyDataSetChanged();
           }

        }
    }


}
