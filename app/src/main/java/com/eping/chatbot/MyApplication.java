package com.eping.chatbot;
import android.app.Application;


public class MyApplication extends Application {

	private static MyApplication mMyApplication;

	public static MyApplication getInstance(){
		return mMyApplication;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mMyApplication = this;
	}
}
