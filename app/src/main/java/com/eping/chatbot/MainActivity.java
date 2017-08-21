package com.eping.chatbot;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void clickToUse(View view){
        Intent intent = new Intent(this, ChatBotActivity.class);
        startActivity(intent);
    }

    public void clickGoSeeInfo(View view){
        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra(WebViewActivity.URL, "http://117.29.166.222:8099/file/chatbot/explain/explain.html");
        startActivity(intent);
    }
}
