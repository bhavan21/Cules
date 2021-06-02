package com.example.drathod.targerian;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    public static final String MyPREF = "Mypref";
    public static final String Login_status= "active";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        sharedPreferences = getSharedPreferences(MyPREF, Context.MODE_PRIVATE);

        Boolean Is_Login=sharedPreferences.getBoolean(Login_status,false);
        if(Is_Login){
            Intent intent = new Intent(MainActivity.this, chatlist.class);
            MainActivity.this.startActivity(intent);
            MainActivity.this.finish();
        }

        setContentView(R.layout.activity_main);

        final Button login = (Button)findViewById(R.id.login);
        final Button signup = (Button)findViewById(R.id.signup);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginscreen = new Intent(MainActivity.this, LoginActivity.class);
                MainActivity.this.startActivity(loginscreen);
                MainActivity.this.finish();
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(MainActivity.this, RegisterActivity.class);
                MainActivity.this.startActivity(registerIntent);
                MainActivity.this.finish();
            }
        });

    }

}
