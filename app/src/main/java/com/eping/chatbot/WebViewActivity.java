package com.eping.chatbot;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import lib.widget.BotWebView;

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
        // 注入一个js对象
        web_view.addJavascriptInterface(new JavaScriptInterface(),"JsNativeObject");
        //注入js代码，用于和前面的js对象同步调用，同步RN调用方式
        web_view.loadUrl("javascript:window.postMessage = function(data){JsNativeObject.onReceiveMsg(data)}");

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

    /** 需要注入的js对象*/
    class JavaScriptInterface {
        public JavaScriptInterface() {
        }
        //js发送消息到onReceiveMsg方法
        @JavascriptInterface
        public void onReceiveMsg(String jsonData) {
            try {
                JSONObject jsonObject = new JSONObject(jsonData);
                String type = jsonObject.optString("type", "");
                if("url_back".equals(type)){
                    onBackPressed();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
