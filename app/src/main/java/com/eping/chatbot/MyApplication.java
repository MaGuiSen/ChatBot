package com.eping.chatbot;
import android.app.Application;

import lib.api.BotClient;


public class MyApplication extends Application {
	private String clientSecret = "123456";//密钥id
	private String clientId = "eping";//客户端类型
    private String grantType = "client_credentials";//验证类型
	private static MyApplication mMyApplication;

	public static MyApplication getInstance(){
		return mMyApplication;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mMyApplication = this;
		BotClient.init(clientId, grantType, clientSecret);
	}
}
