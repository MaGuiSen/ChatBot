package lib.api;

/**
 * Bot客户端访问api
 */
public class BotClient {
    public static String clientId = "";//客户端类型
    public static String grantType = "";//验证类型
    public static String secretId = "";//密钥id

    public static void init(String clientId, String grantType, String secretId){
        BotClient.clientId = clientId;
        BotClient.grantType = grantType;
        BotClient.secretId = secretId;
    }
}
