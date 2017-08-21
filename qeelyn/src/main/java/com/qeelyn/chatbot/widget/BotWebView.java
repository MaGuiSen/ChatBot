package com.qeelyn.chatbot.widget;

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
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.qeelyn.chatbot.http.HttpExecute;
import com.qeelyn.chatbot.http.HttpUrl;
import com.qeelyn.chatbot.http.ResponseListener;
import com.qeelyn.chatbot.util.PreferenceUtils;
import com.qeelyn.chatbot.util.SystemUtil;

/**
 * 机器人聊天WevView
 */
public class BotWebView extends WebView{
    private String baseBotUrl = "http://117.29.166.222:8099/chatbot/mock/index";//"http://maguisen.top/chatbot_test.html?r=111";
    private String[] canLoadUrlStart = new String[]{"http://117.29.166.222:8099"};//以canLoadUrlStart开始的url字符串，可以继续加载

    private String token = "";//访问权限票据
    private String channelId = "";//渠道id
    private String appId = "";//chatbot的id
    private String secretId = "";//密钥id
    private String deviceId = "";//设备号 uuid
    private String userId = "";//用户id：对应不同的系统的用户唯一标识
    private String indentityId = "";//访问对象id
    private String userName = "";//用户名称
    private String gender = "";//性别
    private String language = "";//语言
    private BotWebViewListener botWebViewListener;
    private WebViewClient webViewClient;
    boolean isJsInit = false;

    public BotWebView(Context context) {
        this(context, null);
    }

    public BotWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void init(){
        WebSettings settings = getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setAllowFileAccess(true);
        setWebChromeClient(new WebChromeClient());
        super.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                boolean canLoad = false;
                for(int i=0;i<canLoadUrlStart.length;i++){
                    String urlStart = canLoadUrlStart[i];
                    if(url.startsWith(urlStart)){
                        canLoad = true;
                        break;
                    }
                }
                if(!canLoad){
                    view.stopLoading();
                    //不是本站的url就抛出去
                    if(webViewClient != null){
                        webViewClient.onPageStarted(view, url, favicon);
                    }
                    return;
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                //加载完成
                initJs();
                if(webViewClient != null){
                    webViewClient.onPageFinished(view, url);
                }
            }
        });
        // 注入一个js对象
        addJavascriptInterface(new JavaScriptInterface(),"JsNativeObject");
        //注入js代码，用于和前面的js对象同步调用，同步RN调用方式
        loadUrl("javascript:window.postMessage = function(data){JsNativeObject.onReceiveMsg(data)}");
    }

    private void initJs(){
        if(isJsInit){
            return;
        }
        isJsInit = true;
        if (!TextUtils.isEmpty(userName)) {
            loadUrl("javascript:mockIm.setIid('" + indentityId + "');mockIm.saveUser({from_id:'" + indentityId + "',uname:'" + userName + "',gender:'" + gender + "',language:'" + language + "'},function(){mockIm.init()});");
        } else {
            //初始化js
            loadUrl("javascript:mockIm.setIid('" + indentityId + "');mockIm.init();");
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

    public BotWebView setSecretId(String secretId) {
        this.secretId = secretId;
        return this;
    }

    public BotWebView setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    /** 设置连接的token*/
    public BotWebView setToken(String token){
        this.token = token;
        return this;
    }

    /**
     * 只提供部分浏览器回调方法
     */
    public void setWebViewClient(WebViewClient webViewClient){
        this.webViewClient = webViewClient;
    }

    /**
     * 开启Url连接到Chatbot
     * @param appId 机器人id
     * @param channelId 渠道id
     * 注意：在所有设置之后调用
     */
    public void openChatbotUrl(String appId,String channelId){
        this.appId = appId;
        this.channelId = channelId;
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
            params.put("secret_id", secretId);
            params.put("user_id", userId);
            HttpExecute.getInstance().get(HttpUrl.getIID, params, new ResponseListener<String>() {
                @Override
                public void onSuccess(String object) {
                    //解析得到iid
                    try {
                        JSONObject jsonObject = new JSONObject(object);
                        String errors = jsonObject.optString("errors", "");
                        JSONObject data = jsonObject.optJSONObject("data");
                        Log.e("getIId_errors", errors);
                        Log.e("getIId_data", data.toString());
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
                    Toast.makeText(getContext(), "初始化机器人失败", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFailure(int errCode, String errMsg) {
                    Toast.makeText(getContext(), "初始化机器人失败", Toast.LENGTH_LONG).show();
                }
            });
        }else{
            indentityId = iidSaved;
            startRun();
        }
    }

    private void getToken(){
        //重新请求
        Map<String, String> params = new HashMap<>();
        HttpExecute.getInstance().get(HttpUrl.getToken, params, new ResponseListener<String>() {
            @Override
            public void onSuccess(String object) {
                //解析得到Token
                try {
                    JSONObject jsonObject = new JSONObject(object);
                    String errors = jsonObject.optString("errors", "");
                    JSONObject data = jsonObject.optJSONObject("data");
                    Log.e("getToken_errors", errors);
                    Log.e("getToken_data", data.toString());
                    if(TextUtils.isEmpty(errors) && data != null){
                        token = data.optString("token", "");
                        if(!TextUtils.isEmpty(token)){
                            prepareRun();
                            return;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Toast.makeText(getContext(), "初始化机器人失败", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(int errCode, String errMsg) {
                Toast.makeText(getContext(), "初始化机器人失败", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void startRun(){
        String url = baseBotUrl + "?app_id=" + appId + "&channel_id=" + channelId + "&token=" + token;
        loadUrl(url);
    }

    /**
     * 关闭web页面上的输入控件
     * 注意:需要在页面初始化，并加载完成之后调用
     */
    public BotWebView closeWebInput(){
        loadUrl("javascript:mockIm.whetherShowInput(0)");
        return this;
    }

    /**
     * 开启web页面上的输入控件
     * 注意:需要在页面初始化，并加载完成之后调用
     */
    public BotWebView openWebInput(){
        loadUrl("javascript:mockIm.whetherShowInput(1)");
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

    /**
     * 发送消息对象
     */
    public BotWebView sendMessage(){
        return this;
    }

    /**
     * 发送json文本
     * @param json
     * @return
     */
    public BotWebView sendJson(String json){
        return this;
    }

    public static class BotWebViewListener {
        //显示input输入框
        public void showInputLay(){}
        //接收道语音文本json对象
        public void receiveSpeechData(String speechData){}
    }

    /** 需要注入的js对象*/
    class JavaScriptInterface {
        public JavaScriptInterface() {
        }
        //js发送消息到onReceiveMsg方法
        @JavascriptInterface
        public void onReceiveMsg(String jsonData) {
            try {
                JSONObject jsonObject = new JSONObject(jsonData);
                String type = jsonObject.optString("type", "");
                Log.e("onReceiveMsg_type", type);
                if(botWebViewListener != null){
                    if("1".equals(type)){
                        botWebViewListener.showInputLay();
                    }else if("speech".equals(type)){
                        JSONArray detailList = jsonObject.optJSONArray("data");
                        Log.e("onReceiveMsg_data", detailList.toString());
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
                                    botWebViewListener.receiveSpeechData(sb.toString());
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
