package com.qeelyn.chatbot.api;

/**
 * Bot客户端访问api
 */
public class BotClient {
    public static String clientId = "";//客户端类型
    public static String grantType = "";//验证类型
    public static String clientSecret = "";//密钥id
    public static String baseUrl = "";//基础请求路径

    public static void init(String clientId, String grantType, String clientSecret, String baseUrl){
        BotClient.clientId = clientId;
        BotClient.grantType = grantType;
        BotClient.clientSecret = clientSecret;
        BotClient.baseUrl = baseUrl;
    }
}
