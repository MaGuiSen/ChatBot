package com.eping.chatbot;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class WebViewActivity extends AppCompatActivity {
    public static String URL = "url";
    WebView web_view;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        String url = getIntent().getStringExtra(URL);
        web_view = (WebView) findViewById(R.id.web_view);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        web_view.getSettings().setJavaScriptEnabled(true);
        //设置Web视图
        web_view.setWebViewClient(new WebViewClient());
        //进度条进度修改
        web_view.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {

                super.onProgressChanged(view, newProgress);
                if (progressBar == null) return;
                //这里将textView换成你的progress来设置进度
                if (newProgress < 100) {
                    progressBar.setVisibility(View.VISIBLE);
                }
                progressBar.setProgress(newProgress);
                progressBar.postInvalidate();
                if (newProgress >= 100) {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
        web_view.loadUrl(url);
    }

    @Override
    public void onBackPressed() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
            if(web_view.canGoBack()){
                web_view.goBack();
            }else{
                WebViewActivity.super.onBackPressed();
            }
            }
        });
    }

    public void back(View view){
        onBackPressed();
    }
}
