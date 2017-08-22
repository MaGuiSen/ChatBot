package lib.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import lib.api.BotClient;
import lib.http.HttpExecute;
import lib.http.HttpUrl;
import lib.http.ResponseListener;
import lib.util.PreferenceUtils;
import lib.util.SystemUtil;

/**
 * 机器人聊天WevView
 */
public class BotWebView extends WebView{
    private String token = "";//访问权限票据
    private String channelId = "";//渠道id
    private String appId = "";//chatbot的id
    private String clientId = "";//客户端类型
    private String grantType = "";//验证类型
    private String clientSecret = "";//密钥id
    private String deviceId = "";//设备号 uuid
    private String userId = "";//用户id：对应不同的系统的用户唯一标识
    private String indentityId = "";//访问对象id
    private String userName = "";//用户名称
    private String gender = "";//性别
    private String language = "";//语言
    private BotWebViewListener botWebViewListener;
    boolean isJsInit = false;
    boolean isShowWebInput = true;

    public BotWebView(Context context) {
        this(context, null);
    }

    public BotWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        isJsInit = false;
        init();
    }

    public void init(){
        WebSettings settings = getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        setWebChromeClient(new WebChromeClient());
        super.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if(!url.startsWith(BotClient.baseUrl)){
                    view.stopLoading();
                    //不是本站的url就抛出去
                    if(botWebViewListener != null){
                        botWebViewListener.onIntentDetailUrl(url);
                    }
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                //加载完成
                initJs();
            }
        });
        // 注入一个js对象
        addJavascriptInterface(new JavaScriptInterface(),"JsNativeObject");
        //注入js代码，用于和前面的js对象同步调用，同步RN调用方式
        loadUrl("javascript:window.postMessage = function(data){JsNativeObject.onReceiveMsg(data)};");
    }

    private void initJs(){
        if(isJsInit){
            return;
        }
        if (!TextUtils.isEmpty(userName)) {
            loadUrl("javascript:window.postMessage = function(data){JsNativeObject.onReceiveMsg(data)};mockIm.setIid('" + indentityId + "');mockIm.saveUser({from_id:'" + indentityId + "',uname:'" + userName + "',gender:'" + gender + "',language:'" + language + "'},function(){mockIm.init()});");
        } else {
            //初始化js
            loadUrl("javascript:window.postMessage = function(data){JsNativeObject.onReceiveMsg(data)};mockIm.setIid('" + indentityId + "');mockIm.init()");
        }
        isJsInit = true;
        if(isShowWebInput){
            loadUrl("javascript:mockIm.whetherShowInput(1)");
        }else{
            loadUrl("javascript:mockIm.whetherShowInput(0)");
        }
    }

    /**
     * 设置用户基本信息
     * @param userName
     * @param gender male:男 female:女 unknown:保密
     * @param language 目前只提供cn
     */
    public BotWebView setUserInfo(String userName, String gender, String language){
        this.userName = userName;
        if(!"male".equals(gender) || !"female".equals(gender)){
            this.gender = "unknown";
        }
        this.language = language;
        return this;
    }

    public BotWebView setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    /**
     * 不再向外提供方法
     */
    public void setWebViewClient(WebViewClient webViewClient){}

    /**
     * 开启Url连接到Chatbot
     * @param appId 机器人id
     * @param channelId 渠道id
     * 注意：在所有设置之后调用
     */
    public void openChatbot(String appId, String channelId){
        this.appId = appId;
        this.channelId = channelId;
        this.clientId = BotClient.clientId;
        this.grantType = BotClient.grantType;
        this.clientSecret = BotClient.clientSecret;
        if(TextUtils.isEmpty(BotClient.baseUrl) || TextUtils.isEmpty(this.clientId) || TextUtils.isEmpty(this.grantType) || TextUtils.isEmpty(this.clientSecret) ){
            if(botWebViewListener != null){
                botWebViewListener.error(1003, "初始化失败，未设置必要的参数");
            }
            return;
        }
        getToken();
    }

    private void prepareRun(){
        UUID uuid = SystemUtil.getDeviceUUID(getContext());
        if(uuid != null){
            deviceId = uuid.toString();
        }
        String iidSaved = PreferenceUtils.getInstance(getContext()).getString(PreferenceUtils.KEY_INDENTITY_ID, "");
        String userIdSaved = PreferenceUtils.getInstance(getContext()).getString(PreferenceUtils.KEY_USER_ID, "");
        //从缓存里面获取iid，userId,判断iid是否有值，判断userId是否发生变化，如果iid没值，或者userId变化了，则重新请求
        if(TextUtils.isEmpty(iidSaved) || !userIdSaved.equals(userId)){
            //重新请求
            Map<String, String> params = new HashMap<>();
            params.put("device_id", deviceId);
            params.put("client_secret", clientSecret);
            params.put("user_id", userId);
            HttpExecute.getInstance().get(HttpUrl.getIID(), params, new ResponseListener<String>() {
                @Override
                public void onSuccess(String object) {
                    //解析得到iid
                    String errors = "";
                    try {
                        JSONObject jsonObject = new JSONObject(object);
                        errors = jsonObject.optString("errors", "");
                        JSONObject data = jsonObject.optJSONObject("data");
//                        Log.e("getIId_object", object);
                        if(TextUtils.isEmpty(errors) && data != null){
                            indentityId = data.optString("iid", "");
                            if(!TextUtils.isEmpty(indentityId)){
                                PreferenceUtils.getInstance(getContext()).save(PreferenceUtils.KEY_INDENTITY_ID, indentityId);
                                PreferenceUtils.getInstance(getContext()).save(PreferenceUtils.KEY_USER_ID, userId);
                                startRun();
                                return;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if(botWebViewListener != null){
                        botWebViewListener.error(1002, TextUtils.isEmpty(errors)?"初始化机器人失败":errors);
                    }
                }

                @Override
                public void onFailure(int errCode, String errMsg) {
                    if(botWebViewListener != null){
                        botWebViewListener.error(1002, "初始化机器人失败");
                    }
                }
            });
        }else{
            indentityId = iidSaved;
            startRun();
        }
    }

    private void getToken(){
        String tokenSaved = PreferenceUtils.getInstance(getContext()).getString(PreferenceUtils.KEY_ACCESS_TOKEN, "");
        long tokenExpiresIn = PreferenceUtils.getInstance(getContext()).getLong(PreferenceUtils.KEY_ACCESS_TOKEN_EXPIRES_IN, 0);
        long tokenSaveTime = PreferenceUtils.getInstance(getContext()).getLong(PreferenceUtils.KEY_ACCESS_TOKEN_SAVE_TIME, 0);

        long timeSpace = System.currentTimeMillis()/1000 - tokenSaveTime;
        if(TextUtils.isEmpty(tokenSaved) || timeSpace>=(tokenExpiresIn - 30)){
            //重新请求
            Map<String, String> params = new HashMap<>();
            params.put("grant_type", grantType);
            params.put("client_id", clientId);
            params.put("client_secret", clientSecret);
            HttpExecute.getInstance().get(HttpUrl.getToken(), params, new ResponseListener<String>() {
                @Override
                public void onSuccess(String object) {
                    String errors = "";
                    //解析得到Token
                    try {
//                        Log.e("getToken_object", object);
                        JSONObject jsonObject = new JSONObject(object);
                        errors = jsonObject.optString("errors", "");
                        JSONObject data = jsonObject.optJSONObject("data");
                        if(TextUtils.isEmpty(errors) && data != null){
                            token = data.optString("access_token", "");
                            String token_type = data.optString("token_type", "");
                            long expires_in = data.optLong("expires_in", 0);
                            long currSeconds = System.currentTimeMillis()/1000;
                            if(!TextUtils.isEmpty(token)){
                                PreferenceUtils.getInstance(getContext()).save(PreferenceUtils.KEY_ACCESS_TOKEN, token);
                                PreferenceUtils.getInstance(getContext()).save(PreferenceUtils.KEY_ACCESS_TOKEN_EXPIRES_IN, expires_in);
                                PreferenceUtils.getInstance(getContext()).save(PreferenceUtils.KEY_ACCESS_TOKEN_SAVE_TIME, currSeconds);
                                prepareRun();
                                return;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if(botWebViewListener != null){
                        botWebViewListener.error(1001, TextUtils.isEmpty(errors)?"初始化机器人失败":errors);
                    }
                }

                @Override
                public void onFailure(int errCode, String errMsg) {
                    if(botWebViewListener != null){
                        botWebViewListener.error(1001, "初始化机器人失败");
                    }
                }
            });
        }else{
            token = tokenSaved;
            prepareRun();
        }
    }

    private void startRun(){
        String url = HttpUrl.getChatBotUrl() + "?app_id=" + appId + "&channel_id=" + channelId + "&access_token=" + token;
        loadUrl(url);
    }

    /**
     * 关闭web页面上的输入控件
     * 注意:需要在页面初始化，并加载完成之后调用
     */
    public BotWebView closeWebInput(){
        if(isJsInit){
            loadUrl("javascript:mockIm.whetherShowInput(0)");
        }
        isShowWebInput = false;
        return this;
    }

    /**
     * 开启web页面上的输入控件
     * 注意:需要在页面初始化，并加载完成之后调用
     */
    public BotWebView openWebInput(){
        if(isJsInit){
            loadUrl("javascript:mockIm.whetherShowInput(1)");
        }
        isShowWebInput = true;
        return this;
    }

    public BotWebView setBotWebViewListener(BotWebViewListener botWebViewListener){
        this.botWebViewListener = botWebViewListener;
        return this;
    }

    /**
     * 发送文本消息
     * @param msg
     */
    public BotWebView sendText(String msg){
        loadUrl("javascript:mockIm.dialogueTemplate({text:'"+msg+"',localTimestamp: mockIm.getTimeRecord()}, true)");
        loadUrl("javascript:var data = mockIm.getActivity({localTimestamp: mockIm.getTimeRecord(),text: '"+msg+"'});mockIm.sendMessage(data)");
        return this;
    }

    public static class BotWebViewListener {
        public void onIntentDetailUrl(String url) {}
        //显示input输入框
        public void showNativeInput(){}
        //接收道语音文本json对象
        public void receiveSpeech(String speechData){}
        public void receiveFulfillments(String fulfillments){}
        public void error(int code, String errMsg){}
        public void back(){}
    }

    /** 需要注入的js对象*/
    class JavaScriptInterface {
        public JavaScriptInterface() {
        }
        //js发送消息到onReceiveMsg方法
        @JavascriptInterface
        public void onReceiveMsg(String jsonData) {
            try {
//                Log.e("onReceiveMsg", jsonData);
                Log.e("onReceiveMsg", "接收到消息");
                JSONObject jsonObject = new JSONObject(jsonData);
                String type = jsonObject.optString("type", "");
                if(botWebViewListener != null){
                    if("1".equals(type) || "show_input".equals(type)){
                        botWebViewListener.showNativeInput();
                    }else if("url_back".equals(type)){
                        botWebViewListener.back();
                    }else if("fulfillments".equals(type)){
                        JSONArray detailList = jsonObject.optJSONArray("data");
//                        Log.e("onReceiveMsg_data", detailList.toString());
                        botWebViewListener.receiveFulfillments(detailList.toString());
                    }else if("speech".equals(type)){
                        JSONArray detailList = jsonObject.optJSONArray("data");
//                        Log.e("onReceiveMsg_data", detailList.toString());
                        if(detailList != null && detailList.length()>0){
                            //取第一条的lang
                            String lang = "";
                            StringBuilder sb = new StringBuilder();
                            for(int i=0;i<detailList.length();i++){
                                JSONObject detailItem = detailList.optJSONObject(i);
                                if(i==0){
                                    lang = detailItem.optString("lang", "");
                                }
                                if(TextUtils.isEmpty(lang)){
                                    return;
                                }
                                JSONArray speechesIns = detailItem.optJSONArray("speeches");
                                if(speechesIns != null && speechesIns.length()>0){
                                    for(int j=0;j<speechesIns.length();j++){
                                        String speechesItem = speechesIns.optString(j);
                                        sb.append(speechesItem);
                                        sb.append("。");
                                    }
                                    botWebViewListener.receiveSpeech(sb.toString());
                                }
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
