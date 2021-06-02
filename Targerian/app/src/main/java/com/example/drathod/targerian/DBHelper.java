package com.example.drathod.targerian;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DBHelper extends SQLiteOpenHelper {
    // Logcat tag
    private static final String LOG = "DatabaseHelper";

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "targerian";

    // Table Names
    private static final String TABLE_USERS = "ChatInitiatedUsers";

    private static final String CHAT_DETAILS = "chat_details";
    private static final String FRIENDS = "friends";
    private static final String MESSAGES = "messages";

    // Common column names
    private static final String KEY_ID = "id";

    private static final String CHAT_ID = "chat_id";
    private static final String NAME = "name";
    private static final String IS_GROUP = "is_group";
    private static final String LATEST_MSG = "latest_msg";
    private static final String SENDER = "sender";
    private static final String SENDER_NAME= "sender_name";
    private static final String TIME_OF_LATEST_MSG = "time_of_latest_msg";



    private static final String MESSAGE = "message";
    private static final String TIME_OF_MSG = "time_of_msg";


    //Users Table - column names
    //private static final String CHAT_ID = "user_id";
    private static final String CHAT_NAME = "username";

    private static final String CREATE_TABLE_CHAT_DETAILS = "CREATE TABLE chat_details " +
            "(chat_id int(11) NOT NULL , " +
            "name varchar(100) DEFAULT NULL, " +
            "is_group tinyint(4) NOT NULL, " +
            "latest_msg varchar(1000) DEFAULT NULL, " +
            "sender int(11) DEFAULT NULL, " +
            "sender_name varchar(100) NOT NULL, "+
            "time_of_latest_msg timestamp NULL DEFAULT NULL, " +
            "last_updated_time timestamp DEFAULT '1970-01-01 00:00:01' "+
            ")";

    private static final String CREATE_TABLE_FRIENDS = "CREATE TABLE friends " +
            "( id int(11) NOT NULL, " +
            "firstname varchar(200) NOT NULL, " +
            "lastname varchar(200) NOT NULL, " +
            "age int(11) NOT NULL, " +
            "username varchar(200) NOT NULL, " +
            "PRIMARY KEY (id), " +
            "UNIQUE KEY USERNAME (username) " +
            ") ";

    private static final String CREATE_TABLE_MESSAGES = "CREATE TABLE messages " +
            "(primary_key int(11) NULL , " +
            "chat_id int(11) NOT NULL, " +
            "sender int(11) NOT NULL, " +
            "sender_name varchar(100) NOT NULL, "+
            "message varchar(1000) NOT NULL, " +
            "time_of_msg timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP " +
            ")";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(CREATE_TABLE_CHAT_DETAILS);
        //db.execSQL(CREATE_TABLE_FRIENDS);
        db.execSQL(CREATE_TABLE_MESSAGES);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS " + CHAT_DETAILS);
        db.execSQL("DROP TABLE IF EXISTS " + FRIENDS);
        db.execSQL("DROP TABLE IF EXISTS " + MESSAGES);
        onCreate(db);
    }

    /*
 * Creating
 */
    public void onLogout(){
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + CHAT_DETAILS);
        db.execSQL("DROP TABLE IF EXISTS " + FRIENDS);
        db.execSQL("DROP TABLE IF EXISTS " + MESSAGES);
        onCreate(db);
    }




    public String getLastUpdatedTime(int chatid){

        String selectQuery = "SELECT  last_updated_time FROM " + CHAT_DETAILS + " WHERE " + CHAT_ID + " = " + chatid ;
        String output="";
        Log.d(LOG, selectQuery);


        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                output=c.getString(c.getColumnIndex("last_updated_time"));
            } while (c.moveToNext());
        }
        c.close();
        return output;
    }

    public void setLastUpdatedTime(int chatid,String time){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("last_updated_time",time);
        db.update(CHAT_DETAILS, values, CHAT_ID + " = ?",
                new String[] { Integer.toString( chatid)});

    }


    public Boolean is_newMessage(long todo_id) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + MESSAGES + " WHERE "
                + "primary_key" + " = " + todo_id;

        Log.e(LOG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);
        Boolean td=true;
        if (c != null) {
            if (c.moveToFirst())
                td=false;
        }
        if(c!=null) {
            c.close();
        }
        return td;

    }



    public long insert_message(String  message) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        try {
            JSONObject jsonObject=new JSONObject(message);
            //if(jsonObject.getString("key").equals("chat_details")){
            values.put(CHAT_ID,jsonObject.getInt("chatid"));
            values.put(MESSAGE,jsonObject.getString(MESSAGE));
            values.put(SENDER,jsonObject.getString(SENDER));
            values.put(SENDER_NAME,jsonObject.getString(SENDER_NAME));
            values.put(TIME_OF_MSG,jsonObject.getString(TIME_OF_MSG));
            // insert row
            long todo_id = db.insert(MESSAGES, null, values);
            Log.d("----------", "added succesfullly"+jsonObject.getString(MESSAGE));
            return todo_id;
            // }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return -1;

    }

    public ArrayList<JSONObject> getAllMessages(Integer chatid) throws JSONException {
        ArrayList<JSONObject> arrayList;
        JSONArray jsonArray = new JSONArray(); //ArrayList<ChatUser>();
        String selectQuery = "SELECT  * FROM " + MESSAGES + " WHERE " + CHAT_ID + " = " + chatid + " ORDER BY " + TIME_OF_MSG + " ASC";

        Log.e(LOG, selectQuery);


        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        String tempString="1970-01-01 00:00:01";
        String previousName="";

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                JSONObject jsonObject=new JSONObject();
                //Log.d("uyiii",c.getColumnIndex(NAME));
                jsonObject.put(MESSAGE,c.getString(c.getColumnIndex(MESSAGE))) ;
                jsonObject.put(SENDER_NAME,c.getString(c.getColumnIndex(SENDER_NAME))) ;
                jsonObject.put(SENDER,c.getString(c.getColumnIndex(SENDER))) ;

                String dateString = c.getString(c.getColumnIndex(  TIME_OF_MSG));
                DateFormatter dateFormatter = new DateFormatter(dateString);
                DateFormatter today_formatter = new DateFormatter();
                String date = dateFormatter.format("yyyy-MM-dd");
                String month = dateFormatter.format("MMM");
                String day = dateFormatter.format("dd");
                String hour = dateFormatter.format("HH");
                String minute = dateFormatter.format("mm");
                String today = today_formatter.format("yyyy-MM-dd");
                if (date.equals(today)) {
                    jsonObject.put(TIME_OF_MSG,hour + ":" + minute) ;

                } else {
                    jsonObject.put(TIME_OF_MSG,day + " " + month + " AT " + hour + ":" + minute)  ;
                }

               if( dateFormatter.diff_greaterthan_10min(tempString,dateString)){
                   jsonObject.put("show",1);
                   jsonObject.put("show_name",1);
               }else {
                   jsonObject.put("show",0);
                   if(!previousName.equals(jsonObject.getString(SENDER_NAME))){
                       jsonObject.put("show_name",1);
                   }else{
                       jsonObject.put("show_name",0);
                   }

               }

                tempString=dateString;
                previousName=jsonObject.getString(SENDER_NAME);

                // adding to todo list
                jsonArray.put(jsonObject);
            } while (c.moveToNext());
        }
        c.close();

        Log.d("Data:",jsonArray.toString());

        arrayList=new ArrayList<>();
        for (int i=0;i<jsonArray.length();i++){
            arrayList.add(jsonArray.getJSONObject(i));
        }


        return arrayList;
    }



    public long insert_chat_details(String  message) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        try {
            JSONObject jsonObject=new JSONObject(message);
            //if(jsonObject.getString("key").equals("chat_details")){
                values.put(CHAT_ID,jsonObject.getInt("chatid"));
                values.put(NAME,jsonObject.getString("name"));
                values.put(IS_GROUP,jsonObject.getString("is_group"));
                values.put(LATEST_MSG,jsonObject.getString("latest_msg"));
                values.put(SENDER,jsonObject.getString("sender"));
                values.put(SENDER_NAME,jsonObject.getString(SENDER_NAME));
                values.put(TIME_OF_LATEST_MSG,jsonObject.getString(TIME_OF_LATEST_MSG));
                // insert row
                long todo_id = db.insert(CHAT_DETAILS, null, values);
                Log.d("----------", "message inserted succesfullly"+jsonObject.getString("name"));
                return todo_id;
           // }
        } catch (JSONException e) {
            e.printStackTrace();
        }

       return -1;

    }



    public long update_chat_details(String message){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        try {
            JSONObject jsonObject=new JSONObject(message);
            //if(jsonObject.getString("key").equals("chat_details")){
            values.put(NAME,jsonObject.getString("name"));
            values.put(LATEST_MSG,jsonObject.getString("latest_msg"));
            values.put(SENDER,jsonObject.getString("sender"));
            values.put(TIME_OF_LATEST_MSG,jsonObject.getString(TIME_OF_LATEST_MSG));

        // updating row
        return db.update(CHAT_DETAILS, values, CHAT_ID + " = ?",
                new String[] { jsonObject.getString("chatid") });

           // }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return  -1;
    }

    /*
 * get single todo
 */



    public Boolean is_newChat(long todo_id) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + CHAT_DETAILS + " WHERE "
                + CHAT_ID + " = " + todo_id;

        Log.e(LOG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);
        Boolean td=true;
        if (c != null) {
            if (c.moveToFirst())
                td=false;
        }
        if(c!=null) {
            c.close();
        }
        return td;

    }

    /*
 * getting all todos
 * */
    public ArrayList<JSONObject> getAllUsers() throws JSONException {
        ArrayList<JSONObject> arrayList;
        JSONArray jsonArray = new JSONArray(); //ArrayList<ChatUser>();
        String selectQuery = "SELECT  * FROM " + CHAT_DETAILS + " ORDER BY " + TIME_OF_LATEST_MSG + " DESC";

        Log.e(LOG, selectQuery);


        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                JSONObject jsonObject=new JSONObject();
                //Log.d("uyiii",c.getColumnIndex(NAME));
               jsonObject.put("chatid",c.getInt(c.getColumnIndex(CHAT_ID))) ;
                jsonObject.put(NAME,c.getString(c.getColumnIndex(NAME))) ;
                jsonObject.put(IS_GROUP,c.getInt(c.getColumnIndex(IS_GROUP))) ;
                jsonObject.put(LATEST_MSG,c.getString(c.getColumnIndex(LATEST_MSG))) ;
                jsonObject.put(SENDER,c.getString(c.getColumnIndex(SENDER))) ;
                jsonObject.put(SENDER_NAME,c.getString(c.getColumnIndex(SENDER_NAME))) ;

                String dateString = c.getString(c.getColumnIndex(  TIME_OF_LATEST_MSG));
                DateFormatter dateFormatter = new DateFormatter(dateString);
                DateFormatter today_formatter = new DateFormatter();
                String date = dateFormatter.format("yyyy-MM-dd");
                String month = dateFormatter.format("MMM");
                String day = dateFormatter.format("dd");
                String hour = dateFormatter.format("HH");
                String minute = dateFormatter.format("mm");
                String today = today_formatter.format("yyyy-MM-dd");
                if (date.equals(today)) {
                    jsonObject.put(TIME_OF_LATEST_MSG,hour + ":" + minute) ;

                } else {
                    jsonObject.put(TIME_OF_LATEST_MSG,day + " " + month) ;
                }

                // adding to todo list
                jsonArray.put(jsonObject);
            } while (c.moveToNext());
        }
            c.close();
            db.close();
            arrayList=new ArrayList<>();
            for (int i=0;i<jsonArray.length();i++){
                arrayList.add(jsonArray.getJSONObject(i));
            }

        return arrayList;
    }

}
