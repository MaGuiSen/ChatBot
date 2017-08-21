package com.qeelyn.chatbot.http;


public interface ResponseListener<T> {
	void onSuccess(T object);
	void onFailure(int errCode, String errMsg);
}
