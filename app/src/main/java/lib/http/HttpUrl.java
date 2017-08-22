package lib.http;

import lib.api.BotClient;

public class HttpUrl {
	public static String getIID() {
		//获取iid
		return BotClient.baseUrl + "/chatbot/mock/gain-iid";
	}
	public static String getToken() {
		//获取iid
		return BotClient.baseUrl + "/chatbot/mock/gain-token";//获取Token
	}
	public static String getChatBotUrl() {
		//获取iid
		return BotClient.baseUrl + "/chatbot/mock/index";//获取Token
	}
}
