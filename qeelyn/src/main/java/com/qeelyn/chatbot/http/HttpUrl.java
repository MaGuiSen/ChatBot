package com.qeelyn.chatbot.http;

import com.qeelyn.chatbot.api.BotClient;

public class HttpUrl {
	public static String getIID() {
		return BotClient.baseUrl + "/chatbot/mock/gain-iid";//获取iid
	}
	public static String getToken() {
		return BotClient.baseUrl + "/chatbot/mock/gain-token";//获取Token
	}
	public static String getChatBotUrl() {
		return BotClient.baseUrl + "/chatbot/mock/index";//获取机器人服务器路径
	}
}
