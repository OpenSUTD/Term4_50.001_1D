package com.example.zifang.a3dprintermate;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private EditText Name;
    private TextView Info;
    private Button Login;
    private int counter = 5;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Name = (EditText) findViewById(R.id.etPrinter);
        Info = (TextView) findViewById(R.id.Info);
        Login = (Button) findViewById(R.id.btnLogin);
        Info.setText("No. of attemps remaining: 5");
        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validate(Name.getText().toString());
            }
        });

        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.setStatusBarColor(getColor(R.color.mystatusbar));
        }
        else{
            window.setStatusBarColor(getResources().getColor(R.color.mystatusbar));
        }

        ConstraintLayout constraintLayout = findViewById(R.id.layout);
        AnimationDrawable animationDrawable = (AnimationDrawable) constraintLayout.getBackground();
        animationDrawable.setEnterFadeDuration(2000);
        animationDrawable.setExitFadeDuration(4000);
        animationDrawable.start();

    }

    private void validate(String username){
        if(username.equals("Admin")){
            Intent intent = new Intent(this,SecondActivity.class);
            startActivity(intent);
        }else{
            counter--;

            Info.setText("No. of attemps remaining: " + String.valueOf(counter));
            if(counter == 0){
                Login.setEnabled(false);
            }
        }
    }
    @Override
    public void onBackPressed(){
        //do nothing
    }
}
