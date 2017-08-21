package lib.third.speech;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;

import lib.third.speech.util.JsonParser;

public class IflySTTModule{
    // 语音听写对象
    private SpeechRecognizer mIat;
    Context context;

    private static IflySTTModule iflyTTSModule = null;
    public synchronized static IflySTTModule getInstance(Context context){
        if(iflyTTSModule == null){
            iflyTTSModule = new IflySTTModule(context);
        }
        return iflyTTSModule;
    }

    private IflySTTModule(Context context) {
        this.context = context;
    }

    public void start() {
        // 设置参数
        if(mIat == null){
            mIat = SpeechRecognizer.createRecognizer(context, mInitListener);
            setParam();
        }
        // 不显示听写对话框
        int ret = mIat.startListening(mRecognizerListener);
        if (ret != ErrorCode.SUCCESS) {
            showTip("听写失败,错误码：" + ret);
            if(listener != null){
                listener.onError("听写失败,错误码：" + ret);
            }
        } else {
            showTip("请开始说话…");
        }
    }

    public void remove() {
        if(mIat != null){
            mIat.stopListening();
            mIat.destroy();
            mIat = null;
        }
    }


    public void stop() {
        mIat.stopListening();
    }

    /**
     * 初始化监听器。
     */
    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败，错误码：" + code);
                if(listener != null){
                    listener.onError("听写失败,错误码：" + code);
                }
            }
        }
    };

    /**
    * 听写监听器。
    */
    private RecognizerListener mRecognizerListener = new RecognizerListener() {

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            showTip("请开始说话");
            if(listener != null){
                listener.onBeginOfSpeech();
            }
        }

        @Override
        public void onError(SpeechError error) {
            showTip(error.getPlainDescription(true));
            // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
            // 如果使用本地功能（语记）需要提示用户开启语记的录音权限。
            if(listener != null){
                listener.onError(error.getPlainDescription(true));
            }
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            showTip("结束说话");
            if(listener != null){
                listener.onEndOfSpeech();
            }
        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            showTip(results.getResultString());
            if(listener != null){
                listener.onResult(printResult(results), isLast);
            }
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            showTip("当前正在说话，音量大小：" + volume);
            int db = 1;
            if(volume >= 0 && volume < 5){
                db = 1;
            }else if(volume >= 5 && volume < 10){
                db = 2;
            }else if(volume >= 10 && volume < 20){
                db = 3;
            }else if(volume >= 20 && volume < 30){
                db = 4;
            }else{
                db = 5;
            }
            if(listener != null){
                listener.onVolumeChanged(volume, db);
            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
          // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
          // 若使用本地能力，会话id为null
          //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
          //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
          //		Log.d(TAG, "session id =" + sid);
          //	}
        }
    };


    private String printResult(RecognizerResult results) {
        String text = JsonParser.parseIatResult(results.getResultString());

        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        HashMap<String, String> mIatResults = new LinkedHashMap<>();
        mIatResults.put(sn, text);

        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }
        String result = resultBuffer.toString();
        return result;
    }

    private void showTip(final String str) {
        Log.e("++++++++++++++++++++",str);
    }

    /**
     * 参数设置
     *
     * @return
     */
    private void setParam() {
        if(mIat == null){
            return;
        }
        // 清空参数
        mIat.setParameter(SpeechConstant.PARAMS, null);

        // 设置听写引擎 云端
        mIat.setParameter(SpeechConstant.ENGINE_TYPE,  SpeechConstant.TYPE_CLOUD);
        // 设置返回结果格式
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");

        if (false) {
            // 设置语言
            mIat.setParameter(SpeechConstant.LANGUAGE, "en_us");
        } else {
            // 设置语言
            mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
            // 设置语言区域
            mIat.setParameter(SpeechConstant.ACCENT, "mandarin");//普通话 mandarin
        }

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, "4000");

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, "1000");

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, "1");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mIat.setParameter(SpeechConstant.AUDIO_FORMAT,"wav");
        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/iat.wav");
    }

    public IflySTTModule setListener(IflySTTListener listener) {
        this.listener = listener;
        return this;
    }

    private IflySTTListener listener;
    public class IflySTTListener{

        //开始说话
        public void onBeginOfSpeech() {}

        //结束说话
        public void onEndOfSpeech() {}

        //得到结果
        public void onResult(String results, boolean isLast) {}

        //音量变化
        public void onVolumeChanged(int volume, int volumeLevel) {}


        //播放完成
        public void onCompleted() {}

        //播放出错
        public void onError(String errMsg) {}
    }
}
