package com.qeelyn.chatbot.http;

import android.os.Handler;
import android.os.Message;

public class HttpHandler extends Handler{
	private ResponseListener responseListener;
	public HttpHandler(ResponseListener responseListener){
		this.responseListener = responseListener;
	}
	@Override
	public void handleMessage(Message msg) {
		switch (msg.what) {
			case HttpExecute.REQ_SUCCESS:
				if (responseListener != null){
					responseListener.onSuccess(msg.obj);
				}
				break;
			case HttpExecute.REQ_FAILURE:
				if (responseListener != null){
					responseListener.onFailure(-1, (String) msg.obj);
				}
				break;
			default:
				break;
		}
	}
}
