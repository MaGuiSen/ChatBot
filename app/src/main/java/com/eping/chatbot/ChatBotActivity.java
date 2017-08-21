package com.eping.chatbot;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import lib.third.speech.IflyTTSModule;
import lib.widget.BotWebView;
import lib.widget.ChatBotInputView;
import lib.widget.SpeechSettingDialog;

public class ChatBotActivity extends AppCompatActivity {
    private BotWebView web_view;
    private ChatBotInputView lay_chat_input;

    private String channelId = "";//渠道id
    private String appId = "f65d3b77-7ab8-40fd-af56-2cd24b10ed94";//chatbot的id
    private String secretId = "123456";//密钥id
    private String userId = "111";//用户id：对应不同的系统的用户唯一标识
    private String clientId = "eping";//客户端类型
    private String grantType = "client_credentials";//验证类型

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_bot);
        initView();
    }

    private void initView() {
        web_view = (BotWebView) findViewById(R.id.web_view);
        web_view.setGrantType(grantType)
                .setClientId(clientId)
                .setSecretId(secretId)
                .setUserId(userId)
                .setUserInfo("小黄", "female", "cn")
                .openChatbotUrl(appId, channelId);
        web_view.setWebViewClient(new WebViewClient(){
            //内被只开放：onPageStarted和onPageFinished方法
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                //此方法会抛出非chatbot官方的url
                Intent intent = new Intent(getApplication(), WebViewActivity.class);
                intent.putExtra(WebViewActivity.URL, url);
                startActivity(intent);
            }

            public void onPageFinished(WebView view, String url) {
                //加载完成之后才能调用关闭输入框的方法
                web_view.closeWebInput();
            }
        });
        web_view.setBotWebViewListener(new BotWebView.BotWebViewListener(){
            @Override
            public void showInputLay(){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    lay_chat_input.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void error(int code, String errMsg) {
                Toast.makeText(getApplication(), "code:"+code+",msg:"+errMsg, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void receiveSpeechData(String speechData){
                IflyTTSModule.getInstance(getApplication()).startSpeaking(speechData, "", "");
            }
        });
        lay_chat_input = (ChatBotInputView) findViewById(R.id.lay_chat_input);
        lay_chat_input.setListener(new ChatBotInputView.Listener() {
            @Override
            public void sendTextMsg(String msg) {
                web_view.sendText(msg);
            }
        });
    }

    public void refresh(View view){
        web_view.setSecretId(secretId).setUserId(userId).openChatbotUrl(appId, channelId);
    }
}
