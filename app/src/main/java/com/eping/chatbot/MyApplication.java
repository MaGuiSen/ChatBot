package com.eping.chatbot;
import android.app.Application;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;


public class MyApplication extends Application {

	private static MyApplication mMyApplication;

	public static MyApplication getInstance(){
		return mMyApplication;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mMyApplication = this;
		SpeechUtility dd = SpeechUtility.createUtility(this, SpeechConstant.APPID + "=59479b0d");
	}
}
