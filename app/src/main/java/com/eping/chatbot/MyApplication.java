package com.eping.chatbot;
import android.app.Application;

import com.qeelyn.chatbot.api.BotClient;


public class MyApplication extends Application {
	private String clientSecret = "123456";//密钥id
	private String clientId = "wdxx123456!";//客户端类型
    private String grantType = "client_credentials";//验证类型
    private String baseUrl = "http://test.qeelyn.hk";//chatbot基础地址
	private static MyApplication mMyApplication;

	public static MyApplication getInstance(){
		return mMyApplication;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mMyApplication = this;
		BotClient.init(clientId, grantType, clientSecret, baseUrl);
	}
}
