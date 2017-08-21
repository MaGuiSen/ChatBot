package lib.http;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Map;

/**
 * 网络请求封装
 */
public class HttpExecute {
    public static final int REQ_SUCCESS = 1;
    public static final int REQ_FAILURE = -1;

    private static HttpExecute httpExecute;

    private static final String TAG = HttpExecute.class.getSimpleName();

    private HttpExecute() {
    }

    public static HttpExecute getInstance() {
        if (httpExecute == null)
            httpExecute = new HttpExecute();
        return httpExecute;
    }

    public void post(final String requestUrl, final Map<String, String> paramsMap, final ResponseListener responseListener){
        final HttpHandler handler = new HttpHandler(responseListener);
        new Thread(){
            public void run(){
                try {
                    String paramString = createParamStr(paramsMap);
                    // 请求的参数转换为byte数组
                    byte[] postData = paramString.getBytes();
                    String url = requestUrl;
                    HttpURLConnection urlConn = getConnection(url, false);
                    // 发送请求参数
                    DataOutputStream dos = new DataOutputStream(urlConn.getOutputStream());
                    dos.write(postData);
                    dos.flush();
                    dos.close();
                    // 判断请求是否成功
                    if (urlConn.getResponseCode() == 200) {
                        String result = streamToString(urlConn.getInputStream());
                        sendSuccessMsg(handler, result);
                    } else {
                        Log.e(TAG, "数据非200"+url);
                        sendFailMsg(handler, "网络请求失败了");
                    }
                    // 关闭连接
                    urlConn.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                    sendFailMsg(handler, "网络请求失败了");
                }
            }
        }.start();
    }

    public <T> void get(final String requestUrl, final Map<String, String> paramsMap, final ResponseListener responseListener){
        final HttpHandler handler = new HttpHandler(responseListener);
        new Thread() {
            public void run() {
                try {
                    String paramString = createParamStr(paramsMap);
                    String url = requestUrl;
                    if (!paramString.equals("") && !paramString.equals("?")) {
                        //如果包含？说明url中可以直接添加参数：此时如果包含了&说明url中已经存在参数，则再添加新的参数时候需要添加一个&
                        url += url.contains("?") ? (url.contains("&") ? "&" : "") : "?";
                        url += paramString;
                    }
                    HttpURLConnection urlConn = getConnection(url, true);
                    // 判断请求是否成功
                    if (urlConn.getResponseCode() == 200) {
                        String result = streamToString(urlConn.getInputStream());
                        sendSuccessMsg(handler, result);
                    } else {
                        Log.e(TAG, "数据非200："+url);
                        sendFailMsg(handler, "网络请求失败了");
                    }
                    // 关闭连接
                    urlConn.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                    sendFailMsg(handler, "网络请求失败了");
                }
            }
        }.start();
    }

    private void sendFailMsg(Handler handler, String errMsg){
        Message msg = new Message();
        msg.what = REQ_FAILURE;
        msg.obj = errMsg;
        handler.sendMessage(msg);
    }

    private void sendSuccessMsg(Handler handler, String resultStr){
        Message msg = new Message();
        msg.what = REQ_SUCCESS;
        msg.obj = resultStr;
        handler.sendMessage(msg);
    }

    /**
     * 合成参数
     */
    private String createParamStr(Map<String, String> params) throws UnsupportedEncodingException {
        StringBuilder tempParams = new StringBuilder();
        int pos = 0;
        for (String key : params.keySet()) {
            if (pos > 0) {
                tempParams.append("&");
            }
            tempParams.append(String.format("%s=%s", key, URLEncoder.encode(params.get(key),"utf-8")));
            pos++;
        }
        return tempParams.toString();
    }

    private HttpURLConnection getConnection(String requestUrl, boolean isGet) throws Exception {
        // 新建一个URL对象
        URL url = new URL(requestUrl);
        // 打开一个HttpURLConnection连接
        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
        // 设置连接超时时间
        urlConn.setConnectTimeout(5 * 1000);
        //设置从主机读取数据超时
        urlConn.setReadTimeout(5 * 1000);
        if(isGet){
            // 设置是否使用缓存  默认是true
            urlConn.setUseCaches(true);
            // 设置为Post请求
            urlConn.setRequestMethod("GET");
        }else{
            // Post请求必须设置允许输出 默认false
            urlConn.setDoOutput(true);
            //设置请求允许输入 默认是true
            urlConn.setDoInput(true);
            // Post请求不能使用缓存
            urlConn.setUseCaches(false);
            // 设置为Post请求
            urlConn.setRequestMethod("POST");
        }
        //设置本次连接是否自动处理重定向
        urlConn.setInstanceFollowRedirects(true);
        //设置请求中的媒体类型信息。
        urlConn.setRequestProperty("Content-Type", "application/json");
        //设置客户端与服务连接类型
        urlConn.addRequestProperty("Connection", "Keep-Alive");
        urlConn.addRequestProperty("Accept", "application/json, text/javascript, */*; q=0.01");
        // 开始连接
        urlConn.connect();
        return urlConn;
    }

    /**
     * 将输入流转换成字符串
     */
    private String streamToString(InputStream is) throws Exception{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = is.read(buffer)) != -1) {
            baos.write(buffer, 0, len);
        }
        baos.close();
        is.close();
        byte[] byteArray = baos.toByteArray();
        return new String(byteArray);
    }
}
